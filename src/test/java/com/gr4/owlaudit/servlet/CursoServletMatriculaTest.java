package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.CursoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CursoServletMatriculaTest {

    @Mock
    private CursoService cursoService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private CursoServlet cursoServlet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cursoServlet = new CursoServlet(cursoService);
        when(request.getSession(false)).thenReturn(session);
        when(request.getRequestDispatcher("/WEB-INF/views/misCursos.jsp")).thenReturn(dispatcher);
    }

    @Test
    void deberiaMatricularEstudianteYMostrarMensajeExitoEnPost() throws Exception {
        UsuarioDTO estudiante = new UsuarioDTO(10L, "Erick Bajaña", "erick@epn.edu.ec", RolEnum.ESTUDIANTE);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(estudiante);
        when(request.getParameter("codigo")).thenReturn("CSW-2026B");

        Curso curso = new Curso();
        curso.setId(1L);
        curso.setCodigo("CSW-2026B");
        curso.setNombre("Construcción de Software");

        when(cursoService.matricularEstudiantePorCodigo(10L, "CSW-2026B")).thenReturn(curso);
        when(cursoService.listarCursosPorEstudiante(10L)).thenReturn(List.of(curso));

        cursoServlet.doPost(request, response);

        verify(request).setAttribute("mensajeExito",
                "Matriculación exitosa en el curso: Construcción de Software");
        verify(request).setAttribute("cursos", List.of(curso));
        verify(dispatcher).forward(request, response);
    }

    @Test
    void deberiaMostrarMensajeErrorCuandoMatriculaEstudianteFallaPorNegocio() throws Exception {
        UsuarioDTO estudiante = new UsuarioDTO(10L, "Erick Bajaña", "erick@epn.edu.ec", RolEnum.ESTUDIANTE);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(estudiante);
        when(request.getParameter("codigo")).thenReturn("XXX-9999");

        when(cursoService.matricularEstudiantePorCodigo(10L, "XXX-9999"))
                .thenThrow(new ExcepcionNegocio("No existe ningún curso registrado con el código ingresado"));
        when(cursoService.listarCursosPorEstudiante(10L)).thenReturn(List.of());

        cursoServlet.doPost(request, response);

        verify(request).setAttribute("error",
                "No existe ningún curso registrado con el código ingresado");
        verify(request).setAttribute("cursos", List.of());
        verify(dispatcher).forward(request, response);
    }
}
