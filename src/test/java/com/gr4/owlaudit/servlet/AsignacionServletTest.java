package com.gr4.owlaudit.servlet;

import java.util.Collections;
import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.AuditoriaService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-02 · Crear asignaciones para auditorías específicas.
 * HU-03 · Ejecutar auditoría de proyecto bajo una asignación específica.
 * Pruebas unitarias de AsignacionServlet con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsignacionServletTest {

    private static final String VISTA_CREAR = "/WEB-INF/views/crearAsignacion.jsp";
    private static final String VISTA_DETALLE_ESTUDIANTE = "/WEB-INF/views/DetalleAsignacionJSP.jsp";

    @Mock
    private AsignacionesService asignacionesService;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    private AsignacionServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new AsignacionServlet(asignacionesService, reglaDAO, auditoriaService);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    // =========================================================================
    // PRUEBAS HU-02 (DOCENTE)
    // =========================================================================

    @Test
    @DisplayName("doGet (HU-02): debe consultar reglas vigentes, asignaciones y despachar a crearAsignacion.jsp")
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
    @DisplayName("doPost (HU-02): Escenario 2.3 - Umbral con formato numérico inválido muestra mensaje de error")
    void given_umbralInvalido_when_doPost_then_retornaMensajeErrorFormato() throws Exception {
        // GIVEN: El docente ingresa un umbral no numérico ("abc")
        when(request.getParameter("nombre")).thenReturn("Laboratorio 1");
        when(request.getParameter("umbralAprobacion")).thenReturn("abc");
        when(request.getParameterValues("reglasIds")).thenReturn(new String[] { "1", "2" });

        // WHEN
        servlet.doPost(request, response);

        // THEN: Captura el error de formato y no invoca a la capa de servicio
        verify(request).setAttribute(eq("mensajeError"), contains("umbral"));
        verify(request).getRequestDispatcher(VISTA_CREAR);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost (HU-02): Escenario 2.1 - Creación exitosa empaqueta DTO, invoca servicio y muestra mensaje de éxito")
    void given_datosValidos_when_doPost_then_creaAsignacionYDespachaExito() throws Exception {
        // GIVEN: Parámetros válidos
        when(request.getParameter("nombre")).thenReturn("Examen Final");
        when(request.getParameter("umbralAprobacion")).thenReturn("75.5");
        when(request.getParameterValues("reglasIds")).thenReturn(new String[] { "1", "2" });
        when(asignacionesService.crearAsignacion(any(NuevaAsignacionDTO.class))).thenReturn(new Asignacion());

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(asignacionesService).crearAsignacion(any(NuevaAsignacionDTO.class));
        verify(request).setAttribute(eq("mensajeExito"), contains("correctamente"));
        verify(request).getRequestDispatcher(VISTA_CREAR);
        verify(dispatcher).forward(request, response);
    }

    // =========================================================================
    // PRUEBAS HU-03 (ESTUDIANTE)
    // =========================================================================

    @Test
    @DisplayName("doGet (HU-03): Ciclo 1 - Con idAsignacion válido genera resumen y despacha a DetalleAsignacionJSP")
    void given_idAsignacionValido_when_doGet_then_generaResumenYDespachaADetalle() throws Exception {
        // GIVEN
        when(request.getParameter("idAsignacion")).thenReturn("5");
        ResumenAsignacionDTO resumenEsperado = new ResumenAsignacionDTO();
        resumenEsperado.setIdAsignacion(5L);
        resumenEsperado.setNombreAsignacion("Entrega Parcial 1");
        resumenEsperado.setUmbralAprobacion(75.0);

        when(asignacionesService.generarResumenAsignacion(5L)).thenReturn(resumenEsperado);
        when(asignacionesService.listarAsignaciones()).thenReturn(Collections.emptyList());

        // WHEN
        servlet.doGet(request, response);

        // THEN
        verify(asignacionesService).generarResumenAsignacion(5L);
        verify(request).setAttribute("resumenAsignacion", resumenEsperado);
        verify(request).getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doGet (HU-03): Ciclo 2 - Con idAsignacion inválido no invoca servicio y muestra error")
    void given_idAsignacionInvalido_when_doGet_then_noLlamaServicioYMuestraError() throws Exception {
        // GIVEN
        when(request.getParameter("idAsignacion")).thenReturn("abc");

        // WHEN
        servlet.doGet(request, response);

        // THEN
        verify(asignacionesService, never()).generarResumenAsignacion(anyLong());
        verify(request).setAttribute(eq("mensajeError"), anyString());
        verify(request).getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost (HU-03): Escenarios 1.1 y 2.3 - Con proyecto y asignación válidos ejecuta auditoría e inyecta resultado")
    void given_asignacionYProyectoValidos_when_doPost_then_ejecutaAuditoriaYDespachaResultado() throws Exception {
        // GIVEN
        when(request.getParameter("idAsignacion")).thenReturn("5");
        when(request.getParameter("idProyecto")).thenReturn("10");

        ResultadoDTO resultadoEsperado = new ResultadoDTO(85, 100, 85.0);
        resultadoEsperado.setDictamen("APROBADA");
        resultadoEsperado.setUmbralAprobacion(75.0);

        when(auditoriaService.ejecutarAuditoriaDeAsignacion(any(AuditoriaDTO.class))).thenReturn(resultadoEsperado);

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(auditoriaService).ejecutarAuditoriaDeAsignacion(any(AuditoriaDTO.class));
        verify(request).setAttribute("resultadoAuditoria", resultadoEsperado);
        verify(request).getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("doPost (HU-03): Escenario 1.2 - Sin seleccionar proyecto captura ExcepcionNegocio y muestra mensaje de error")
    void given_sinProyectoSeleccionado_when_doPost_then_muestraMensajeErrorYDespacha() throws Exception {
        // GIVEN
        when(request.getParameter("idAsignacion")).thenReturn("5");
        when(request.getParameter("idProyecto")).thenReturn("");
        when(auditoriaService.ejecutarAuditoriaDeAsignacion(any(AuditoriaDTO.class)))
                .thenThrow(new ExcepcionNegocio("Debe seleccionar un proyecto válido"));

        // WHEN
        servlet.doPost(request, response);

        // THEN
        verify(request).setAttribute("mensajeError", "Debe seleccionar un proyecto válido");
        verify(request).getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE);
        verify(dispatcher).forward(request, response);
    }
}
