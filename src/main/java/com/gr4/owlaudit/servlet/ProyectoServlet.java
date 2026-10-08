package com.gr4.owlaudit.servlet;

import java.io.IOException;

import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.service.ProyectoService;
import com.gr4.owlaudit.service.ProyectoServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CU02 - Registrar proyecto académico (rol ESTUDIANTE, validado por AutenticacionFilter).
 */
@WebServlet("/proyecto")
public class ProyectoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/registrarProyecto.jsp";

    private final ProyectoService proyectoService = new ProyectoServiceImpl();
    private final ProyectoDAO proyectoDAO = new ProyectoDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            List<Proyecto> proyectos = proyectoDAO.listarTodos();
            request.setAttribute("proyectos", proyectos);
        } catch (Exception e) {
            System.err.println("[ProyectoServlet] Error al listar proyectos: " + e.getMessage());
        }
        // Redirige a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configura la codificación de caracteres para acentos y caracteres especiales
        request.setCharacterEncoding("UTF-8");

        // 1. Obtención de parámetros del formulario HTML
        String nombre = request.getParameter("nombre");
        String urlGithub = request.getParameter("urlGithub");

        // 2. Instanciación del DTO según el Diagrama de Clases
        NuevoProyectoDTO dto = new NuevoProyectoDTO(nombre, urlGithub);
        request.setAttribute("proyectoDTO", dto);

        // 3. Delegación a la capa de servicio (validación del enlace + persistencia)
        try {
            proyectoService.solicitarRegistroDeNuevoProyecto(dto);
            request.setAttribute("mensajeExito", "Proyecto '" + dto.getNombre() + "' registrado correctamente.");

            List<Proyecto> proyectos = proyectoDAO.listarTodos();
            request.setAttribute("proyectos", proyectos);
            if (proyectos != null && urlGithub != null) {
                Proyecto proyectoRegistrado = proyectos.stream()
                        .filter(p -> urlGithub.trim().equalsIgnoreCase(p.getUrlGithub() != null ? p.getUrlGithub().trim() : ""))
                        .findFirst()
                        .orElse(null);
                request.setAttribute("proyectoRegistrado", proyectoRegistrado);
            }
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
            try {
                request.setAttribute("proyectos", proyectoDAO.listarTodos());
            } catch (Exception ignored) {}
        } catch (RuntimeException e) {
            System.err.println("[ProyectoServlet] Error técnico: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar el proyecto. Intente nuevamente.");
            try {
                request.setAttribute("proyectos", proyectoDAO.listarTodos());
            } catch (Exception ignored) {}
        }

        // 4. Redirección a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}