package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dto.NuevaReglaDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/regla")
public class ReglaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/registrarRegla.jsp").forward(request, response);
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

        // 3. Impresión por consola para depuración
        System.out.println("====== [FRONTEND LOG: ReglaServlet] ======");
        System.out.println("Petición POST capturada correctamente.");
        System.out.println("DTO Instanciado: " + dto.toString());
        System.out.println("==========================================");

        // 4. Atributos para la vista
        request.setAttribute("mensajeExito", "Regla '" + dto.getNombreRepresentativo() + "' registrada con éxito en la capa de presentación.");
        request.setAttribute("reglaDTO", dto);

        request.getRequestDispatcher("/WEB-INF/views/registrarRegla.jsp").forward(request, response);
    }
}