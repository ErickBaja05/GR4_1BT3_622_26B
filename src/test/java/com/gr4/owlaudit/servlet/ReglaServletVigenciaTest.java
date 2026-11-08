package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.ReglaService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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
    @Mock private HttpSession session;

    private ReglaServlet servlet;

    @BeforeEach
    void setUp() {
        servlet = new ReglaServlet(reglaService, reglaDAO);
        when(request.getRequestDispatcher(anyString())).thenReturn(dispatcher);
    }

    /** Simula el formulario del catálogo: POST /regla con accion=cambiarVigencia y contexto de curso. */
    private void simularSolicitudDeCambio(String nombreRegla, String nuevoEstado) {
        when(request.getParameter("idCurso")).thenReturn("10");
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

    /** Simula el modal "Modificar Regla" enviado por el docente titular en sesión (id 7). */
    private void simularModificacionDeRegla(String descripcion, String vigencia) {
        UsuarioDTO docente = new UsuarioDTO(7L, "Docente Titular", "docente@epn.edu.ec", RolEnum.DOCENTE);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(docente);
        when(request.getParameter("accion")).thenReturn("modificarRegla");
        when(request.getParameter("idCurso")).thenReturn("10");
        when(request.getParameter("idRegla")).thenReturn("5");
        when(request.getParameter("descripcionDetallada")).thenReturn(descripcion);
        when(request.getParameter("vigencia")).thenReturn(vigencia);
    }

    @Test
    @DisplayName("HU-02 CA1 y CA2: guarda solo descripción y vigencia, ignora nombre y motor enviados y confirma en el catálogo")
    void given_modificacionValida_when_doPost_then_confirmaEIgnoraNombreYMotor() throws Exception {
        simularModificacionDeRegla("Nueva descripción", "NO_VIGENTE");
        when(request.getParameter("nombreRepresentativo")).thenReturn("Nombre manipulado");
        when(request.getParameter("tipoMotor")).thenReturn("CONVENCION_COMMITS");

        servlet.doPost(request, response);

        verify(reglaService).modificarDescripcionYVigencia(5L, 10L, 7L, "Nueva descripción", EstadoVigencia.NO_VIGENTE);
        verify(reglaService, never()).registrarReglaEvaluacionTecnica(any());
        verify(request).setAttribute("mensajeExito", "Descripción actualizada exitosamente");
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("HU-02 CA3: muestra el mensaje de validación y permanece en el catálogo cuando el servicio rechaza la modificación")
    void given_servicioRechaza_when_doPost_then_muestraMensajeDeError() throws Exception {
        String mensaje = "La descripción detallada de la regla no puede quedar vacía";
        simularModificacionDeRegla("   ", "VIGENTE");
        doThrow(new ExcepcionNegocio(mensaje)).when(reglaService)
                .modificarDescripcionYVigencia(5L, 10L, 7L, "   ", EstadoVigencia.VIGENTE);

        servlet.doPost(request, response);

        verify(request).setAttribute("mensajeError", mensaje);
        verify(request, never()).setAttribute(eq("mensajeExito"), any());
        verify(request).getRequestDispatcher(VISTA_CATALOGO);
        verify(dispatcher).forward(request, response);
    }

    @Test
    @DisplayName("HU-02: rechaza un estado de vigencia manipulado sin invocar al servicio")
    void given_vigenciaInvalida_when_doPost_then_muestraErrorSinModificar() throws Exception {
        simularModificacionDeRegla("Descripción válida", "ESTADO_INVENTADO");

        servlet.doPost(request, response);

        verify(request).setAttribute("mensajeError", "El estado de vigencia seleccionado no es válido.");
        verify(reglaService, never()).modificarDescripcionYVigencia(any(), any(), any(), any(), any());
        verify(dispatcher).forward(request, response);
    }
}
