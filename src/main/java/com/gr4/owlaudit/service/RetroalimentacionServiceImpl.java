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
    public synchronized void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto) {
        if (dto == null || dto.getHallazgoId() == null || dto.getHallazgoId() <= 0) {
            throw new ExcepcionNegocio("El identificador del hallazgo no es válido.");
        }

        verificarEstadoDeRevisionDelHallazgo(dto.getHallazgoId());

        if (dto.getUsuarioId() != null) {
            Hallazgo hallazgo = solicitudDAO.buscarHallazgoPorId(dto.getHallazgoId());
            if (hallazgo != null && hallazgo.getResultadoRegla() != null && hallazgo.getResultadoRegla().getAuditoria() != null) {
                Auditoria aud = hallazgo.getResultadoRegla().getAuditoria();
                if (aud.getProyecto() != null && !aud.getProyecto().esPropietario(dto.getUsuarioId())) {
                    throw new ExcepcionNegocio("No tiene permisos para solicitar retroalimentación sobre proyectos de otro estudiante.");
                }
            }
        }

        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion(dto);
        if (solicitud.getHallazgo() == null) {
            com.gr4.owlaudit.model.Hallazgo h = new com.gr4.owlaudit.model.Hallazgo();
            h.setId(dto.getHallazgoId());
            solicitud.setHallazgo(h);
        }
        if (solicitud.getUsuario() == null && dto.getUsuarioId() != null) {
            com.gr4.owlaudit.model.Usuario u = new com.gr4.owlaudit.model.Usuario();
            u.setId(dto.getUsuarioId());
            solicitud.setUsuario(u);
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
        return obtenerHallazgosDisponibles(proyectoId, null, null, null, null);
    }

    @Override
    public List<Map<String, Object>> obtenerHallazgosDisponibles(Long proyectoId, Long usuarioId) {
        return obtenerHallazgosDisponibles(proyectoId, usuarioId, null, null, null);
    }

    @Override
    public List<Map<String, Object>> obtenerHallazgosDisponibles(Long proyectoId, Long usuarioId, String tipoAuditoria, Long asignacionId, Long auditoriaId) {
        List<Auditoria> auditorias;

        if (auditoriaId != null) {
            Auditoria aud = auditoriaDAO.buscarPorId(auditoriaId);
            if (aud != null) {
                boolean valido = true;
                if (usuarioId != null && aud.getProyecto() != null && !aud.getProyecto().esPropietario(usuarioId)) {
                    valido = false;
                }
                if (proyectoId != null && aud.getProyecto() != null && !proyectoId.equals(aud.getProyecto().getId())) {
                    valido = false;
                }
                if ("GENERAL".equalsIgnoreCase(tipoAuditoria) && (aud.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(aud.getTipoAuditoria()))) {
                    valido = false;
                }
                if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) && aud.getAsignacion() == null && !"ASIGNACION".equalsIgnoreCase(aud.getTipoAuditoria())) {
                    valido = false;
                }
                if (asignacionId != null && (aud.getAsignacion() == null || !asignacionId.equals(aud.getAsignacion().getId()))) {
                    valido = false;
                }
                auditorias = valido ? new ArrayList<>(List.of(aud)) : new ArrayList<>();
            } else {
                auditorias = new ArrayList<>();
            }
        } else if (proyectoId != null) {
            if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                auditorias = auditoriaDAO.consultarHistorialAuditoriasGenerales(proyectoId);
            } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                if (asignacionId != null) {
                    auditorias = auditoriaDAO.consultarHistorialAuditoriasAsignacion(proyectoId, asignacionId);
                } else {
                    List<Auditoria> todasProy = auditoriaDAO.consultarHistorialDeAuditorias(proyectoId);
                    auditorias = todasProy != null ? todasProy.stream()
                        .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                        .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
                }
            } else {
                auditorias = auditoriaDAO.consultarHistorialDeAuditorias(proyectoId);
            }
        } else if (usuarioId != null) {
            List<Auditoria> delUsuario = auditoriaDAO.consultarHistorialDeAuditoriasPorUsuario(usuarioId);
            if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                auditorias = delUsuario != null ? delUsuario.stream()
                    .filter(a -> a.getAsignacion() == null && !"ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                    .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
            } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                if (asignacionId != null) {
                    auditorias = delUsuario != null ? delUsuario.stream()
                        .filter(a -> a.getAsignacion() != null && asignacionId.equals(a.getAsignacion().getId()))
                        .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
                } else {
                    auditorias = delUsuario != null ? delUsuario.stream()
                        .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                        .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
                }
            } else {
                auditorias = delUsuario != null ? delUsuario : new ArrayList<>();
            }
        } else {
            List<Auditoria> todas = auditoriaDAO.consultarTodas();
            if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                auditorias = todas != null ? todas.stream()
                    .filter(a -> a.getAsignacion() == null && !"ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                    .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
            } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                if (asignacionId != null) {
                    auditorias = todas != null ? todas.stream()
                        .filter(a -> a.getAsignacion() != null && asignacionId.equals(a.getAsignacion().getId()))
                        .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
                } else {
                    auditorias = todas != null ? todas.stream()
                        .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                        .collect(java.util.stream.Collectors.toList()) : new ArrayList<>();
                }
            } else {
                auditorias = todas != null ? todas : new ArrayList<>();
            }
        }

        List<Map<String, Object>> hallazgosDisponibles = new ArrayList<>();
        if (auditorias != null) {
            for (Auditoria aud : auditorias) {
                if (aud.getResultados() != null) {
                    for (ResultadoRegla rr : aud.getResultados()) {
                        if (!rr.isCumple() && rr.getHallazgo() != null) {
                            hallazgosDisponibles.add(extraerInfoHallazgo(rr.getHallazgo(), aud, rr));
                        }
                    }
                }
            }
        }
        return hallazgosDisponibles;
    }

    private Map<String, Object> extraerInfoHallazgo(Hallazgo h, Auditoria aud, ResultadoRegla rr) {
        Map<String, Object> map = new HashMap<>();
        map.put("hallazgoId", h.getId());
        map.put("auditoriaId", aud.getId());
        map.put("codigoVisible", aud.getCodigoVisible());
        map.put("tipoAuditoria", aud.getTipoAuditoria());
        if (aud.getAsignacion() != null) {
            map.put("asignacionId", aud.getAsignacion().getId());
            map.put("asignacionNombre", aud.getAsignacion().getNombre());
        }
        if (aud.getProyecto() != null) {
            map.put("proyectoId", aud.getProyecto().getId());
            map.put("proyectoNombre", aud.getProyecto().getNombre());
        } else {
            map.put("proyectoNombre", "Proyecto Académico");
        }
        map.put("fechaHora", aud.getFechaHora() != null ? aud.getFechaHora().toString().replace('T', ' ') : "");
        map.put("reglaNombre", rr.getRegla() != null ? rr.getRegla().getNombreRepresentativo() : "Regla de Evaluación");
        map.put("severidad", h.getNivelSeveridad() != null ? h.getNivelSeveridad().name() : "MEDIA");
        map.put("evidencia", h.getEvidencia() != null ? h.getEvidencia() : "");
        map.put("recomendacion", h.getRecomendacion() != null ? h.getRecomendacion() : "");

        adjuntarEstadoSolicitud(h.getId(), map);
        return map;
    }

    private void adjuntarEstadoSolicitud(Long hallazgoId, Map<String, Object> map) {
        SolicitudRetroalimentacion sol = solicitudDAO.buscarPorHallazgoId(hallazgoId);
        if (sol != null && sol.getEstado() == EstadoRetroEnum.PENDIENTE) {
            map.put("tieneSolicitud", true);
            map.put("estadoSolicitud", "PENDIENTE");
            map.put("solicitudId", sol.getId());
        } else if (sol != null && sol.getEstado() == EstadoRetroEnum.ATENDIDA) {
            map.put("tieneSolicitud", true);
            map.put("estadoSolicitud", "ATENDIDA");
            map.put("solicitudId", sol.getId());
        } else {
            map.put("tieneSolicitud", false);
            map.put("estadoSolicitud", "SIN_SOLICITUD");
        }
    }

    // ========================================================
    // MÉTODOS PRIVADOS DEL DIAGRAMA DE CLASES
    // ========================================================

    private void verificarEstadoDeRevisionDelHallazgo(Long hallazgoId) {
        SolicitudRetroalimentacion existente = solicitudDAO.buscarPorHallazgoId(hallazgoId);
        if (existente != null && existente.getEstado() == EstadoRetroEnum.PENDIENTE) {
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
