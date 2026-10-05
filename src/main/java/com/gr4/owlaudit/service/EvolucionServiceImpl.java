package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO.ComparacionRegla;
import com.gr4.owlaudit.dto.AuditoriaResumenDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
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

    // Estado interno utilizado por los métodos privados del Diagrama de Clases
    private Auditoria auditoriaBase;
    private Auditoria auditoriaComparada;
    private EvolucionFinalDTO evolucionFinalDTO;
    private Regla reglaEnEvaluacion;
    private boolean cumpleBaseActual;
    private boolean cumpleComparadaActual;
    private ResultadoRegla resultadoBaseActual;
    private ResultadoRegla resultadoComparadaActual;

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

        if (historial == null || historial.size() < 2) {
            throw new ExcepcionNegocio(MENSAJE_SIN_AUDITORIAS_SUFICIENTES);
        }

        historial.sort(Comparator.comparing(Auditoria::getFechaHora));

        List<AuditoriaResumenDTO> items = new ArrayList<>();
        for (Auditoria a : historial) {
            items.add(new AuditoriaResumenDTO(
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
    public synchronized EvolucionFinalDTO indicarDosAuditoriasAComparar(EvolucionDTO dto) {
        if (dto == null || dto.getBaseId() == null || dto.getComparadaId() == null) {
            throw new ExcepcionNegocio(MENSAJE_CONSULTA_CANCELADA);
        }

        if (dto.getBaseId().equals(dto.getComparadaId())) {
            throw new ExcepcionNegocio(MENSAJE_AUDITORIAS_IGUALES);
        }

        // 1. Ordenar auditorías por fecha
        ordenarAuditoriasPorFecha(dto);

        // 2. Consultar resultados de reglas
        List<ResultadoRegla> resultados = auditoriaDAO.consultarResultadosDeReglas(dto.getBaseId(), dto.getComparadaId());

        this.auditoriaBase = null;
        this.auditoriaComparada = null;
        Map<Long, ResultadoRegla> resultadosBase = new LinkedHashMap<>();
        Map<Long, ResultadoRegla> resultadosComparada = new LinkedHashMap<>();
        Map<Long, Regla> reglasMap = new LinkedHashMap<>();

        for (ResultadoRegla r : resultados) {
            if (r.getAuditoria() != null) {
                if (r.getAuditoria().getId().equals(dto.getBaseId())) {
                    if (this.auditoriaBase == null) this.auditoriaBase = r.getAuditoria();
                    if (r.getRegla() != null) {
                        resultadosBase.put(r.getRegla().getId(), r);
                        reglasMap.put(r.getRegla().getId(), r.getRegla());
                    }
                } else if (r.getAuditoria().getId().equals(dto.getComparadaId())) {
                    if (this.auditoriaComparada == null) this.auditoriaComparada = r.getAuditoria();
                    if (r.getRegla() != null) {
                        resultadosComparada.put(r.getRegla().getId(), r);
                        reglasMap.put(r.getRegla().getId(), r.getRegla());
                    }
                }
            }
        }

        if ((this.auditoriaBase == null || this.auditoriaComparada == null) && dto.getProyectoId() != null) {
            List<Auditoria> historial = auditoriaDAO.consultarHistorialDeAuditorias(dto.getProyectoId());
            if (historial != null) {
                for (Auditoria a : historial) {
                    if (a.getId().equals(dto.getBaseId())) this.auditoriaBase = a;
                    if (a.getId().equals(dto.getComparadaId())) this.auditoriaComparada = a;
                }
            }
        }

        this.evolucionFinalDTO = new EvolucionFinalDTO();
        if (this.auditoriaBase != null) {
            this.evolucionFinalDTO.setBaseId(this.auditoriaBase.getId());
            this.evolucionFinalDTO.setFechaHoraBase(this.auditoriaBase.getFechaHora());
            this.evolucionFinalDTO.setPuntajeObtenidoBase(this.auditoriaBase.getPuntajeObtenido());
            this.evolucionFinalDTO.setPuntajeMaximoBase(this.auditoriaBase.getPuntajeMaximo());
            this.evolucionFinalDTO.setPorcentajeBase(this.auditoriaBase.getPorcentaje());
        }
        if (this.auditoriaComparada != null) {
            this.evolucionFinalDTO.setComparadaId(this.auditoriaComparada.getId());
            this.evolucionFinalDTO.setFechaHoraComparada(this.auditoriaComparada.getFechaHora());
            this.evolucionFinalDTO.setPuntajeObtenidoComparada(this.auditoriaComparada.getPuntajeObtenido());
            this.evolucionFinalDTO.setPuntajeMaximoComparada(this.auditoriaComparada.getPuntajeMaximo());
            this.evolucionFinalDTO.setPorcentajeComparada(this.auditoriaComparada.getPorcentaje());
        }

        // 3. Calcular variación de puntajes
        calcularVariacionDePuntajes();

        // 4. Bucle por cada regla para clasificar el estado del hallazgo
        for (Regla regla : reglasMap.values()) {
            this.reglaEnEvaluacion = regla;
            this.resultadoBaseActual = resultadosBase.get(regla.getId());
            this.resultadoComparadaActual = resultadosComparada.get(regla.getId());
            this.cumpleBaseActual = this.resultadoBaseActual != null && this.resultadoBaseActual.isCumple();
            this.cumpleComparadaActual = this.resultadoComparadaActual != null && this.resultadoComparadaActual.isCumple();

            clasificarEstadoDelHallazgo();
        }

        // 5. Contar hallazgos por estado
        contarHallazgosPorEstado();

        // 6. Mensaje en caso de no haber hallazgos
        if (this.evolucionFinalDTO.getComparaciones().isEmpty()) {
            this.evolucionFinalDTO.setMensaje(MENSAJE_SIN_HALLAZGOS);
        }

        return this.evolucionFinalDTO;
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
        int variacionPuntos = this.evolucionFinalDTO.getPuntajeObtenidoComparada() - this.evolucionFinalDTO.getPuntajeObtenidoBase();
        double variacionPorcentaje = Math.round((this.evolucionFinalDTO.getPorcentajeComparada() - this.evolucionFinalDTO.getPorcentajeBase()) * 10.0) / 10.0;
        this.evolucionFinalDTO.setVariacionPuntaje(variacionPuntos);
        this.evolucionFinalDTO.setVariacionPorcentaje(variacionPorcentaje);
    }

    private void clasificarEstadoDelHallazgo() {
        String estado = null;
        if (this.cumpleBaseActual) {
            if (!this.cumpleComparadaActual) {
                estado = "Nuevo";
            }
        } else {
            if (this.cumpleComparadaActual) {
                estado = "Corregido";
            } else {
                estado = "Persistente";
            }
        }

        if (estado != null && this.reglaEnEvaluacion != null) {
            String evidencia = "";
            String recomendacion = "";
            if (this.resultadoComparadaActual != null && this.resultadoComparadaActual.getHallazgo() != null) {
                evidencia = this.resultadoComparadaActual.getHallazgo().getEvidencia();
                recomendacion = this.resultadoComparadaActual.getHallazgo().getRecomendacion();
            } else if (this.resultadoBaseActual != null && this.resultadoBaseActual.getHallazgo() != null) {
                evidencia = this.resultadoBaseActual.getHallazgo().getEvidencia();
                recomendacion = this.resultadoBaseActual.getHallazgo().getRecomendacion();
            }
            this.evolucionFinalDTO.getComparaciones().add(new ComparacionRegla(
                this.reglaEnEvaluacion.getNombreRepresentativo(),
                this.reglaEnEvaluacion.getNivelSeveridad(),
                estado,
                evidencia,
                recomendacion
            ));
        }
    }

    private void contarHallazgosPorEstado() {
        int nuevos = 0;
        int persistentes = 0;
        int corregidos = 0;

        for (ComparacionRegla item : this.evolucionFinalDTO.getComparaciones()) {
            if ("Nuevo".equalsIgnoreCase(item.getEstado())) {
                nuevos++;
            } else if ("Persistente".equalsIgnoreCase(item.getEstado())) {
                persistentes++;
            } else if ("Corregido".equalsIgnoreCase(item.getEstado())) {
                corregidos++;
            }
        }

        this.evolucionFinalDTO.setTotalNuevos(nuevos);
        this.evolucionFinalDTO.setTotalPersistentes(persistentes);
        this.evolucionFinalDTO.setTotalCorregidos(corregidos);
    }
}
