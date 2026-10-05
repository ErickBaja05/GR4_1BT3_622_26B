package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.SolicitudDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Retroalimentación de hallazgos. Se publica en dos rutas para que cada caso de uso
 * conserve su propio control de rol en OpcionMenu:
 *  - /solicitarFeedback : CU05 - Solicitar retroalimentación de hallazgo (ESTUDIANTE), secuencia5.puml.
 *  - /atenderFeedback   : CU06 - Atender retroalimentación (DOCENTE), secuencia6.puml.
 */
@WebServlet({RetroalimentacionServlet.RUTA_SOLICITAR, RetroalimentacionServlet.RUTA_ATENDER})
public class RetroalimentacionServlet extends HttpServlet {

    static final String RUTA_SOLICITAR = "/solicitarFeedback";
    static final String RUTA_ATENDER = "/atenderFeedback";

    private static final String VISTA_SOLICITAR = "/WEB-INF/views/solicitarRetroalimentacion.jsp";
    private static final String VISTA_ATENDER = "/WEB-INF/views/atenderRetroalimentacion.jsp";

    // TODO: private final RetroalimentacionService retroalimentacionService = new RetroalimentacionServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher(vistaPara(request)).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (RUTA_ATENDER.equals(request.getServletPath())) {
            atenderSolicitud(request);
        } else {
            solicitarRetroalimentacion(request);
        }

        request.getRequestDispatcher(vistaPara(request)).forward(request, response);
    }

    // CU05 según secuencia5.puml
    private void solicitarRetroalimentacion(HttpServletRequest request) {
        SolicitudDTO dto = new SolicitudDTO(
            parsearId(request.getParameter("hallazgoId")),
            null,
            recortar(request.getParameter("justificacion")),
            null
        );
        request.setAttribute("solicitudDTO", dto);
        System.out.println("[RetroalimentacionServlet] doPost CU05 -> " + dto);

        try {
            // TODO: integrar cuando exista RetroalimentacionService
            // retroalimentacionService.solicitarRetroalimentacionDeHallazgo(dto);
            request.setAttribute("mensajeExito", "Solicitud de retroalimentación enviada con éxito");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[RetroalimentacionServlet] Error técnico en CU05: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al enviar la solicitud. Intente nuevamente.");
        }
    }

    // CU06 según secuencia6.puml
    private void atenderSolicitud(HttpServletRequest request) {
        SolicitudDTO dto = new SolicitudDTO(
            null,
            parsearId(request.getParameter("solicitudId")),
            null,
            recortar(request.getParameter("orientacion"))
        );
        request.setAttribute("solicitudDTO", dto);
        System.out.println("[RetroalimentacionServlet] doPost CU06 -> " + dto);

        try {
            // TODO: integrar cuando exista RetroalimentacionService
            // retroalimentacionService.registrarOrientacionTecnicaEnLaSolicitud(dto);
            request.setAttribute("mensajeExito", "Retroalimentación atendida exitosamente");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[RetroalimentacionServlet] Error técnico en CU06: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar la orientación. Intente nuevamente.");
        }
    }

    private static String vistaPara(HttpServletRequest request) {
        return RUTA_ATENDER.equals(request.getServletPath()) ? VISTA_ATENDER : VISTA_SOLICITAR;
    }

    private static String recortar(String valor) {
        return valor == null ? null : valor.trim();
    }

    /** Convierte un identificador recibido del formulario; null si no es un entero positivo. */
    private static Long parsearId(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            long id = Long.parseLong(valor.trim());
            return id > 0 ? id : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
