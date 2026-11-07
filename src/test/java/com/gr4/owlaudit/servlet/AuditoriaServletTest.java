package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.AuditoriaService;
import com.gr4.owlaudit.service.ValidadorAccesoCurso;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AuditoriaServletTest {

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private AsignacionesService asignacionesService;

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private AuditoriaServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new AuditoriaServlet(auditoriaService, proyectoDAO, null,
                asignacionesService, new ValidadorAccesoCurso(cursoDAO));
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
        when(request.getContextPath()).thenReturn("/Owl_Audit_App");
        when(request.getSession(false)).thenReturn(session);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    @DisplayName("doGet (HU-03): Sin idAsignacion es bloqueado y redirigido a /curso?error=sinAsignacion")
    void given_sinIdAsignacion_when_doGet_then_redirigeACursoConErrorSinAsignacion() throws Exception {
        when(request.getParameter("idAsignacion")).thenReturn(null);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/Owl_Audit_App/curso?error=sinAsignacion");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("doGet (HU-03): Estudiante no matriculado en el curso de la asignación es bloqueado y redirigido a /curso?error=permisos")
    void given_estudianteNoMatriculado_when_doGet_then_redirigeACursoConErrorPermisos() throws Exception {
        UsuarioDTO usuarioSofia = new UsuarioDTO(99L, "Sofía Torres", "sofia@epn.edu.ec", RolEnum.ESTUDIANTE);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(usuarioSofia);
        when(request.getParameter("idAsignacion")).thenReturn("10");

        Curso cursoArquitectura = new Curso();
        cursoArquitectura.setId(2L);
        Asignacion asignacion = new Asignacion();
        asignacion.setId(10L);
        asignacion.setCurso(cursoArquitectura);

        when(asignacionesService.buscarPorId(10L)).thenReturn(asignacion);
        when(cursoDAO.estaEstudianteMatriculado(2L, 99L)).thenReturn(false);

        servlet.doGet(request, response);

        verify(response).sendRedirect("/Owl_Audit_App/curso?error=permisos");
        verify(dispatcher, never()).forward(request, response);
    }

    @Test
    @DisplayName("doPost (HU-03): Estudiante no matriculado es bloqueado y redirigido a /curso?error=permisos en POST")
    void given_estudianteNoMatriculado_when_doPost_then_redirigeACursoConErrorPermisos() throws Exception {
        UsuarioDTO usuarioSofia = new UsuarioDTO(99L, "Sofía Torres", "sofia@epn.edu.ec", RolEnum.ESTUDIANTE);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(usuarioSofia);
        when(request.getParameter("idAsignacion")).thenReturn("10");

        Curso cursoArquitectura = new Curso();
        cursoArquitectura.setId(2L);
        Asignacion asignacion = new Asignacion();
        asignacion.setId(10L);
        asignacion.setCurso(cursoArquitectura);

        when(asignacionesService.buscarPorId(10L)).thenReturn(asignacion);
        when(cursoDAO.estaEstudianteMatriculado(2L, 99L)).thenReturn(false);

        servlet.doPost(request, response);

        verify(response).sendRedirect("/Owl_Audit_App/curso?error=permisos");
        verify(dispatcher, never()).forward(request, response);
    }
}
