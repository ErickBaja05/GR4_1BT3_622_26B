package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.service.AsignacionesService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HU-02 · Crear asignaciones para auditorías específicas.
 * Boundary: Interfaz de Asignaciones / Control de Asignaciones.
 */
@WebServlet("/asignaciones")
public class AsignacionServlet extends HttpServlet {

    private static final String VISTA_CREAR = "/WEB-INF/views/crearAsignacion.jsp";

    private final AsignacionesService asignacionesService;
    private final ReglaDAO reglaDAO;

    public AsignacionServlet() {
        this(null, new ReglaDAOImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO) {
        this.asignacionesService = asignacionesService;
        this.reglaDAO = reglaDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
        } catch (Exception e) {
            System.err.println("[AsignacionServlet] Error al consultar reglas vigentes: " + e.getMessage());
        }
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String nombre = request.getParameter("nombre");
        String umbralStr = request.getParameter("umbralAprobacion");
        String[] reglasIdsParam = request.getParameterValues("reglasIds");

        Double umbral;
        try {
            if (umbralStr == null || umbralStr.trim().isEmpty()) {
                throw new NumberFormatException("El umbral no puede ser vacío");
            }
            umbral = Double.parseDouble(umbralStr.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "El umbral de aprobación debe ser un número válido.");
            request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
            return;
        }

        java.util.List<Long> idsReglas = new java.util.ArrayList<>();
        if (reglasIdsParam != null) {
            for (String idStr : reglasIdsParam) {
                try {
                    idsReglas.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        com.gr4.owlaudit.dto.NuevaAsignacionDTO dto = new com.gr4.owlaudit.dto.NuevaAsignacionDTO(nombre, umbral, idsReglas);
        boolean creada = asignacionesService != null && asignacionesService.crearAsignacion(dto);

        if (creada) {
            request.setAttribute("mensajeExito", "Asignación creada correctamente.");
        } else {
            request.setAttribute("mensajeError", "No se pudo crear la asignación.");
        }

        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }
}
