package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.CursoService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EP-01 >> FT-02 >> HU-01: cuando ReglaServlet bloquea a un docente que no es titular del curso
 * (redirección a /curso?error=permisos), el mensaje mostrado debe corresponder al rol del usuario.
 */
@ExtendWith(MockitoExtension.class)
class CursoServletMensajePermisosTest {

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

    private CursoServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new CursoServlet(cursoService);
        when(request.getSession(false)).thenReturn(session);
        when(request.getParameter("error")).thenReturn("permisos");
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    private void simularUsuarioEnSesion(RolEnum rol) {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setId(7L);
        usuario.setRol(rol);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(usuario);
    }

    @Test
    @DisplayName("Given docente no titular redirigido con error=permisos, When carga Mis Cursos, Then muestra mensaje de titularidad del curso")
    void givenDocenteConErrorPermisos_whenDoGet_thenMuestraMensajeDeTitularidad() throws Exception {
        simularUsuarioEnSesion(RolEnum.DOCENTE);

        servlet.doGet(request, response);

        verify(request).setAttribute("mensajeError",
                "No tiene permisos para gestionar un curso del que no es docente titular.");
    }

    @Test
    @DisplayName("Given estudiante redirigido con error=permisos, When carga Mis Cursos, Then conserva el mensaje de matricula")
    void givenEstudianteConErrorPermisos_whenDoGet_thenMuestraMensajeDeMatricula() throws Exception {
        simularUsuarioEnSesion(RolEnum.ESTUDIANTE);

        servlet.doGet(request, response);

        verify(request).setAttribute("mensajeError",
                "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado.");
    }
}
