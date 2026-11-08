package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServletTest {

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

    private RolEnum rolEnum;

    @BeforeEach
    void setUp() {
        cursoServlet = new CursoServlet(cursoService);
    }

    @Test
    @DisplayName("CA1 - Escenario 1.1: POST exitoso registra el curso y muestra mensaje con el código único")
    void doPost_cuandoDocenteEnviaDatosValidos_creaCursoYMuestraMensajeExito() throws Exception {
        UsuarioDTO docenteDto = new UsuarioDTO(1L, "Dr. Xavier", "docente@epn.edu.ec", rolEnum.DOCENTE);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuario")).thenReturn(docenteDto);
        when(request.getParameter("nombre")).thenReturn("Construcción de Software");
        when(request.getParameter("codigo")).thenReturn("CSW-2026B");
        when(request.getParameter("descripcion")).thenReturn("Curso de ingeniería de software grupo 4");
        when(request.getRequestDispatcher("/WEB-INF/views/misCursos.jsp")).thenReturn(dispatcher);

        Curso cursoCreado = new Curso();
        cursoCreado.setNombre("Construcción de Software");
        cursoCreado.setCodigo("CSW-2026B");
        cursoCreado.setDescripcion("Curso de ingeniería de software grupo 4");

        when(cursoService.crearCurso(any(NuevoCursoDTO.class), eq("docente@epn.edu.ec"))).thenReturn(cursoCreado);
        when(cursoService.listarCursosPorCorreoDocente("docente@epn.edu.ec")).thenReturn(List.of(cursoCreado));

        cursoServlet.doPost(request, response);

        ArgumentCaptor<NuevoCursoDTO> dtoCaptor = ArgumentCaptor.forClass(NuevoCursoDTO.class);
        verify(cursoService).crearCurso(dtoCaptor.capture(), eq("docente@epn.edu.ec"));
        assertEquals("CSW-2026B", dtoCaptor.getValue().getCodigo());

        verify(request).setAttribute(
                "mensajeExito",
                "Curso creado exitosamente, comparta el código: CSW-2026B con sus estudiantes para que se matriculen");
        verify(request).setAttribute("cursos", List.of(cursoCreado));
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("CA2 y CA3: POST con ExcepcionNegocio captura el error y reenvía a la vista manteniendo listado")
    void doPost_cuandoServicioLanzaExcepcionNegocio_seteaAtributoErrorYHaceForward() throws Exception {
        UsuarioDTO docenteDto = new UsuarioDTO(1L, "Dr. Xavier", "docente@epn.edu.ec", rolEnum.DOCENTE);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuario")).thenReturn(docenteDto);
        when(request.getParameter("nombre")).thenReturn("Construcción de Software");
        when(request.getParameter("codigo")).thenReturn("CSW-2026B");
        when(request.getParameter("descripcion")).thenReturn("Descripción");
        when(request.getRequestDispatcher("/WEB-INF/views/misCursos.jsp")).thenReturn(dispatcher);

        String mensajeError = "El código de curso ingresado ya se encuentra en uso; ingrese un código único";
        when(cursoService.crearCurso(any(NuevoCursoDTO.class), eq("docente@epn.edu.ec")))
                .thenThrow(new ExcepcionNegocio(mensajeError));

        cursoServlet.doPost(request, response);

        verify(request).setAttribute("error", mensajeError);
        verify(cursoService).listarCursosPorCorreoDocente("docente@epn.edu.ec");
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Seguridad: Estudiante que intenta hacer POST a /curso es rechazado")
    void doPost_cuandoUsuarioEnSesionEsEstudiante_redirigeConErrorPermisos() throws Exception {
        UsuarioDTO estudianteDto = new UsuarioDTO(1L, "Juan Estudiante", "estudiante@epn.edu.ec", rolEnum.DOCENTE);

        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuario")).thenReturn(estudianteDto);
        when(request.getContextPath()).thenReturn("/owlaudit");

        cursoServlet.doPost(request, response);

        verify(cursoService, never()).crearCurso(any(), any());
        verify(response).sendRedirect("/owlaudit/curso?error=permisos");
    }
}
