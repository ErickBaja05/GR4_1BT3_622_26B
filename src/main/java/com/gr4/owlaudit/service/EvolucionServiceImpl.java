package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dto.ComparacionReglaDTO;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO.AuditoriaItemDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EvolucionServiceImpl implements EvolucionService {

    private static final String MENSAJE_SIN_AUDITORIAS_SUFICIENTES = "El proyecto no tiene suficientes auditorías para comparar";
    private static final String MENSAJE_CONSULTA_CANCELADA = "Consulta cancelada";
    private static final String MENSAJE_AUDITORIAS_IGUALES = "Debe indicar dos auditorías distintas";
    private static final String MENSAJE_SIN_HALLAZGOS = "No existen hallazgos en las auditorías comparadas";

    private final AuditoriaDAO auditoriaDAO;

    public EvolucionServiceImpl() {
        this(new AuditoriaDAOImpl());
    }

    public EvolucionServiceImpl(AuditoriaDAO auditoriaDAO) {
        this.auditoriaDAO = auditoriaDAO;
    }

    @Override
    public ResumenHistorialDTO solicitarEvolucionDeCalidad(EvolucionDTO dto) {
        if (dto == null || dto.getProyectoId() == null || dto.getProyectoId() <= 0) {
            throw new ExcepcionNegocio("El identificador del proyecto no es válido.");
        }

        List<Auditoria> historial = auditoriaDAO.consultarHistorialDeAuditorias(dto.getProyectoId());

        // Escenario Alternativo 1
        if (historial == null || historial.size() < 2) {
            throw new ExcepcionNegocio(MENSAJE_SIN_AUDITORIAS_SUFICIENTES);
        }

        // Ordenar auditorías por fecha cronológica ascendente
        historial.sort(Comparator.comparing(Auditoria::getFechaHora));

        List<AuditoriaItemDTO> items = new ArrayList<>();
        for (Auditoria a : historial) {
            items.add(new AuditoriaItemDTO(
                a.getId(),
                a.getFechaHora(),
                a.getPuntajeObtenido(),
                a.getPuntajeMaximo(),
                a.getPorcentaje()
            ));
        }

        return new ResumenHistorialDTO(dto.getProyectoId(), items);
    }

    @Override
    public EvolucionFinalDTO indicarDosAuditoriasAComparar(EvolucionDTO dto) {
        // Escenario Alternativo 2: no se indican las dos auditorías
        if (dto == null || dto.getBaseId() == null || dto.getComparadaId() == null) {
            throw new ExcepcionNegocio(MENSAJE_CONSULTA_CANCELADA);
        }

        // Escenario Alternativo 3: se indican dos auditorías iguales
        if (dto.getBaseId().equals(dto.getComparadaId())) {
            throw new ExcepcionNegocio(MENSAJE_AUDITORIAS_IGUALES);
        }

        // 1. Ordenar auditorías por fecha (más antigua como base y más reciente como comparada)
        ordenarAuditoriasPorFecha(dto);

        // 2. Consultar resultados de reglas para ambas auditorías
        List<ResultadoRegla> resultados = auditoriaDAO.consultarResultadosDeReglas(dto.getBaseId(), dto.getComparadaId());

        Auditoria audBase = null;
        Auditoria audComparada = null;
        Map<Long, ResultadoRegla> resultadosBase = new LinkedHashMap<>();
        Map<Long, ResultadoRegla> resultadosComparada = new LinkedHashMap<>();
        Map<Long, Regla> reglasMap = new LinkedHashMap<>();

        for (ResultadoRegla r : resultados) {
            if (r.getAuditoria() != null) {
                if (r.getAuditoria().getId().equals(dto.getBaseId())) {
                    if (audBase == null) audBase = r.getAuditoria();
                    if (r.getRegla() != null) {
                        resultadosBase.put(r.getRegla().getId(), r);
                        reglasMap.put(r.getRegla().getId(), r.getRegla());
                    }
                } else if (r.getAuditoria().getId().equals(dto.getComparadaId())) {
                    if (audComparada == null) audComparada = r.getAuditoria();
                    if (r.getRegla() != null) {
                        resultadosComparada.put(r.getRegla().getId(), r);
                        reglasMap.put(r.getRegla().getId(), r.getRegla());
                    }
                }
            }
        }

        // Si audBase o audComparada aún son null (por ejemplo si no tenían resultados de regla o en mock DAO)
        if ((audBase == null || audComparada == null) && dto.getProyectoId() != null) {
            List<Auditoria> historial = auditoriaDAO.consultarHistorialDeAuditorias(dto.getProyectoId());
            if (historial != null) {
                for (Auditoria a : historial) {
                    if (a.getId().equals(dto.getBaseId())) audBase = a;
                    if (a.getId().equals(dto.getComparadaId())) audComparada = a;
                }
            }
        }

        EvolucionFinalDTO evolucionFinal = new EvolucionFinalDTO();
        if (audBase != null) {
            evolucionFinal.setBaseId(audBase.getId());
            evolucionFinal.setFechaHoraBase(audBase.getFechaHora());
            evolucionFinal.setPuntajeObtenidoBase(audBase.getPuntajeObtenido());
            evolucionFinal.setPuntajeMaximoBase(audBase.getPuntajeMaximo());
            evolucionFinal.setPorcentajeBase(audBase.getPorcentaje());
        }
        if (audComparada != null) {
            evolucionFinal.setComparadaId(audComparada.getId());
            evolucionFinal.setFechaHoraComparada(audComparada.getFechaHora());
            evolucionFinal.setPuntajeObtenidoComparada(audComparada.getPuntajeObtenido());
            evolucionFinal.setPuntajeMaximoComparada(audComparada.getPuntajeMaximo());
            evolucionFinal.setPorcentajeComparada(audComparada.getPorcentaje());
        }

        // 3. Calcular variación de puntajes
        calcularVariacionDePuntajes(evolucionFinal);

        // 4. Comparar cada regla y clasificar el estado del hallazgo
        List<ComparacionReglaDTO> comparaciones = new ArrayList<>();
        for (Regla regla : reglasMap.values()) {
            ResultadoRegla resB = resultadosBase.get(regla.getId());
            ResultadoRegla resC = resultadosComparada.get(regla.getId());
            boolean cumpleBase = resB != null && resB.isCumple();
            boolean cumpleComp = resC != null && resC.isCumple();

            String estado = clasificarEstadoDelHallazgo(cumpleBase, cumpleComp);
            if (estado != null) {
                String evidencia = "";
                String recomendacion = "";
                if (resC != null && resC.getHallazgo() != null) {
                    evidencia = resC.getHallazgo().getEvidencia();
                    recomendacion = resC.getHallazgo().getRecomendacion();
                } else if (resB != null && resB.getHallazgo() != null) {
                    evidencia = resB.getHallazgo().getEvidencia();
                    recomendacion = resB.getHallazgo().getRecomendacion();
                }
                comparaciones.add(new ComparacionReglaDTO(
                    regla.getNombreRepresentativo(),
                    regla.getNivelSeveridad(),
                    estado,
                    evidencia,
                    recomendacion
                ));
            }
        }
        evolucionFinal.setComparaciones(comparaciones);

        // 5. Contar hallazgos por estado
        contarHallazgosPorEstado(evolucionFinal);

        // 6. Escenario Alternativo 4: todas las reglas cumplidas en ambas auditorías
        if (comparaciones.isEmpty()) {
            evolucionFinal.setMensaje(MENSAJE_SIN_HALLAZGOS);
        }

        return evolucionFinal;
    }

    // ========================================================
    // MÉTODOS PRIVADOS DEL DIAGRAMA DE CLASES
    // ========================================================

    private void ordenarAuditoriasPorFecha(EvolucionDTO dto) {
        if (dto.getProyectoId() != null) {
            List<Auditoria> historial = auditoriaDAO.consultarHistorialDeAuditorias(dto.getProyectoId());
            if (historial != null) {
                Auditoria aud1 = null;
                Auditoria aud2 = null;
                for (Auditoria a : historial) {
                    if (a.getId().equals(dto.getBaseId())) aud1 = a;
                    if (a.getId().equals(dto.getComparadaId())) aud2 = a;
                }
                if (aud1 != null && aud2 != null && aud1.getFechaHora().isAfter(aud2.getFechaHora())) {
                    Long temp = dto.getBaseId();
                    dto.setBaseId(dto.getComparadaId());
                    dto.setComparadaId(temp);
                }
            }
        }
    }

    private void calcularVariacionDePuntajes() {
        // Método trazable 1:1 con el Diagrama de Clases
    }

    private void calcularVariacionDePuntajes(EvolucionFinalDTO evolucionFinal) {
        calcularVariacionDePuntajes();
        int variacionPuntos = evolucionFinal.getPuntajeObtenidoComparada() - evolucionFinal.getPuntajeObtenidoBase();
        double variacionPorcentaje = Math.round((evolucionFinal.getPorcentajeComparada() - evolucionFinal.getPorcentajeBase()) * 10.0) / 10.0;
        evolucionFinal.setVariacionPuntaje(variacionPuntos);
        evolucionFinal.setVariacionPorcentaje(variacionPorcentaje);
    }

    private void clasificarEstadoDelHallazgo() {
        // Método trazable 1:1 con el Diagrama de Clases
    }

    private String clasificarEstadoDelHallazgo(boolean cumpleBase, boolean cumpleComparada) {
        clasificarEstadoDelHallazgo();
        if (cumpleBase) {
            if (!cumpleComparada) {
                return "Nuevo";
            }
            return null; // Cumple en ambas -> sin hallazgo
        } else {
            if (cumpleComparada) {
                return "Corregido";
            } else {
                return "Persistente";
            }
        }
    }

    private void contarHallazgosPorEstado() {
        // Método trazable 1:1 con el Diagrama de Clases
    }

    private void contarHallazgosPorEstado(EvolucionFinalDTO evolucionFinal) {
        contarHallazgosPorEstado();
        int nuevos = 0;
        int persistentes = 0;
        int corregidos = 0;

        for (ComparacionReglaDTO item : evolucionFinal.getComparaciones()) {
            if ("Nuevo".equalsIgnoreCase(item.getEstado())) {
                nuevos++;
            } else if ("Persistente".equalsIgnoreCase(item.getEstado())) {
                persistentes++;
            } else if ("Corregido".equalsIgnoreCase(item.getEstado())) {
                corregidos++;
            }
        }

        evolucionFinal.setTotalNuevos(nuevos);
        evolucionFinal.setTotalPersistentes(persistentes);
        evolucionFinal.setTotalCorregidos(corregidos);
    }
}
