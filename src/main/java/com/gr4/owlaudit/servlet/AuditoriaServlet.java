package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.service.AuditoriaService;
import com.gr4.owlaudit.service.AuditoriaServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.model.Proyecto;
import java.util.List;

/**
 * CU03 - Ejecutar auditoría de calidad del repositorio (roles ESTUDIANTE y DOCENTE).
 */
@WebServlet("/auditoria")
public class AuditoriaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/evaluarRepositorio.jsp";

    private final AuditoriaService auditoriaService;
    private final ProyectoDAO proyectoDAO;

    public AuditoriaServlet() {
        this(new AuditoriaServiceImpl(), new ProyectoDAOImpl());
    }

    public AuditoriaServlet(AuditoriaService auditoriaService, ProyectoDAO proyectoDAO) {
        this.auditoriaService = auditoriaService;
        this.proyectoDAO = proyectoDAO;
    }

    // 1. Previa de Auditoría (doGet) según secuencia3.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        List<Proyecto> proyectos = proyectoDAO.listarTodos();
        request.setAttribute("proyectos", proyectos);

        String proyectoIdParam = request.getParameter("proyectoId");
        String urlRepo = request.getParameter("url");
        Long proyectoId = null;

        if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
            try {
                proyectoId = Long.parseLong(proyectoIdParam.trim());
                if (urlRepo == null || urlRepo.isBlank()) {
                    for (Proyecto p : proyectos) {
                        if (p.getId().equals(proyectoId)) {
                            urlRepo = p.getUrlGithub();
                            break;
                        }
                    }
                    if (urlRepo == null) {
                        Proyecto p = proyectoDAO.buscarPorId(proyectoId);
                        if (p != null) {
                            urlRepo = p.getUrlGithub();
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (urlRepo != null && !urlRepo.isBlank()) {
            // Si vino URL pero no proyectoId, intentar inferirlo de la lista de proyectos
            if (proyectoId == null) {
                for (Proyecto p : proyectos) {
                    if (p.getUrlGithub() != null && p.getUrlGithub().trim().equalsIgnoreCase(urlRepo.trim())) {
                        proyectoId = p.getId();
                        break;
                    }
                }
            }

            AuditoriaDTO dto = new AuditoriaDTO(proyectoId, urlRepo.trim());
            request.setAttribute("auditoriaDTO", dto);

            try {
                ResumenDTO resumenDTO = auditoriaService.solicitarPreviaDeAuditoria(dto);
                request.setAttribute("resumenDTO", resumenDTO);
            } catch (ExcepcionNegocio e) {
                request.setAttribute("mensajeError", e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("[AuditoriaServlet] Error técnico en previa: " + e.getMessage());
                request.setAttribute("mensajeError", "Ocurrió un error inesperado al preparar la auditoría. Intente nuevamente.");
            }
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // 2. Ejecución Definitiva de Auditoría (doPost) según secuencia3.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String urlRepo = request.getParameter("urlGithub");
        String proyectoIdParam = request.getParameter("proyectoId");
        Long proyectoId = null;

        if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
            try {
                proyectoId = Long.parseLong(proyectoIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        List<Proyecto> proyectos = proyectoDAO.listarTodos();
        request.setAttribute("proyectos", proyectos);

        if (proyectoId == null && urlRepo != null) {
            for (Proyecto p : proyectos) {
                if (p.getUrlGithub() != null && p.getUrlGithub().trim().equalsIgnoreCase(urlRepo.trim())) {
                    proyectoId = p.getId();
                    break;
                }
            }
        }

        AuditoriaDTO dto = new AuditoriaDTO(proyectoId, urlRepo == null ? null : urlRepo.trim());
        request.setAttribute("auditoriaDTO", dto);

        try {
            ResultadoDTO resultadoDTO = auditoriaService.confirmarEjecucionDeAuditoria(dto);
            request.setAttribute("resultadoDTO", resultadoDTO);
            request.setAttribute("mensajeExito", "Auditoría ejecutada y registrada en el historial correctamente.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[AuditoriaServlet] Error técnico en ejecución: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al ejecutar la auditoría. Intente nuevamente.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}