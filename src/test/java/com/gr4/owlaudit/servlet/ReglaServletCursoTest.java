package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.service.ReglaService;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ReglaServlet con contexto de Curso.
 * Historia de Usuario: EP-01 >> FT-02 >> HU-01: Crear reglas para el curso.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglaServletCursoTest {

    private static final String VISTA_CATALOGO = "/WEB-INF/views/catalogoReglas.jsp";
    private static final String VISTA_REGISTRO = "/WEB-INF/views/registrarRegla.jsp";

    @Mock
    private ReglaService reglaService;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    @Mock
    private RequestDispatcher dispatcher;

    private ReglaServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ReglaServlet(reglaService, reglaDAO);
        when(request.getSession()).thenReturn(session);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("Given idCurso en solicitud, When docente accede al catalogo, Then consulta y aisla las reglas de ese curso")
    void givenIdCursoEnParametro_whenDoGetCatalogo_thenFiltraReglasPorCursoYEstableceIdCursoEnRequest() throws Exception {
        when(request.getParameter("idCurso")).thenReturn("10");

        Regla reglaCurso = new Regla();
        reglaCurso.setId(1L);
        reglaCurso.setNombreRepresentativo("Regla de CSW");
        List<Regla> reglasCurso = List.of(reglaCurso);

        when(reglaService.listarReglasPorCurso(10L)).thenReturn(reglasCurso);
        when(reglaService.listarReglasVigentesPorCurso(10L)).thenReturn(reglasCurso);

        servlet.doGet(request, response);

        verify(request).setAttribute(eq("idCurso"), eq(10L));
        verify(request).setAttribute(eq("reglas"), eq(reglasCurso));
        verify(request).setAttribute(eq("reglasVigentes"), eq(reglasCurso));
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("Given idCurso y datos validos en formulario, When docente registra regla, Then el servlet asigna idCurso al DTO y delega al servicio")
    void givenIdCursoYDatosValidos_whenDoPostCrearRegla_thenAsignaIdCursoADTOYRedirigeACatalogoConExito() throws Exception {
        when(request.getParameter("idCurso")).thenReturn("10");
        when(request.getParameter("tipoMotor")).thenReturn("PRESENCIA_ARCHIVO");
        when(request.getParameter("nombreRepresentativo")).thenReturn("Validar README");
        when(request.getParameter("descripcionDetallada")).thenReturn("Exige README en raiz");
        when(request.getParameter("parametroExacto")).thenReturn("README.md");
        when(request.getParameter("nivelSeveridad")).thenReturn("ALTA");
        when(request.getParameter("ponderacion")).thenReturn("20");

        servlet.doPost(request, response);

        ArgumentCaptor<NuevaReglaDTO> dtoCaptor = ArgumentCaptor.forClass(NuevaReglaDTO.class);
        verify(reglaService).registrarReglaEvaluacionTecnica(dtoCaptor.capture());

        NuevaReglaDTO capturado = dtoCaptor.getValue();
        assertEquals(10L, capturado.getIdCurso());
        assertEquals("Validar README", capturado.getNombreRepresentativo());
        assertEquals("README.md", capturado.getParametroExacto());
        verify(request).setAttribute(eq("idCurso"), eq(10L));
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }
}

