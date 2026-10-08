package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dao.SolicitudDAOImpl;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Hallazgo;
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

    public RetroalimentacionServlet() {
        this(new RetroalimentacionServiceImpl(), new SolicitudDAOImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService) {
        this(retroalimentacionService, new SolicitudDAOImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService, SolicitudDAO solicitudDAO) {
        this.retroalimentacionService = retroalimentacionService;
        this.solicitudDAO = solicitudDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);

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
        
        String hallazgoIdStr = request.getParameter("hallazgoId");
        String justificacion = request.getParameter("justificacion");
        Long hallazgoId = parseLongOrNull(hallazgoIdStr);

        // 1. Armado del DTO según diagrama de clases y secuencia5.puml
        SolicitudDTO dto = new SolicitudDTO(hallazgoId, justificacion != null ? justificacion.trim() : "");
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

        if (dto.getJustificacion() == null || dto.getJustificacion().length() < 10) {
            request.setAttribute("mensajeError", "Debe proporcionar una justificación técnica válida (mínimo 10 caracteres).");
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
            return;
        }

        // 2. Invocación del servicio según secuencia5.puml
        try {
            retroalimentacionService.solicitarRetroalimentacionDeHallazgo(dto);
            // 5. Notificar nueva solicitud creada
            request.setAttribute("mensajeExito", "Solicitud de retroalimentación enviada con éxito.");
        } catch (ExcepcionNegocio e) {
            // 6. Informar que el hallazgo está en revisión u otro error de negocio
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
                        if (rr.getAuditoria() != null && rr.getAuditoria().getProyecto() != null && request.getAttribute("nombreProyecto") == null) {
                            request.setAttribute("nombreProyecto", rr.getAuditoria().getProyecto().getNombre());
                        }
                    }
                    if (h.getNivelSeveridad() != null && request.getAttribute("severidad") == null) {
                        request.setAttribute("severidad", h.getNivelSeveridad().name());
                    }
                    if (h.getEvidencia() != null && request.getAttribute("evidencia") == null) {
                        request.setAttribute("evidencia", h.getEvidencia());
                    }
                }
            } catch (Exception e) {
                System.err.println("[RetroalimentacionServlet] Error al cargar hallazgo real " + hallazgoId + ": " + e.getMessage());
            }
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
                            if (rr.getAuditoria().getProyecto() != null) {
                                map.put("nombreProyecto", rr.getAuditoria().getProyecto().getNombre());
                            }
                        }
                    }
                }
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
