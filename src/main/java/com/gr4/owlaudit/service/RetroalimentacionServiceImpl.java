package com.gr4.owlaudit.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dao.SolicitudDAOImpl;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.EstadoRetroEnum;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;

public class RetroalimentacionServiceImpl implements RetroalimentacionService {

    private static final String MENSAJE_ORIENTACION_INVALIDA = "Orientación en blanco o inválida";
    private static final String MENSAJE_HALLAZGO_EN_REVISION = "Hallazgo en revisión";

    private final SolicitudDAO solicitudDAO;
    private final AuditoriaDAO auditoriaDAO;

    public RetroalimentacionServiceImpl() {
        this(new SolicitudDAOImpl(), new AuditoriaDAOImpl());
    }

    public RetroalimentacionServiceImpl(SolicitudDAO solicitudDAO) {
        this(solicitudDAO, new AuditoriaDAOImpl());
    }

    public RetroalimentacionServiceImpl(SolicitudDAO solicitudDAO, AuditoriaDAO auditoriaDAO) {
        this.solicitudDAO = solicitudDAO;
        this.auditoriaDAO = auditoriaDAO;
    }

    @Override
    public void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto) {
        if (dto == null || dto.getHallazgoId() == null || dto.getHallazgoId() <= 0) {
            throw new ExcepcionNegocio("El identificador del hallazgo no es válido.");
        }

        verificarEstadoDeRevisionDelHallazgo(dto.getHallazgoId());

        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion(dto);
        if (solicitud.getHallazgo() == null) {
            com.gr4.owlaudit.model.Hallazgo h = new com.gr4.owlaudit.model.Hallazgo();
            h.setId(dto.getHallazgoId());
            solicitud.setHallazgo(h);
        }
        solicitudDAO.crearSolicitudDeRetroalimentacion(solicitud);
    }

    @Override
    public void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }

        validarOrientacionRedactada(dto.getOrientacion());

        if (dto.getSolicitudId() == null || dto.getSolicitudId() <= 0) {
            throw new ExcepcionNegocio("Identificador de solicitud no válido.");
        }

        SolicitudRetroalimentacion solicitud = solicitudDAO.buscarPorId(dto.getSolicitudId());
        if (solicitud == null) {
            throw new ExcepcionNegocio("La solicitud de retroalimentación no existe.");
        }

        if (solicitud.getEstado() != EstadoRetroEnum.PENDIENTE) {
            throw new ExcepcionNegocio("La solicitud ya ha sido atendida.");
        }

        solicitud.setOrientacionTecnica(dto.getOrientacion().trim());
        solicitud.marcarSolicitudComoAtendida();
        solicitudDAO.actualizarSolicitud(solicitud);
    }

    /**
     * Extrae y ensambla la lista de hallazgos no conformes disponibles para solicitar retroalimentación.
     * (Método creado mediante refactorización Extract Method y trasladado mediante Move Method).
     *
     * @param proyectoId Identificador del proyecto para filtrar, o null si se consultan todas las auditorías.
     * @return Lista de mapas con la información representativa de cada hallazgo disponible.
     */
    @Override
    public List<Map<String, Object>> obtenerHallazgosDisponibles(Long proyectoId) {
        List<Auditoria> auditorias;
        if (proyectoId != null) {
            auditorias = auditoriaDAO.consultarHistorialDeAuditorias(proyectoId);
        } else {
            auditorias = auditoriaDAO.consultarTodas();
        }

        List<Map<String, Object>> hallazgosDisponibles = new ArrayList<>();
        if (auditorias != null) {
            for (Auditoria aud : auditorias) {
                if (aud.getResultados() != null) {
                    for (ResultadoRegla rr : aud.getResultados()) {
                        if (!rr.isCumple() && rr.getHallazgo() != null) {
                            Hallazgo h = rr.getHallazgo();
                            Map<String, Object> map = new HashMap<>();
                            map.put("hallazgoId", h.getId());
                            map.put("auditoriaId", aud.getId());
                            map.put("fechaHora", aud.getFechaHora() != null ? aud.getFechaHora().toString().replace('T', ' ') : "");
                            map.put("proyectoNombre", aud.getProyecto() != null ? aud.getProyecto().getNombre() : "Proyecto Académico");
                            map.put("reglaNombre", rr.getRegla() != null ? rr.getRegla().getNombreRepresentativo() : "Regla de Evaluación");
                            map.put("severidad", h.getNivelSeveridad() != null ? h.getNivelSeveridad().name() : "MEDIA");
                            map.put("evidencia", h.getEvidencia() != null ? h.getEvidencia() : "");
                            map.put("recomendacion", h.getRecomendacion() != null ? h.getRecomendacion() : "");

                            SolicitudRetroalimentacion sol = solicitudDAO.buscarPorHallazgoId(h.getId());
                            if (sol != null) {
                                map.put("tieneSolicitud", true);
                                map.put("estadoSolicitud", sol.getEstado() != null ? sol.getEstado().name() : "PENDIENTE");
                                map.put("solicitudId", sol.getId());
                            } else {
                                map.put("tieneSolicitud", false);
                                map.put("estadoSolicitud", "SIN_SOLICITUD");
                            }
                            hallazgosDisponibles.add(map);
                        }
                    }
                }
            }
        }
        return hallazgosDisponibles;
    }

    // ========================================================
    // MÉTODOS PRIVADOS DEL DIAGRAMA DE CLASES
    // ========================================================

    private void verificarEstadoDeRevisionDelHallazgo(Long hallazgoId) {
        SolicitudRetroalimentacion existente = solicitudDAO.buscarPorHallazgoId(hallazgoId);
        if (existente != null) {
            throw new ExcepcionNegocio(MENSAJE_HALLAZGO_EN_REVISION);
        }
    }

    private void validarOrientacionRedactada(String orientacion) {
        if (orientacion == null) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }
        String texto = orientacion.trim();
        if (texto.isEmpty() || texto.length() < 10 || texto.length() > 1000) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }
    }
}
