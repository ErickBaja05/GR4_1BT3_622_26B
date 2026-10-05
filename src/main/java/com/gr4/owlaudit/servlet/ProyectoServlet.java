package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
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
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[ProyectoServlet] Error técnico: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar el proyecto. Intente nuevamente.");
        }

        // 4. Redirección a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}