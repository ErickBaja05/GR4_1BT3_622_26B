package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.DeudaTecnicaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.AuditoriaService;
import com.gr4.owlaudit.service.AuditoriaServiceImpl;
import com.gr4.owlaudit.service.CalculadoraDeudaTecnica;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.model.Proyecto;

/**
 * CU03 - Ejecutar auditoría de calidad del repositorio (roles ESTUDIANTE y DOCENTE).
 */
@WebServlet("/auditoria")
public class AuditoriaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/evaluarRepositorio.jsp";

    private final AuditoriaService auditoriaService;
    private final ProyectoDAO proyectoDAO;
    private final CalculadoraDeudaTecnica calculadoraDeudaTecnica;
    private final com.gr4.owlaudit.service.AsignacionesService asignacionesService;
    private final com.gr4.owlaudit.service.ValidadorAccesoCurso validadorAccesoCurso;

    public AuditoriaServlet() {
        this(new AuditoriaServiceImpl(), new ProyectoDAOImpl(), new CalculadoraDeudaTecnica(),
                new com.gr4.owlaudit.service.AsignacionesServiceImpl(), new com.gr4.owlaudit.service.ValidadorAccesoCurso(new com.gr4.owlaudit.dao.CursoDAOImpl()));
    }

    public AuditoriaServlet(AuditoriaService auditoriaService, ProyectoDAO proyectoDAO) {
        this(auditoriaService, proyectoDAO, new CalculadoraDeudaTecnica(), null, null);
    }

    public AuditoriaServlet(AuditoriaService auditoriaService, ProyectoDAO proyectoDAO,
                            CalculadoraDeudaTecnica calculadoraDeudaTecnica) {
        this(auditoriaService, proyectoDAO, calculadoraDeudaTecnica, null, null);
    }

    public AuditoriaServlet(AuditoriaService auditoriaService, ProyectoDAO proyectoDAO,
                            CalculadoraDeudaTecnica calculadoraDeudaTecnica,
                            com.gr4.owlaudit.service.AsignacionesService asignacionesService,
                            com.gr4.owlaudit.service.ValidadorAccesoCurso validadorAccesoCurso) {
        this.auditoriaService = auditoriaService;
        this.proyectoDAO = proyectoDAO;
        this.calculadoraDeudaTecnica = calculadoraDeudaTecnica;
        this.asignacionesService = asignacionesService;
        this.validadorAccesoCurso = validadorAccesoCurso;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idAsignacionParam = obtenerIdAsignacionParam(request);
        if (idAsignacionParam == null || idAsignacionParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
            return;
        }

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null)
                ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)
                : null;

        if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE && validadorAccesoCurso != null && asignacionesService != null) {
            try {
                Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
                com.gr4.owlaudit.model.Asignacion asignacion = asignacionesService.buscarPorId(idAsignacion);
                if (asignacion == null || asignacion.getCurso() == null ||
                        !validadorAccesoCurso.estaEstudianteMatriculado(asignacion.getCurso(), usuario.getId())) {
                    response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                    return;
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
                return;
            }
        }

        List<Proyecto> proyectos = (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE)
                ? proyectoDAO.listarPorUsuarioId(usuario.getId())
                : proyectoDAO.listarTodos();
        request.setAttribute("proyectos", proyectos);

        String proyectoIdParam = request.getParameter("proyectoId");
        String urlRepo = request.getParameter("url");
        Long proyectoId = null;

        if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
            try {
                proyectoId = Long.parseLong(proyectoIdParam.trim());
                if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE) {
                    final Long finalPid = proyectoId;
                    Proyecto miProyecto = null;
                    if (proyectos != null) {
                        for (Proyecto p : proyectos) {
                            if (finalPid.equals(p.getId()) || (p.getIdEstudiante() != null && finalPid.equals(p.getIdEstudiante()))) {
                                miProyecto = p;
                                break;
                            }
                        }
                    }
                    if (miProyecto == null) {
                        request.setAttribute("mensajeError", "No tiene permisos para auditar el proyecto seleccionado o no existe.");
                        request.getRequestDispatcher(VISTA).forward(request, response);
                        return;
                    }
                    proyectoId = miProyecto.getId();
                    urlRepo = miProyecto.getUrlGithub();
                } else if (urlRepo == null || urlRepo.isBlank()) {
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
            if (proyectoId == null) {
                for (Proyecto p : proyectos) {
                    if (p.getUrlGithub() != null && p.getUrlGithub().trim().equalsIgnoreCase(urlRepo.trim())) {
                        proyectoId = p.getId();
                        break;
                    }
                }
            }
            if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE && proyectoId == null) {
                request.setAttribute("mensajeError", "El repositorio no pertenece a sus proyectos registrados.");
                request.getRequestDispatcher(VISTA).forward(request, response);
                return;
            }

            AuditoriaDTO dto = new AuditoriaDTO(proyectoId, urlRepo.trim(), usuario != null ? usuario.getId() : null);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idAsignacionParam = obtenerIdAsignacionParam(request);
        if (idAsignacionParam == null || idAsignacionParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
            return;
        }

        jakarta.servlet.http.HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null)
                ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)
                : null;

        if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE && validadorAccesoCurso != null && asignacionesService != null) {
            try {
                Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
                com.gr4.owlaudit.model.Asignacion asignacion = asignacionesService.buscarPorId(idAsignacion);
                if (asignacion == null || asignacion.getCurso() == null ||
                        !validadorAccesoCurso.estaEstudianteMatriculado(asignacion.getCurso(), usuario.getId())) {
                    response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                    return;
                }
            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
                return;
            }
        }
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

        List<Proyecto> proyectos = (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE)
                ? proyectoDAO.listarPorUsuarioId(usuario.getId())
                : proyectoDAO.listarTodos();
        request.setAttribute("proyectos", proyectos);

        if (proyectoId == null && urlRepo != null) {
            for (Proyecto p : proyectos) {
                if (p.getUrlGithub() != null && p.getUrlGithub().trim().equalsIgnoreCase(urlRepo.trim())) {
                    proyectoId = p.getId();
                    break;
                }
            }
        }

        if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE) {
            final Long finalPid = proyectoId;
            Proyecto miProyecto = null;
            if (proyectos != null && finalPid != null) {
                for (Proyecto p : proyectos) {
                    if (finalPid.equals(p.getId()) || (p.getIdEstudiante() != null && finalPid.equals(p.getIdEstudiante()))) {
                        miProyecto = p;
                        break;
                    }
                }
            }
            if (miProyecto == null) {
                request.setAttribute("mensajeError", "No tiene permisos para auditar el proyecto seleccionado o no existe.");
                request.getRequestDispatcher(VISTA).forward(request, response);
                return;
            }
            proyectoId = miProyecto.getId();
            urlRepo = miProyecto.getUrlGithub();
        }

        AuditoriaDTO dto = new AuditoriaDTO(proyectoId, urlRepo == null ? null : urlRepo.trim(), usuario != null ? usuario.getId() : null);
        String idAsignacionStr = obtenerIdAsignacionParam(request);
        if (idAsignacionStr != null && !idAsignacionStr.isBlank()) {
            try {
                dto.setIdAsignacion(Long.parseLong(idAsignacionStr.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        request.setAttribute("auditoriaDTO", dto);

        try {
            ResultadoDTO resultadoDTO = auditoriaService.ejecutarAuditoriaDeAsignacion(dto);
            request.setAttribute("resultadoDTO", resultadoDTO);
            request.setAttribute("mensajeExito", "Auditoría ejecutada y registrada en el historial correctamente.");

            // Deuda T�cnica Estimada: solo considera las reglas incumplidas (hallazgos reales)
            DeudaTecnicaDTO deudaTecnica = calculadoraDeudaTecnica.calcularDesdeResultado(resultadoDTO);
            request.setAttribute("deudaTecnica", deudaTecnica);

        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[AuditoriaServlet] Error técnico en ejecución: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al ejecutar la auditoría. Intente nuevamente.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Refactoring: Replace Temp with Query (Martin Fowler)
     */
    private String obtenerIdAsignacionParam(HttpServletRequest request) {
        return request.getParameter("idAsignacion");
    }
}
