package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.CursoService;
import com.gr4.owlaudit.service.CursoServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet("/curso")
public class CursoServlet extends HttpServlet {

    private static final String VISTA_MIS_CURSOS = "/WEB-INF/views/misCursos.jsp";

    private final CursoService cursoService;

    public CursoServlet() {
        this(new CursoServiceImpl());
    }

    public CursoServlet(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null)
                ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)
                : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String errorParam = request.getParameter("error");
        if ("sinAsignacion".equalsIgnoreCase(errorParam)) {
            request.setAttribute("mensajeError", "Debe seleccionar una asignación dentro de sus cursos matriculados para ejecutar una auditoría.");
        } else if ("permisos".equalsIgnoreCase(errorParam)) {
            request.setAttribute("mensajeError", "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado.");
        } else if ("sinCurso".equalsIgnoreCase(errorParam)) {
            request.setAttribute("mensajeError", "Debe ingresar a las asignaciones a través de uno de sus cursos matriculados.");
        }

        List<Curso> cursos;
        if (usuario.getRol() == RolEnum.ESTUDIANTE) {
            cursos = cursoService.listarCursosPorEstudiante(usuario.getId());
        } else if (usuario.getRol() == RolEnum.DOCENTE) {
            cursos = cursoService.listarCursosPorDocente(usuario.getId());
        } else {
            cursos = Collections.emptyList();
        }

        request.setAttribute("cursos", cursos);
        request.getRequestDispatcher(VISTA_MIS_CURSOS).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}

