package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dao.SolicitudDAOImpl;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import com.gr4.owlaudit.service.RetroalimentacionService;
import com.gr4.owlaudit.service.RetroalimentacionServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet unificado para los casos de uso de retroalimentación:
 *  - CU05: Solicitar retroalimentación de hallazgo (rol ESTUDIANTE) - secuencia5.puml
 *  - CU06: Registrar orientación técnica / Atender retroalimentación (rol DOCENTE) - secuencia6.puml
 *
 * Basado en diagramaClasesIncremento2.puml.
 */
@WebServlet("/retroalimentacion")
public class RetroalimentacionServlet extends HttpServlet {

    private static final String VISTA_SOLICITAR = "/WEB-INF/views/solicitarRetroalimentacion.jsp";
    private static final String VISTA_ATENDER = "/WEB-INF/views/atenderRetroalimentacion.jsp";

    private final RetroalimentacionService retroalimentacionService;
    private final SolicitudDAO solicitudDAO;
    private final ProyectoDAO proyectoDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final com.gr4.owlaudit.service.AsignacionesService asignacionesService;

    public RetroalimentacionServlet() {
        this(new RetroalimentacionServiceImpl(), new SolicitudDAOImpl(), new ProyectoDAOImpl(), new AuditoriaDAOImpl(), new com.gr4.owlaudit.service.AsignacionesServiceImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService) {
        this(retroalimentacionService, new SolicitudDAOImpl(), new ProyectoDAOImpl(), new AuditoriaDAOImpl(), new com.gr4.owlaudit.service.AsignacionesServiceImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService, SolicitudDAO solicitudDAO) {
        this(retroalimentacionService, solicitudDAO, new ProyectoDAOImpl(), new AuditoriaDAOImpl(), new com.gr4.owlaudit.service.AsignacionesServiceImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService, SolicitudDAO solicitudDAO,
                                   ProyectoDAO proyectoDAO, AuditoriaDAO auditoriaDAO) {
        this(retroalimentacionService, solicitudDAO, proyectoDAO, auditoriaDAO, new com.gr4.owlaudit.service.AsignacionesServiceImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService, SolicitudDAO solicitudDAO,
                                   ProyectoDAO proyectoDAO, AuditoriaDAO auditoriaDAO,
                                   com.gr4.owlaudit.service.AsignacionesService asignacionesService) {
        this.retroalimentacionService = retroalimentacionService;
        this.solicitudDAO = solicitudDAO;
        this.proyectoDAO = proyectoDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.asignacionesService = asignacionesService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);

        if (request.getParameter("cancelado") != null) {
            request.setAttribute("mensajeInfo", "Solicitud cancelada");
        }

        String accion = request.getParameter("accion");
        if (accion == null || accion.isBlank()) {
            // Decidir por rol si no se especifica explícitamente en la URL
            if (usuario != null && usuario.getRol() == RolEnum.DOCENTE) {
                accion = "atender";
            } else {
                accion = "solicitar";
            }
        }

        if ("atender".equalsIgnoreCase(accion)) {
            prepararVistaAtender(request);
            request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
        } else {
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        // CU05: Solicitar retroalimentación (Estudiante)
        if ("solicitar".equalsIgnoreCase(accion) || (request.getParameter("justificacion") != null && request.getParameter("orientacion") == null)) {
            procesarSolicitudEstudiante(request, response);
            return;
        }

        // CU06: Atender retroalimentación (Docente)
        if ("atender".equalsIgnoreCase(accion) || request.getParameter("orientacion") != null) {
            procesarAtencionDocente(request, response);
            return;
        }

        // Por defecto
        response.sendRedirect(request.getContextPath() + "/vistas");
    }

    /**
     * Procesa la solicitud enviada por un estudiante (CU05 - secuencia5.puml).
     */
    private void procesarSolicitudEstudiante(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Si el estudiante decide cancelar la solicitud (Escenario Alternativo 3)
        if (request.getParameter("cancelar") != null) {
            request.setAttribute("mensajeInfo", "Solicitud cancelada");
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
            return;
        }

        String hallazgoIdStr = request.getParameter("hallazgoId");
        String justificacion = request.getParameter("justificacion");
        Long hallazgoId = parseLongOrNull(hallazgoIdStr);

        // 1. Armado del DTO según diagrama de clases y secuencia5.puml
        UsuarioDTO usuarioSesion = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        SolicitudDTO dto = new SolicitudDTO(hallazgoId, justificacion != null ? justificacion.trim() : "");
        if (usuarioSesion != null) {
            dto.setUsuarioId(usuarioSesion.getId());
            dto.setNombreEstudiante(usuarioSesion.getNombre());
        }
        request.setAttribute("solicitudDTO", dto);

        // Preservar atributos de contexto para la vista
        if (request.getParameter("reglaNombre") != null) {
            request.setAttribute("reglaNombre", request.getParameter("reglaNombre"));
        }
        if (request.getParameter("severidad") != null) {
            request.setAttribute("severidad", request.getParameter("severidad"));
        }

        // Validaciones según el caso de uso solicitarRetroalimentacionHallazgo.md
        if (dto.getHallazgoId() == null || dto.getHallazgoId() <= 0) {
            request.setAttribute("mensajeError", "Debe indicar un hallazgo válido sobre el cual consultar.");
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
            return;
        }

        // Escenario Alternativo 2: longitud requerida
        if (dto.getJustificacion() == null || dto.getJustificacion().isBlank() 
                || dto.getJustificacion().length() < 10 || dto.getJustificacion().length() > 1000) {
            request.setAttribute("mensajeError", "Debe proporcionar una justificación técnica válida");
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
            return;
        }

        // 2. Invocación del servicio según secuencia5.puml
        try {
            retroalimentacionService.solicitarRetroalimentacionDeHallazgo(dto);
            // 5. Notificar nueva solicitud creada (Escenario Básico paso 9)
            request.setAttribute("mensajeExito", "Solicitud de retroalimentación enviada con éxito.");
            request.removeAttribute("solicitudDTO");
        } catch (ExcepcionNegocio e) {
            // 6. Informar que el hallazgo está en revisión u otro error de negocio (Escenario Alternativo 1)
            if ("Hallazgo en revisión".equalsIgnoreCase(e.getMessage())) {
                request.setAttribute("mensajeError", "El hallazgo ya se encuentra en proceso de revisión.");
            } else {
                request.setAttribute("mensajeError", e.getMessage());
            }
        } catch (RuntimeException e) {
            System.err.println("[RetroalimentacionServlet] Error técnico al solicitar retroalimentación: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al enviar la solicitud.");
        }

        prepararVistaSolicitar(request);
        request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
    }

    /**
     * Procesa la respuesta de orientación técnica enviada por un docente (CU06 - secuencia6.puml).
     */
    private void procesarAtencionDocente(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String solicitudIdStr = request.getParameter("solicitudId");
        String orientacion = request.getParameter("orientacion");
        Long solicitudId = parseLongOrNull(solicitudIdStr);

        // 1. Armado del DTO según diagrama de clases y secuencia6.puml
        SolicitudDTO dto = new SolicitudDTO(solicitudId, orientacion != null ? orientacion.trim() : "", true);
        request.setAttribute("solicitudDTO", dto);

        // Validaciones según el caso de uso cu06-atenderFeedback.md
        if (dto.getSolicitudId() == null || dto.getSolicitudId() <= 0) {
            request.setAttribute("mensajeError", "Debe seleccionar una solicitud de retroalimentación válida.");
            prepararVistaAtender(request);
            request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
            return;
        }

        if (dto.getOrientacion() == null || dto.getOrientacion().isBlank() || dto.getOrientacion().length() < 10) {
            request.setAttribute("mensajeError", "Orientación en blanco o inválida (mínimo 10 caracteres).");
            prepararVistaAtender(request);
            request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
            return;
        }

        // 2. Invocación del servicio según secuencia6.puml
        try {
            retroalimentacionService.registrarOrientacionTecnicaEnLaSolicitud(dto);
            // 6. Notificar orientación técnica exitosa
            request.setAttribute("mensajeExito", "Retroalimentación atendida exitosamente.");
        } catch (ExcepcionNegocio e) {
            // 7. Informar error en la redacción u otro error de negocio
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[RetroalimentacionServlet] Error técnico al atender retroalimentación: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar la orientación técnica.");
        }

        prepararVistaAtender(request);
        request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
    }

    private void prepararVistaSolicitar(HttpServletRequest request) {
        String hallazgoIdParam = request.getParameter("hallazgoId");
        Long hallazgoId = parseLongOrNull(hallazgoIdParam);

        SolicitudDTO dtoActual = (SolicitudDTO) request.getAttribute("solicitudDTO");
        if (dtoActual != null && dtoActual.getHallazgoId() != null) {
            hallazgoId = dtoActual.getHallazgoId();
        }

        if (hallazgoId != null) {
            if (dtoActual == null) {
                SolicitudDTO nuevo = new SolicitudDTO();
                nuevo.setHallazgoId(hallazgoId);
                request.setAttribute("solicitudDTO", nuevo);
            }

            try {
                Hallazgo h = solicitudDAO.buscarHallazgoPorId(hallazgoId);
                if (h != null) {
                    if (h.getResultadoRegla() != null) {
                        ResultadoRegla rr = h.getResultadoRegla();
                        if (rr.getRegla() != null && request.getAttribute("reglaNombre") == null) {
                            request.setAttribute("reglaNombre", rr.getRegla().getNombreRepresentativo());
                        }
                        if (rr.getAuditoria() != null) {
                            com.gr4.owlaudit.model.Auditoria a = rr.getAuditoria();
                            if (a.getId() != null && request.getAttribute("auditoriaId") == null) {
                                request.setAttribute("auditoriaId", a.getId());
                            }
                            if (request.getAttribute("codigoVisibleAuditoria") == null) {
                                request.setAttribute("codigoVisibleAuditoria", a.getCodigoVisible());
                            }
                            if (request.getAttribute("tipoAuditoria") == null) {
                                request.setAttribute("tipoAuditoria", a.getTipoAuditoria());
                            }
                            if (a.getAsignacion() != null && request.getAttribute("nombreAsignacion") == null) {
                                request.setAttribute("nombreAsignacion", a.getAsignacion().getNombre());
                            }
                            if (a.getFechaHora() != null && request.getAttribute("fechaAuditoria") == null) {
                                request.setAttribute("fechaAuditoria", a.getFechaHora().toString().replace('T', ' '));
                            }
                            if (a.getProyecto() != null) {
                                if (request.getAttribute("nombreProyecto") == null) {
                                    request.setAttribute("nombreProyecto", a.getProyecto().getNombre());
                                }
                                if (request.getAttribute("urlGithub") == null && a.getProyecto().getUrlGithub() != null) {
                                    request.setAttribute("urlGithub", a.getProyecto().getUrlGithub());
                                }
                            }
                        }
                    }
                    if (h.getNivelSeveridad() != null && request.getAttribute("severidad") == null) {
                        request.setAttribute("severidad", h.getNivelSeveridad().name());
                    }
                    if (h.getEvidencia() != null && request.getAttribute("evidencia") == null) {
                        request.setAttribute("evidencia", h.getEvidencia());
                    }
                    if (h.getRecomendacion() != null && request.getAttribute("recomendacion") == null) {
                        request.setAttribute("recomendacion", h.getRecomendacion());
                    }
                }
                SolicitudRetroalimentacion existente = solicitudDAO.buscarPorHallazgoId(hallazgoId);
                if (existente != null) {
                    request.setAttribute("solicitudExistente", existente);
                }
            } catch (Exception e) {
                System.err.println("[RetroalimentacionServlet] Error al cargar hallazgo real " + hallazgoId + ": " + e.getMessage());
            }
        }

        // Cargar proyectos y auditorías para selección (Paso 1 y 2 CU05)
        try {
            UsuarioDTO usuarioSesion = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
            Long usuarioId = (usuarioSesion != null && usuarioSesion.getRol() == RolEnum.ESTUDIANTE) ? usuarioSesion.getId() : null;
            List<Proyecto> listaProyectos = (usuarioId != null)
                    ? proyectoDAO.listarPorUsuarioId(usuarioId)
                    : proyectoDAO.listarTodos();
            request.setAttribute("proyectosDisponibles", listaProyectos);

            String proyectoIdStr = request.getParameter("proyectoId");
            Long proyectoId = parseLongOrNull(proyectoIdStr);
            if (usuarioId != null && proyectoId != null) {
                final Long finalPid = proyectoId;
                boolean pertenece = listaProyectos != null && listaProyectos.stream().anyMatch(p -> p.getId().equals(finalPid));
                if (!pertenece) {
                    proyectoId = null;
                }
            }
            request.setAttribute("proyectoIdFiltro", proyectoId);

            String tipoAuditoria = request.getParameter("tipoAuditoria");
            if (tipoAuditoria != null) {
                tipoAuditoria = tipoAuditoria.trim();
                if (tipoAuditoria.isBlank()) tipoAuditoria = null;
            }
            request.setAttribute("tipoAuditoriaFiltro", tipoAuditoria);

            String asignacionIdStr = request.getParameter("asignacionId");
            Long asignacionId = parseLongOrNull(asignacionIdStr);
            request.setAttribute("asignacionIdFiltro", asignacionId);

            String auditoriaIdStr = request.getParameter("auditoriaId");
            Long auditoriaIdFiltro = parseLongOrNull(auditoriaIdStr);
            request.setAttribute("auditoriaIdFiltro", auditoriaIdFiltro);

            if (asignacionesService != null) {
                request.setAttribute("asignacionesDisponibles", asignacionesService.listarAsignaciones());
            }

            // Cargar auditorías disponibles para el selector de auditoría específica
            List<com.gr4.owlaudit.model.Auditoria> auditoriasCandidatas;
            if (proyectoId != null) {
                if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                    auditoriasCandidatas = auditoriaDAO.consultarHistorialAuditoriasGenerales(proyectoId);
                } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                    if (asignacionId != null) {
                        auditoriasCandidatas = auditoriaDAO.consultarHistorialAuditoriasAsignacion(proyectoId, asignacionId);
                    } else {
                        List<com.gr4.owlaudit.model.Auditoria> delProy = auditoriaDAO.consultarHistorialDeAuditorias(proyectoId);
                        auditoriasCandidatas = delProy != null ? delProy.stream()
                            .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                            .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                    }
                } else {
                    auditoriasCandidatas = auditoriaDAO.consultarHistorialDeAuditorias(proyectoId);
                }
            } else if (usuarioId != null) {
                List<com.gr4.owlaudit.model.Auditoria> delUsuario = auditoriaDAO.consultarHistorialDeAuditoriasPorUsuario(usuarioId);
                if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                    auditoriasCandidatas = delUsuario != null ? delUsuario.stream()
                        .filter(a -> a.getAsignacion() == null && !"ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                        .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                    if (asignacionId != null) {
                        auditoriasCandidatas = delUsuario != null ? delUsuario.stream()
                            .filter(a -> a.getAsignacion() != null && asignacionId.equals(a.getAsignacion().getId()))
                            .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                    } else {
                        auditoriasCandidatas = delUsuario != null ? delUsuario.stream()
                            .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                            .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                    }
                } else {
                    auditoriasCandidatas = delUsuario != null ? delUsuario : new java.util.ArrayList<>();
                }
            } else {
                List<com.gr4.owlaudit.model.Auditoria> todas = auditoriaDAO.consultarTodas();
                if ("GENERAL".equalsIgnoreCase(tipoAuditoria)) {
                    auditoriasCandidatas = todas != null ? todas.stream()
                        .filter(a -> a.getAsignacion() == null && !"ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                        .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                } else if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) || asignacionId != null) {
                    if (asignacionId != null) {
                        auditoriasCandidatas = todas != null ? todas.stream()
                            .filter(a -> a.getAsignacion() != null && asignacionId.equals(a.getAsignacion().getId()))
                            .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                    } else {
                        auditoriasCandidatas = todas != null ? todas.stream()
                            .filter(a -> a.getAsignacion() != null || "ASIGNACION".equalsIgnoreCase(a.getTipoAuditoria()))
                            .collect(java.util.stream.Collectors.toList()) : new java.util.ArrayList<>();
                    }
                } else {
                    auditoriasCandidatas = todas != null ? todas : new java.util.ArrayList<>();
                }
            }
            request.setAttribute("auditoriasDisponibles", auditoriasCandidatas);

            // Invocación al método del servicio filtrado con los parámetros ampliados
            List<Map<String, Object>> hallazgosDisponibles = retroalimentacionService.obtenerHallazgosDisponibles(
                    proyectoId, usuarioId, tipoAuditoria, asignacionId, auditoriaIdFiltro);
            request.setAttribute("hallazgosDisponibles", hallazgosDisponibles);

            // Cargar solicitudes previas para consulta del estudiante (cierre del corte vertical)
            List<SolicitudRetroalimentacion> misSolicitudes = (usuarioId != null)
                    ? solicitudDAO.consultarSolicitudesPorUsuarioId(usuarioId)
                    : solicitudDAO.consultarTodasLasSolicitudes();
            request.setAttribute("misSolicitudes", misSolicitudes);

        } catch (Exception e) {
            System.err.println("[RetroalimentacionServlet] Error al preparar datos de selección para CU05: " + e.getMessage());
        }
    }


    private void prepararVistaAtender(HttpServletRequest request) {
        String solicitudIdParam = request.getParameter("solicitudId");
        Long solicitudIdSeleccionada = parseLongOrNull(solicitudIdParam);

        SolicitudDTO dtoActual = (SolicitudDTO) request.getAttribute("solicitudDTO");
        if (dtoActual == null && solicitudIdSeleccionada != null) {
            SolicitudDTO seleccionado = new SolicitudDTO();
            seleccionado.setSolicitudId(solicitudIdSeleccionada);
            request.setAttribute("solicitudDTO", seleccionado);
        }

        List<SolicitudRetroalimentacion> lista = null;
        try {
            lista = solicitudDAO.consultarSolicitudesPendientes();
        } catch (Exception e) {
            System.err.println("[RetroalimentacionServlet] Error al consultar pendientes: " + e.getMessage());
        }

        List<Map<String, Object>> pendientes = new ArrayList<>();
        if (lista != null) {
            for (SolicitudRetroalimentacion s : lista) {
                Map<String, Object> map = new HashMap<>();
                map.put("solicitudId", s.getId());
                map.put("justificacion", s.getJustificacion() != null ? s.getJustificacion() : "");
                map.put("estado", s.getEstado() != null ? s.getEstado().name() : "PENDIENTE");
                map.put("fechaHora", "");

                String nombreEstudiante = null;
                String urlGithub = "";
                if (s.getUsuario() != null && s.getUsuario().getNombre() != null) {
                    nombreEstudiante = s.getUsuario().getNombre();
                }

                if (s.getHallazgo() != null) {
                    map.put("hallazgoId", s.getHallazgo().getId());
                    map.put("severidad", s.getHallazgo().getNivelSeveridad() != null ? s.getHallazgo().getNivelSeveridad().name() : "MEDIA");
                    map.put("evidencia", s.getHallazgo().getEvidencia() != null ? s.getHallazgo().getEvidencia() : "");

                    if (s.getHallazgo().getResultadoRegla() != null) {
                        ResultadoRegla rr = s.getHallazgo().getResultadoRegla();
                        if (rr.getRegla() != null) {
                            map.put("reglaNombre", rr.getRegla().getNombreRepresentativo());
                        }
                        if (rr.getAuditoria() != null) {
                            if (rr.getAuditoria().getFechaHora() != null) {
                                map.put("fechaHora", rr.getAuditoria().getFechaHora().toString());
                            }
                            map.put("codigoVisible", rr.getAuditoria().getCodigoVisible());
                            map.put("tipoAuditoria", rr.getAuditoria().getTipoAuditoria());
                            if (rr.getAuditoria().getAsignacion() != null) {
                                map.put("nombreAsignacion", rr.getAuditoria().getAsignacion().getNombre());
                            }
                            if (nombreEstudiante == null && rr.getAuditoria().getUsuario() != null) {
                                nombreEstudiante = rr.getAuditoria().getUsuario().getNombre();
                            }
                            if (rr.getAuditoria().getProyecto() != null) {
                                map.put("nombreProyecto", rr.getAuditoria().getProyecto().getNombre());
                                if (rr.getAuditoria().getProyecto().getUrlGithub() != null) {
                                    urlGithub = rr.getAuditoria().getProyecto().getUrlGithub();
                                }
                                if (nombreEstudiante == null && rr.getAuditoria().getProyecto().getUsuario() != null) {
                                    nombreEstudiante = rr.getAuditoria().getProyecto().getUsuario().getNombre();
                                }
                            }
                        }
                    }
                }
                map.put("nombreEstudiante", (nombreEstudiante != null && !nombreEstudiante.isBlank()) ? nombreEstudiante : "Estudiante");
                map.put("urlGithub", urlGithub);
                if (map.get("nombreProyecto") == null) {
                    map.put("nombreProyecto", "Proyecto Académico");
                }
                if (map.get("reglaNombre") == null) {
                    map.put("reglaNombre", "Regla de Evaluación");
                }
                if (map.get("severidad") == null) {
                    map.put("severidad", "MEDIA");
                }
                if (map.get("evidencia") == null) {
                    map.put("evidencia", "—");
                }
                pendientes.add(map);
            }
        }

        request.setAttribute("solicitudesPendientes", pendientes);
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
