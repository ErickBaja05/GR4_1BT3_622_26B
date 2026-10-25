package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
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
        // En Fase RED: sin implementación
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // En Fase RED: sin implementación
    }
}
