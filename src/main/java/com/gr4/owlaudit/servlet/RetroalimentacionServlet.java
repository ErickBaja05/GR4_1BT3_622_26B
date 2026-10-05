package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.RolEnum;

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

        System.out.println("------------------------------------------------------------------");
        System.out.println("[RetroalimentacionServlet - GET] Navegación: accion=" + accion 
                + ", usuario=" + (usuario != null ? usuario.getCorreo() : "anónimo"));
        System.out.println("------------------------------------------------------------------");

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
        if ("solicitar".equalsIgnoreCase(accion) || request.getParameter("justificacion") != null && request.getParameter("orientacion") == null) {
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

        // 1. Armado del DTO según diagrama de clases
        SolicitudDTO dto = new SolicitudDTO();
        dto.setHallazgoId(hallazgoId);
        dto.setJustificacion(justificacion != null ? justificacion.trim() : "");
        dto.setReglaNombre(request.getParameter("reglaNombre"));
        dto.setSeveridad(request.getParameter("severidad"));

        request.setAttribute("solicitudDTO", dto);

        // Trazabilidad por consola para pruebas rápidas
        System.out.println("==================================================================");
        System.out.println("[RetroalimentacionServlet - POST] CU05: Solicitar Retroalimentación");
        System.out.println("  -> DTO armado: " + dto);
        System.out.println("  -> Hallazgo ID: " + dto.getHallazgoId());
        System.out.println("  -> Justificación técnica: " + dto.getJustificacion());
        System.out.println("==================================================================");

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

        // Simulación de control de hallazgo en revisión (ejemplo: ID 999 ya registrado)
        if (Long.valueOf(999L).equals(dto.getHallazgoId())) {
            System.out.println("[RetroalimentacionServlet] Hallazgo ya en revisión: ID=" + dto.getHallazgoId());
            request.setAttribute("mensajeError", "El hallazgo ya se encuentra en proceso de revisión.");
            prepararVistaSolicitar(request);
            request.getRequestDispatcher(VISTA_SOLICITAR).forward(request, response);
            return;
        }

        // Caso de éxito
        System.out.println("[RetroalimentacionServlet] Solicitud creada con éxito y notificada al docente.");
        request.setAttribute("mensajeExito", "Solicitud de retroalimentación enviada con éxito.");
        
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

        // 1. Armado del DTO según diagrama de clases
        SolicitudDTO dto = new SolicitudDTO();
        dto.setSolicitudId(solicitudId);
        dto.setOrientacion(orientacion != null ? orientacion.trim() : "");
        dto.setReglaNombre(request.getParameter("reglaNombre"));
        dto.setJustificacion(request.getParameter("justificacion"));

        request.setAttribute("solicitudDTO", dto);

        // Trazabilidad por consola para pruebas rápidas
        System.out.println("==================================================================");
        System.out.println("[RetroalimentacionServlet - POST] CU06: Atender Retroalimentación (Docente)");
        System.out.println("  -> DTO armado: " + dto);
        System.out.println("  -> Solicitud ID: " + dto.getSolicitudId());
        System.out.println("  -> Orientación técnica redactada: " + dto.getOrientacion());
        System.out.println("==================================================================");

        // Validaciones según el caso de uso cu06-atenderFeedback.md
        if (dto.getSolicitudId() == null || dto.getSolicitudId() <= 0) {
            request.setAttribute("mensajeError", "Debe seleccionar una solicitud de retroalimentación válida.");
            prepararVistaAtender(request);
            request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
            return;
        }

        if (dto.getOrientacion() == null || dto.getOrientacion().isBlank() || dto.getOrientacion().length() < 10) {
            request.setAttribute("mensajeError", "Error: La respuesta técnica no puede estar vacía y debe contener al menos 10 caracteres.");
            prepararVistaAtender(request);
            request.getRequestDispatcher(VISTA_ATENDER).forward(request, response);
            return;
        }

        // Caso de éxito: persistencia simulada y cambio de estado a ATENDIDA
        System.out.println("[RetroalimentacionServlet] Retroalimentación ID=" + dto.getSolicitudId() + " atendida exitosamente.");
        request.setAttribute("mensajeExito", "Retroalimentación atendida exitosamente.");

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
            demo.setNombreProyecto("Sistema de Gestión Académica - GR4");
            demo.setReglaNombre("Restricción de ejecutables y binarios (.exe, .jar)");
            demo.setSeveridad("ALTA");
            demo.setEvidencia("Se detectó el archivo 'dist/app.jar' en el repositorio.");
            request.setAttribute("solicitudDTO", demo);
        }
    }

    private void prepararVistaAtender(HttpServletRequest request) {
        List<SolicitudDTO> pendientes = new ArrayList<>();

        SolicitudDTO s1 = new SolicitudDTO(202L, 501L, 
                "El archivo JAR detectado corresponde a una dependencia requerida para ejecutar la base de datos embebida en pruebas locales.", 
                null);
        s1.setNombreProyecto("Sistema de Gestión Académica - GR4");
        s1.setReglaNombre("Restricción de ejecutables y binarios (.exe, .jar)");
        s1.setSeveridad("ALTA");
        s1.setEvidencia("Archivo 'dist/app.jar' en el repositorio.");
        s1.setEstado("PENDIENTE");
        s1.setFechaHora("2026-10-02 11:20:00");

        SolicitudDTO s2 = new SolicitudDTO(203L, 502L, 
                "Se movieron los paquetes bajo 'src/main/kotlin' debido a una migración parcial y no se detectó el paquete java estándar.", 
                null);
        s2.setNombreProyecto("Portal de Reservas - GR2");
        s2.setReglaNombre("Estructura estándar de carpetas /src/main/java");
        s2.setSeveridad("MEDIA");
        s2.setEvidencia("No existe la ruta 'src/main/java'.");
        s2.setEstado("PENDIENTE");
        s2.setFechaHora("2026-10-03 09:45:00");

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
