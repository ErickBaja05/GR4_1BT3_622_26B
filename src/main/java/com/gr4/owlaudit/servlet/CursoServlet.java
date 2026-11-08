package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
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
    static final String MENSAJE_PERMISOS_DOCENTE = "No tiene permisos para gestionar un curso del que no es docente titular.";
    static final String MENSAJE_PERMISOS_ESTUDIANTE = "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado.";

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
            request.setAttribute("mensajeError",
                    "Debe seleccionar una asignación dentro de sus cursos matriculados para ejecutar una auditoría.");
        } else if ("permisos".equalsIgnoreCase(errorParam)) {
            request.setAttribute("mensajeError", esDocente(usuario)
                    ? MENSAJE_PERMISOS_DOCENTE
                    : MENSAJE_PERMISOS_ESTUDIANTE);
        } else if ("sinCurso".equalsIgnoreCase(errorParam)) {
            request.setAttribute("mensajeError", esDocente(usuario)
                    ? "Debe seleccionar uno de sus cursos para gestionar sus reglas o asignaciones."
                    : "Debe ingresar a las asignaciones a través de uno de sus cursos matriculados.");
        }

        List<Curso> cursos;
        if (usuario.getRol() == RolEnum.ESTUDIANTE) {
            cursos = (usuario.getId() != null)
                    ? cursoService.listarCursosPorEstudiante(usuario.getId())
                    : Collections.emptyList();
            if ((cursos == null || cursos.isEmpty()) && usuario.getCorreo() != null) {
                List<Curso> cursosPorCorreo = cursoService.listarCursosPorCorreoEstudiante(usuario.getCorreo());
                if (cursosPorCorreo != null && !cursosPorCorreo.isEmpty()) {
                    cursos = cursosPorCorreo;
                }
            }
        } else if (usuario.getRol() == RolEnum.DOCENTE) {
            cursos = (usuario.getId() != null)
                    ? cursoService.listarCursosPorDocente(usuario.getId())
                    : cursoService.listarCursosPorCorreoDocente(usuario.getCorreo());
        } else {
            cursos = Collections.emptyList();
        }

        request.setAttribute("cursos", cursos);
        request.getRequestDispatcher(VISTA_MIS_CURSOS).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioDTO usuario = obtenerUsuarioAutenticado(request);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (esEstudiante(usuario)) {
            procesarMatriculaEstudiante(request, response, usuario);
            return;
        }
        if (!esDocente(usuario)) {
            response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
            return;
        }

        try {
            Curso cursoCreado = cursoService.crearCurso(extraerNuevoCursoDTO(request), usuario.getCorreo());
            request.setAttribute(
                    "mensajeExito",
                    "Curso creado exitosamente, comparta el código: " + cursoCreado.getCodigo()
                            + " con sus estudiantes para que se matriculen");
        } catch (ExcepcionNegocio e) {
            preservarDatosFormularioConError(request, e.getMessage());
        }

        cargarVistaCursosDocente(request, response, usuario.getCorreo());
    }

    private boolean esEstudiante(UsuarioDTO usuario) {
        return usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE;
    }

    private void procesarMatriculaEstudiante(HttpServletRequest request, HttpServletResponse response,
            UsuarioDTO estudiante) throws ServletException, IOException {
        String codigo = request.getParameter("codigo");
        try {
            Curso cursoMatriculado = cursoService.matricularEstudiantePorCodigo(estudiante.getId(), codigo);
            request.setAttribute("mensajeExito",
                    "Matriculación exitosa en el curso: " + cursoMatriculado.getNombre());
        } catch (ExcepcionNegocio e) {
            request.setAttribute("error", e.getMessage());
        }
        List<Curso> cursos = (estudiante.getId() != null)
                ? cursoService.listarCursosPorEstudiante(estudiante.getId())
                : Collections.emptyList();
        if ((cursos == null || cursos.isEmpty()) && estudiante.getCorreo() != null) {
            List<Curso> cursosPorCorreo = cursoService.listarCursosPorCorreoEstudiante(estudiante.getCorreo());
            if (cursosPorCorreo != null && !cursosPorCorreo.isEmpty()) {
                cursos = cursosPorCorreo;
            }
        }
        request.setAttribute("cursos", cursos);
        request.getRequestDispatcher(VISTA_MIS_CURSOS).forward(request, response);
    }

    private UsuarioDTO obtenerUsuarioAutenticado(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        return (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);
    }

    private boolean esDocente(UsuarioDTO usuario) {
        return usuario != null && usuario.getRol() == RolEnum.DOCENTE;
    }

    private NuevoCursoDTO extraerNuevoCursoDTO(HttpServletRequest request) {
        return new NuevoCursoDTO(
                request.getParameter("nombre"),
                request.getParameter("codigo"),
                request.getParameter("descripcion"));
    }

    private void preservarDatosFormularioConError(HttpServletRequest request, String mensajeError) {
        request.setAttribute("error", mensajeError);
        request.setAttribute("nombreIngresado", request.getParameter("nombre"));
        request.setAttribute("codigoIngresado", request.getParameter("codigo"));
        request.setAttribute("descripcionIngresada", request.getParameter("descripcion"));
    }

    private void cargarVistaCursosDocente(HttpServletRequest request, HttpServletResponse response,
            String correoDocente)
            throws ServletException, IOException {
        request.setAttribute("cursos", cursoService.listarCursosPorCorreoDocente(correoDocente));
        request.getRequestDispatcher(VISTA_MIS_CURSOS).forward(request, response);
    }
}
