package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.service.EvolucionService;
import com.gr4.owlaudit.service.EvolucionServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CU04 - Consultar evolución de la calidad (roles ESTUDIANTE y DOCENTE).
 */
@WebServlet("/evolucion")
public class EvolucionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/consultarEvolucion.jsp";

    private final EvolucionService evolucionService;

    public EvolucionServlet() {
        this(new EvolucionServiceImpl());
    }

    public EvolucionServlet(EvolucionService evolucionService) {
        this.evolucionService = evolucionService;
    }

    // 1. Carga del Historial (doGet) según secuencia4.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String proyectoIdParam = request.getParameter("proyectoId");
        if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
            try {
                Long proyectoId = Long.parseLong(proyectoIdParam.trim());
                EvolucionDTO dto = new EvolucionDTO(proyectoId);
                request.setAttribute("evolucionDTO", dto);

                ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);
                request.setAttribute("resumenHistorialDTO", resumen);
            } catch (NumberFormatException e) {
                request.setAttribute("mensajeError", "El identificador del proyecto debe ser un número entero válido.");
            } catch (ExcepcionNegocio e) {
                request.setAttribute("mensajeError", e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("[EvolucionServlet] Error técnico al consultar historial: " + e.getMessage());
                request.setAttribute("mensajeError", "Ocurrió un error inesperado al consultar el historial de auditorías.");
            }
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // 2. Comparación de Auditorías (doPost) según secuencia4.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        if ("cancelar".equalsIgnoreCase(accion)) {
            request.setAttribute("mensajeInfo", "Consulta cancelada.");
            doGet(request, response);
            return;
        }

        String proyectoIdParam = request.getParameter("proyectoId");
        String baseIdParam = request.getParameter("baseId");
        String comparadaIdParam = request.getParameter("comparadaId");

        Long proyectoId = null;
        Long baseId = null;
        Long comparadaId = null;

        try {
            if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
                proyectoId = Long.parseLong(proyectoIdParam.trim());
            }
            if (baseIdParam != null && !baseIdParam.isBlank()) {
                baseId = Long.parseLong(baseIdParam.trim());
            }
            if (comparadaIdParam != null && !comparadaIdParam.isBlank()) {
                comparadaId = Long.parseLong(comparadaIdParam.trim());
            }

            EvolucionDTO dto = new EvolucionDTO(proyectoId, baseId, comparadaId);
            request.setAttribute("evolucionDTO", dto);

            EvolucionFinalDTO evolucionFinal = evolucionService.indicarDosAuditoriasAComparar(dto);
            request.setAttribute("evolucionFinalDTO", evolucionFinal);

            // Mantener el historial cargado para la vista
            if (proyectoId != null) {
                ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(new EvolucionDTO(proyectoId));
                request.setAttribute("resumenHistorialDTO", resumen);
            }

        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "Los identificadores ingresados no son válidos.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
            // Si hubo error de comparación pero el proyecto es válido, reintentar cargar el historial
            if (proyectoId != null) {
                try {
                    ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(new EvolucionDTO(proyectoId));
                    request.setAttribute("resumenHistorialDTO", resumen);
                } catch (Exception ignored) {
                }
            }
        } catch (RuntimeException e) {
            System.err.println("[EvolucionServlet] Error técnico al comparar auditorías: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al procesar la comparación.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
