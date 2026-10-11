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

    private static class ContextoComparacion {
        Auditoria auditoriaBase;
        Auditoria auditoriaComparada;
        EvolucionFinalDTO evolucionFinalDTO;
        Regla reglaEnEvaluacion;
        ResultadoRegla resultadoBaseActual;
        ResultadoRegla resultadoComparadaActual;
    }

    private final ThreadLocal<ContextoComparacion> contextoLocal = new ThreadLocal<>();

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
    public EvolucionFinalDTO indicarDosAuditoriasAComparar(EvolucionDTO dto) {
        if (dto == null || dto.getBaseId() == null || dto.getComparadaId() == null) {
            throw new ExcepcionNegocio(MENSAJE_CONSULTA_CANCELADA);
        }

        if (dto.getBaseId().equals(dto.getComparadaId())) {
            throw new ExcepcionNegocio(MENSAJE_AUDITORIAS_IGUALES);
        }

        ContextoComparacion ctx = new ContextoComparacion();
        contextoLocal.set(ctx);
        try {
            // 1. Ordenar auditorías por fecha
            ordenarAuditoriasPorFecha(dto);

            // 2. Consultar resultados de reglas
            List<ResultadoRegla> resultados = auditoriaDAO.consultarResultadosDeReglas(dto.getBaseId(), dto.getComparadaId());

            Map<Long, ResultadoRegla> resultadosBase = new LinkedHashMap<>();
            Map<Long, ResultadoRegla> resultadosComparada = new LinkedHashMap<>();
            Map<Long, Regla> reglasMap = new LinkedHashMap<>();

            for (ResultadoRegla r : resultados) {
                if (r.getAuditoria() != null) {
                    if (r.getAuditoria().getId().equals(dto.getBaseId())) {
                        if (ctx.auditoriaBase == null) ctx.auditoriaBase = r.getAuditoria();
                        if (r.getRegla() != null) {
                            resultadosBase.put(r.getRegla().getId(), r);
                            reglasMap.put(r.getRegla().getId(), r.getRegla());
                        }
                    } else if (r.getAuditoria().getId().equals(dto.getComparadaId())) {
                        if (ctx.auditoriaComparada == null) ctx.auditoriaComparada = r.getAuditoria();
                        if (r.getRegla() != null) {
                            resultadosComparada.put(r.getRegla().getId(), r);
                            reglasMap.put(r.getRegla().getId(), r.getRegla());
                        }
                    }
                }
            }

            if ((ctx.auditoriaBase == null || ctx.auditoriaComparada == null) && dto.getProyectoId() != null) {
                List<Auditoria> historial = auditoriaDAO.consultarHistorialDeAuditorias(dto.getProyectoId());
                if (historial != null) {
                    for (Auditoria a : historial) {
                        if (a.getId().equals(dto.getBaseId())) ctx.auditoriaBase = a;
                        if (a.getId().equals(dto.getComparadaId())) ctx.auditoriaComparada = a;
                    }
                }
            }

            ctx.evolucionFinalDTO = new EvolucionFinalDTO();
            if (ctx.auditoriaBase != null) {
                ctx.evolucionFinalDTO.setBaseId(ctx.auditoriaBase.getId());
                ctx.evolucionFinalDTO.setFechaHoraBase(ctx.auditoriaBase.getFechaHora());
                ctx.evolucionFinalDTO.setPuntajeObtenidoBase(ctx.auditoriaBase.getPuntajeObtenido());
                ctx.evolucionFinalDTO.setPuntajeMaximoBase(ctx.auditoriaBase.getPuntajeMaximo());
                ctx.evolucionFinalDTO.setPorcentajeBase(ctx.auditoriaBase.getPorcentaje());
            }
            if (ctx.auditoriaComparada != null) {
                ctx.evolucionFinalDTO.setComparadaId(ctx.auditoriaComparada.getId());
                ctx.evolucionFinalDTO.setFechaHoraComparada(ctx.auditoriaComparada.getFechaHora());
                ctx.evolucionFinalDTO.setPuntajeObtenidoComparada(ctx.auditoriaComparada.getPuntajeObtenido());
                ctx.evolucionFinalDTO.setPuntajeMaximoComparada(ctx.auditoriaComparada.getPuntajeMaximo());
                ctx.evolucionFinalDTO.setPorcentajeComparada(ctx.auditoriaComparada.getPorcentaje());
            }

            // 3. Calcular variación de puntajes
            calcularVariacionDePuntajes();

            // 4. Bucle por cada regla para clasificar el estado del hallazgo
            for (Regla regla : reglasMap.values()) {
                ctx.reglaEnEvaluacion = regla;
                ctx.resultadoBaseActual = resultadosBase.get(regla.getId());
                ctx.resultadoComparadaActual = resultadosComparada.get(regla.getId());

                clasificarEstadoDelHallazgo();
            }

            // 5. Contar hallazgos por estado
            contarHallazgosPorEstado();

            // 6. Mensaje en caso de no haber hallazgos
            if (ctx.evolucionFinalDTO.getComparaciones().isEmpty()) {
                ctx.evolucionFinalDTO.setMensaje(MENSAJE_SIN_HALLAZGOS);
            }

            return ctx.evolucionFinalDTO;
        } finally {
            contextoLocal.remove();
        }
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
        ContextoComparacion ctx = contextoLocal.get();
        if (ctx == null || ctx.evolucionFinalDTO == null) return;
        int variacionPuntos = ctx.evolucionFinalDTO.getPuntajeObtenidoComparada() - ctx.evolucionFinalDTO.getPuntajeObtenidoBase();
        double variacionPorcentaje = Math.round((ctx.evolucionFinalDTO.getPorcentajeComparada() - ctx.evolucionFinalDTO.getPorcentajeBase()) * 10.0) / 10.0;
        ctx.evolucionFinalDTO.setVariacionPuntaje(variacionPuntos);
        ctx.evolucionFinalDTO.setVariacionPorcentaje(variacionPorcentaje);
    }

    private void clasificarEstadoDelHallazgo() {
        ContextoComparacion ctx = contextoLocal.get();
        if (ctx == null || ctx.evolucionFinalDTO == null) return;

        String estado = null;
        if (cumpleBaseActual()) {
            if (!cumpleComparadaActual()) {
                estado = "Nuevo";
            }
        } else {
            if (cumpleComparadaActual()) {
                estado = "Corregido";
            } else {
                estado = "Persistente";
            }
        }

        if (estado != null && ctx.reglaEnEvaluacion != null) {
            String evidencia = "";
            String recomendacion = "";
            Long hallazgoId = null;
            if (ctx.resultadoComparadaActual != null && ctx.resultadoComparadaActual.getHallazgo() != null) {
                evidencia = ctx.resultadoComparadaActual.getHallazgo().getEvidencia();
                recomendacion = ctx.resultadoComparadaActual.getHallazgo().getRecomendacion();
                hallazgoId = ctx.resultadoComparadaActual.getHallazgo().getId();
            } else if (ctx.resultadoBaseActual != null && ctx.resultadoBaseActual.getHallazgo() != null) {
                evidencia = ctx.resultadoBaseActual.getHallazgo().getEvidencia();
                recomendacion = ctx.resultadoBaseActual.getHallazgo().getRecomendacion();
                hallazgoId = ctx.resultadoBaseActual.getHallazgo().getId();
            }
            ctx.evolucionFinalDTO.getComparaciones().add(new ComparacionRegla(
                ctx.reglaEnEvaluacion.getNombreRepresentativo(),
                ctx.reglaEnEvaluacion.getNivelSeveridad(),
                estado,
                evidencia,
                recomendacion,
                hallazgoId
            ));
        }
    }

    private void contarHallazgosPorEstado() {
        ContextoComparacion ctx = contextoLocal.get();
        if (ctx == null || ctx.evolucionFinalDTO == null) return;

        int nuevos = 0;
        int persistentes = 0;
        int corregidos = 0;

        for (ComparacionRegla item : ctx.evolucionFinalDTO.getComparaciones()) {
            if ("Nuevo".equalsIgnoreCase(item.getEstado())) {
                nuevos++;
            } else if ("Persistente".equalsIgnoreCase(item.getEstado())) {
                persistentes++;
            } else if ("Corregido".equalsIgnoreCase(item.getEstado())) {
                corregidos++;
            }
        }

        ctx.evolucionFinalDTO.setTotalNuevos(nuevos);
        ctx.evolucionFinalDTO.setTotalPersistentes(persistentes);
        ctx.evolucionFinalDTO.setTotalCorregidos(corregidos);
    }

    private boolean cumpleBaseActual() {
        ContextoComparacion ctx = contextoLocal.get();
        return ctx != null && ctx.resultadoBaseActual != null && ctx.resultadoBaseActual.isCumple();
    }

    private boolean cumpleComparadaActual() {
        ContextoComparacion ctx = contextoLocal.get();
        return ctx != null && ctx.resultadoComparadaActual != null && ctx.resultadoComparadaActual.isCumple();
    }
}
