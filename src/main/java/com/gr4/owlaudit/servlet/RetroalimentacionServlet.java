package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.RolEnum;
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

    public RetroalimentacionServlet() {
        this(new RetroalimentacionServiceImpl());
    }

    public RetroalimentacionServlet(RetroalimentacionService retroalimentacionService) {
        this.retroalimentacionService = retroalimentacionService;
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
        if (hallazgoId == null) {
            hallazgoId = 202L; // Hallazgo demo: Restricción de ejecutables
        }

        SolicitudDTO dtoActual = (SolicitudDTO) request.getAttribute("solicitudDTO");
        if (dtoActual == null) {
            SolicitudDTO demo = new SolicitudDTO();
            demo.setHallazgoId(hallazgoId);
            request.setAttribute("solicitudDTO", demo);
        }

        if (request.getAttribute("nombreProyecto") == null) {
            request.setAttribute("nombreProyecto", "Sistema de Gestión Académica - GR4");
        }
        if (request.getAttribute("reglaNombre") == null) {
            request.setAttribute("reglaNombre", "Restricción de ejecutables y binarios (.exe, .jar)");
        }
        if (request.getAttribute("severidad") == null) {
            request.setAttribute("severidad", "ALTA");
        }
        if (request.getAttribute("evidencia") == null) {
            request.setAttribute("evidencia", "Se detectó el archivo 'dist/app.jar' en el repositorio.");
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

        List<Map<String, Object>> pendientes = new ArrayList<>();

        Map<String, Object> s1 = new HashMap<>();
        s1.put("solicitudId", 501L);
        s1.put("hallazgoId", 202L);
        s1.put("nombreProyecto", "Sistema de Gestión Académica - GR4");
        s1.put("reglaNombre", "Restricción de ejecutables y binarios (.exe, .jar)");
        s1.put("severidad", "ALTA");
        s1.put("evidencia", "Archivo 'dist/app.jar' en el repositorio.");
        s1.put("justificacion", "El archivo JAR detectado corresponde a una dependencia requerida para ejecutar la base de datos embebida en pruebas locales.");
        s1.put("estado", "PENDIENTE");
        s1.put("fechaHora", "2026-10-02 11:20:00");

        Map<String, Object> s2 = new HashMap<>();
        s2.put("solicitudId", 502L);
        s2.put("hallazgoId", 203L);
        s2.put("nombreProyecto", "Portal de Reservas - GR2");
        s2.put("reglaNombre", "Estructura estándar de carpetas /src/main/java");
        s2.put("severidad", "MEDIA");
        s2.put("evidencia", "No existe la ruta 'src/main/java'.");
        s2.put("justificacion", "Se movieron los paquetes bajo 'src/main/kotlin' debido a una migración parcial y no se detectó el paquete java estándar.");
        s2.put("estado", "PENDIENTE");
        s2.put("fechaHora", "2026-10-03 09:45:00");

        pendientes.add(s1);
        pendientes.add(s2);

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
