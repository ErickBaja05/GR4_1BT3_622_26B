package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/auditoria")
public class AuditoriaServlet extends HttpServlet {

    // 1. Previa de Auditoría (doGet) según secuencia3.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String urlRepo = request.getParameter("url");
        if (urlRepo != null && !urlRepo.isBlank()) {
            AuditoriaDTO dto = new AuditoriaDTO(urlRepo);

            // Simulación del ResumenDTO devuelto por el servicio
            ResumenDTO resumenDTO = new ResumenDTO(100);

            System.out.println("====== [FRONTEND LOG: AuditoriaServlet - Previa] ======");
            System.out.println("Solicitando previa para: " + dto.getUrl());
            System.out.println("Puntaje Máximo Posible: " + resumenDTO.getPuntajeMaximo());
            System.out.println("=======================================================");

            request.setAttribute("auditoriaDTO", dto);
            request.setAttribute("resumenDTO", resumenDTO);
        }

        request.getRequestDispatcher("/WEB-INF/views/evaluarRepositorio.jsp").forward(request, response);
    }

    // 2. Ejecución Definitiva de Auditoría (doPost) según secuencia3.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String urlRepo = request.getParameter("urlGithub");

        AuditoriaDTO dto = new AuditoriaDTO(urlRepo);

        // Simulación del ResultadoDTO devuelto por el servicio
        ResultadoDTO resultadoDTO = new ResultadoDTO(85, 100, 85.0);

        System.out.println("====== [FRONTEND LOG: AuditoriaServlet - Ejecución] ======");
        System.out.println("Confirmando ejecución de auditoría.");
        System.out.println("AuditoriaDTO: " + dto.getUrl());
        System.out.println("ResultadoDTO Generado: " + resultadoDTO.getPuntajeObtenido() + "/" + resultadoDTO.getPuntajeMaximo() + " (" + resultadoDTO.getPorcentaje() + "%)");
        System.out.println("==========================================================");

        request.setAttribute("mensajeExito", "Auditoría ejecutada correctamente en la capa de presentación.");
        request.setAttribute("auditoriaDTO", dto);
        request.setAttribute("resultadoDTO", resultadoDTO);

        request.getRequestDispatcher("/WEB-INF/views/evaluarRepositorio.jsp").forward(request, response);
    }
}