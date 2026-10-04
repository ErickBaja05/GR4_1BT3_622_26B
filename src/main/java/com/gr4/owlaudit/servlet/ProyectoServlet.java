package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dto.NuevoProyectoDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/proyecto")
public class ProyectoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirige a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher("/WEB-INF/views/registrarProyecto.jsp").forward(request, response);
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

        // 3. Log por consola para verificar captura de datos en el Frontend
        System.out.println("====== [FRONTEND LOG: ProyectoServlet] ======");
        System.out.println("Petición POST capturada correctamente.");
        System.out.println("DTO Instanciado: " + dto.toString());
        System.out.println("=============================================");

        // 4. Atributos enviables de regreso a la vista JSP
        request.setAttribute("mensajeExito", "Proyecto '" + dto.getNombre() + "' capturado correctamente en la capa de presentación.");
        request.setAttribute("proyectoDTO", dto);

        // 5. Redirección a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher("/WEB-INF/views/registrarProyecto.jsp").forward(request, response);
    }
}