package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.EvolucionDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CU04 - Consultar evolución de calidad (roles ESTUDIANTE y DOCENTE) según secuencia4.puml.
 *  - doGet  : carga del historial de auditorías del proyecto.
 *  - doPost : comparación de dos auditorías del historial.
 */
@WebServlet("/evolucion")
public class EvolucionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/consultarEvolucion.jsp";

    // TODO: private final EvolucionService evolucionService = new EvolucionServiceImpl();

    // 1. Carga del Historial (doGet) según secuencia4.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String proyectoParam = request.getParameter("proyectoId");
        if (proyectoParam != null && !proyectoParam.isBlank()) {
            EvolucionDTO dto = new EvolucionDTO(parsearId(proyectoParam));
            request.setAttribute("evolucionDTO", dto);
            System.out.println("[EvolucionServlet] doGet -> " + dto);

            try {
                if (dto.getProyectoId() == null) {
                    throw new ExcepcionNegocio("El identificador del proyecto debe ser un número entero positivo.");
                }
                // TODO: integrar cuando exista EvolucionService
                // ResumenHistorialDTO resumenHistorialDTO = evolucionService.solicitarEvolucionDeCalidad(dto);
                // request.setAttribute("resumenHistorialDTO", resumenHistorialDTO);
            } catch (ExcepcionNegocio e) {
                request.setAttribute("mensajeError", e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("[EvolucionServlet] Error técnico en historial: " + e.getMessage());
                request.setAttribute("mensajeError", "Ocurrió un error inesperado al consultar el historial. Intente nuevamente.");
            }
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // 2. Comparación de Auditorías (doPost) según secuencia4.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Las dos auditorías seleccionadas; el servicio decide cuál es la base (la más antigua)
        String[] seleccionadas = request.getParameterValues("auditoriaId");
        Long baseId = null;
        Long comparadaId = null;
        if (seleccionadas != null && seleccionadas.length == 2) {
            baseId = parsearId(seleccionadas[0]);
            comparadaId = parsearId(seleccionadas[1]);
        }

        EvolucionDTO dto = new EvolucionDTO(parsearId(request.getParameter("proyectoId")), baseId, comparadaId);
        request.setAttribute("evolucionDTO", dto);
        System.out.println("[EvolucionServlet] doPost -> " + dto);

        try {
            if (dto.getBaseId() == null || dto.getComparadaId() == null) {
                throw new ExcepcionNegocio("Consulta cancelada");
            }
            // TODO: integrar cuando exista EvolucionService
            // request.setAttribute("resumenHistorialDTO", evolucionService.solicitarEvolucionDeCalidad(dto));
            // EvolucionFinalDTO evolucionFinalDTO = evolucionService.indicarDosAuditoriasAComparar(dto);
            // request.setAttribute("evolucionFinalDTO", evolucionFinalDTO);
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[EvolucionServlet] Error técnico en comparación: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al comparar las auditorías. Intente nuevamente.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
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
