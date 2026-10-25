package com.gr4.owlaudit.servlet;

import java.util.Collections;
import java.util.List;

import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.service.AsignacionesService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-02 · Crear asignaciones para auditorías específicas.
 * Pruebas unitarias de AsignacionServlet con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsignacionServletTest {

    private static final String VISTA_CREAR = "/WEB-INF/views/crearAsignacion.jsp";

    @Mock
    private AsignacionesService asignacionesService;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private AsignacionServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new AsignacionServlet(asignacionesService, reglaDAO);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    @Test
    @DisplayName("doGet: debe consultar reglas vigentes, asignaciones y despachar a crearAsignacion.jsp")
    void given_peticionGet_when_doGet_then_cargaReglasVigentesYDespacha() throws Exception {
        // GIVEN
        List<Regla> reglasVigentes = Collections.emptyList();
        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(reglasVigentes);
        when(asignacionesService.listarAsignaciones()).thenReturn(Collections.emptyList());

        // WHEN
        servlet.doGet(request, response);

        // THEN
        verify(reglaDAO).consultarReglasDeEvaluacionVigentes();
        verify(request).setAttribute("reglasVigentes", reglasVigentes);
        verify(asignacionesService).listarAsignaciones();
        verify(request).setAttribute(eq("asignaciones"), any());
        verify(request).getRequestDispatcher(VISTA_CREAR);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Escenario 2.3 - Umbral con formato numérico inválido muestra mensaje de error")
    void given_umbralInvalido_when_doPost_then_retornaMensajeErrorFormato() throws Exception {
        // GIVEN: El docente ingresa un umbral no numérico ("abc")
        when(request.getParameter("nombre")).thenReturn("Laboratorio 1");
        when(request.getParameter("umbralAprobacion")).thenReturn("abc");
        when(request.getParameterValues("reglasIds")).thenReturn(new String[]{"1", "2"});

        // WHEN
        servlet.doPost(request, response);

        // THEN: Captura el error de formato y no invoca a la capa de servicio
        verify(request).setAttribute(eq("mensajeError"), contains("umbral"));
        verify(request).getRequestDispatcher(VISTA_CREAR);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost: Escenario 2.1 - Creación exitosa empaqueta DTO, invoca servicio y muestra mensaje de éxito")
    void given_datosValidos_when_doPost_then_creaAsignacionYDespachaExito() throws Exception {
        // GIVEN: Parámetros válidos
        when(request.getParameter("nombre")).thenReturn("Examen Final");
        when(request.getParameter("umbralAprobacion")).thenReturn("75.5");
        when(request.getParameterValues("reglasIds")).thenReturn(new String[]{"1", "2"});
        when(asignacionesService.crearAsignacion(any(NuevaAsignacionDTO.class))).thenReturn(new Asignacion());

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(asignacionesService).crearAsignacion(any(NuevaAsignacionDTO.class));
        verify(request).setAttribute(eq("mensajeExito"), contains("correctamente"));
        verify(request).getRequestDispatcher(VISTA_CREAR);
        verify(dispatcher).forward(request, response);
    }
}
