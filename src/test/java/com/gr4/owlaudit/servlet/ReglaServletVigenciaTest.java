package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.service.ReglaService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-01 · Activar / Desactivar vigencia de reglas.
 * Pruebas de ReglaServlet con mocks: sin Tomcat, sin base de datos
 * y sin depender de ReglaServiceImpl ni de ReglaDAOImpl.
 *
 * Strictness.LENIENT: el doPost lee también los parámetros del registro de reglas (CU01),
 * que estas pruebas no simulan.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglaServletVigenciaTest {

    private static final String VISTA_CATALOGO = "/WEB-INF/views/catalogoReglas.jsp";

    @Mock private ReglaService reglaService;
    @Mock private ReglaDAO reglaDAO;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private RequestDispatcher dispatcher;

    private ReglaServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ReglaServlet(reglaService, reglaDAO);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    /** Simula el formulario del catálogo: POST /regla con accion=cambiarVigencia. */
    private void simularSolicitudDeCambio(String nombreRegla, String nuevoEstado) {
        when(request.getParameter("accion")).thenReturn("cambiarVigencia");
        when(request.getParameter("nombreRegla")).thenReturn(nombreRegla);
        when(request.getParameter("nuevoEstado")).thenReturn(nuevoEstado);
    }

    @ParameterizedTest(name = "Cambiar a {0} -> mensaje contiene \"{1}\"")
    @CsvSource({
            "NO_VIGENTE, desactivada",   // Escenario 1.1: desactivación exitosa
            "VIGENTE,    reactivada"     // Escenario 1.2: reactivación exitosa
    })
    @DisplayName("POST cambiarVigencia: invoca al servicio y confirma el cambio (Esc. 1.1 y 1.2)")
    void given_cambioValido_when_doPost_then_confirmaCambio(String estadoSolicitado,
                                                            String textoEsperado) throws Exception {
        // GIVEN: el docente elige la regla "Archivo README" y el servicio acepta el cambio
        EstadoVigencia estado = EstadoVigencia.valueOf(estadoSolicitado);
        simularSolicitudDeCambio("Archivo README", estadoSolicitado);
        when(reglaService.cambiarEstadoVigenciaDeUnaRegla("Archivo README", estado)).thenReturn(true);

        // WHEN: envía la solicitud de cambio
        servlet.doPost(request, response);

        // THEN: delega al servicio, confirma el cambio y responde hacia la vista del catálogo
        verify(reglaService).cambiarEstadoVigenciaDeUnaRegla("Archivo README", estado);
        verify(request).setAttribute(eq("mensajeExito"), contains(textoEsperado));
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("POST cambiarVigencia: Escenario 1.3 (la regla ya se encuentra en el estado solicitado)")
    void given_reglaEnMismoEstado_when_doPost_then_indicaQueYaSeEncuentraEnEseEstado() throws Exception {
        // GIVEN: El docente solicita cambiar a NO_VIGENTE una regla que ya está no vigente (el servicio retorna false)
        String nombreRegla = "Archivo README";
        EstadoVigencia estadoSolicitado = EstadoVigencia.NO_VIGENTE;
        simularSolicitudDeCambio(nombreRegla, "NO_VIGENTE");
        when(reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, estadoSolicitado)).thenReturn(false);

        // WHEN: envía la solicitud de cambio
        servlet.doPost(request, response);

        // THEN: delega al servicio, notifica que ya está en ese estado y responde hacia la vista del catálogo
        verify(reglaService).cambiarEstadoVigenciaDeUnaRegla(nombreRegla, estadoSolicitado);
        verify(request).setAttribute(eq("mensajeError"), contains("ya se encuentra en estado no vigente"));
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }
}
