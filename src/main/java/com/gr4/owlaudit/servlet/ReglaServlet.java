package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.service.ReglaService;
import com.gr4.owlaudit.service.ReglaServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CU01 - Registrar regla de evaluación técnica (rol DOCENTE, validado por AutenticacionFilter).
 */
@WebServlet("/regla")
public class ReglaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/registrarRegla.jsp";

    private final ReglaService reglaService = new ReglaServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        // 1. Lectura de parámetros desde el formulario
        String tipoMotor = request.getParameter("tipoMotor");
        String nombreRepresentativo = request.getParameter("nombreRepresentativo");
        String descripcionDetallada = request.getParameter("descripcionDetallada");
        String parametroExacto = request.getParameter("parametroExacto");
        String nivelSeveridad = request.getParameter("nivelSeveridad");
        
        int ponderacion = 0;
        try {
            ponderacion = Integer.parseInt(request.getParameter("ponderacion"));
        } catch (NumberFormatException e) {
            ponderacion = 0;
        }

        // 2. Instanciación del DTO según clases.puml
        NuevaReglaDTO dto = new NuevaReglaDTO(
            tipoMotor, 
            nombreRepresentativo, 
            descripcionDetallada, 
            parametroExacto, 
            nivelSeveridad, 
            ponderacion
        );
        request.setAttribute("reglaDTO", dto);

        // 3. Delegación a la capa de servicio (validación de sintaxis, unicidad y persistencia)
        try {
            reglaService.registrarReglaEvaluacionTecnica(dto);
            request.setAttribute("mensajeExito", "Regla '" + dto.getNombreRepresentativo() + "' registrada con éxito y activa para las auditorías.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[ReglaServlet] Error técnico: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar la regla. Intente nuevamente.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}