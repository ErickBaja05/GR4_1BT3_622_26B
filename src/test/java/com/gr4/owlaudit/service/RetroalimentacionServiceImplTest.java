package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.EstadoRetroEnum;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para CU05 y CU06 con Mockito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RetroalimentacionServiceImplTest {

    @Mock
    private SolicitudDAO solicitudDAO;

    @InjectMocks
    private RetroalimentacionServiceImpl retroService;

    private SolicitudRetroalimentacion solicitudR4;
    private Hallazgo hallazgoR4;

    @BeforeEach
    void setUp() {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setNombre("Proyecto Demo");

        Auditoria auditoria = new Auditoria();
        auditoria.setId(102L);
        auditoria.setProyecto(proyecto);
        auditoria.setFechaHora(LocalDateTime.of(2026, 10, 12, 11, 0));

        Regla reglaEnv = new Regla();
        reglaEnv.setId(4L);
        reglaEnv.setNombreRepresentativo("Sin archivos env");
        reglaEnv.setTipoMotor(TipoMotorEnum.RESTRICCION_ARCHIVOS);
        reglaEnv.setParametroExacto(".env");
        reglaEnv.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        reglaEnv.setPonderacion(5);

        ResultadoRegla resultadoEnv = new ResultadoRegla(false, 0, reglaEnv, auditoria);

        hallazgoR4 = new Hallazgo(NivelSeveridadEnum.ALTA, "Contiene archivos prohibidos: .env", "Elimine los archivos .env", resultadoEnv);
        hallazgoR4.setId(401L);
        resultadoEnv.setHallazgo(hallazgoR4);

        solicitudR4 = new SolicitudRetroalimentacion();
        solicitudR4.setId(10L);
        solicitudR4.setHallazgo(hallazgoR4);
        solicitudR4.setJustificacion("¿Cómo podemos compartir las variables necesarias para producción sin subir el archivo .env?");
        solicitudR4.setEstado(EstadoRetroEnum.PENDIENTE);
    }

    // ========================================================
    // MÉTODOS AUXILIARES DE PRUEBA
    // ========================================================

    private void dadoHallazgoSinSolicitudPendiente(Long hallazgoId) {
        when(solicitudDAO.buscarSolicitudPendientePorHallazgo(hallazgoId)).thenReturn(null);
    }

    private void dadoHallazgoConSolicitudPendiente(Long hallazgoId, SolicitudRetroalimentacion pendiente) {
        when(solicitudDAO.buscarSolicitudPendientePorHallazgo(hallazgoId)).thenReturn(pendiente);
    }

    private void assertHallazgoEnRevision(SolicitudDTO dto) {
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dto));
        assertEquals("Hallazgo en revisión", ex.getMessage());
    }

    // ========================================================
    // PRUEBAS UNITARIAS DE TDD
    // ========================================================

    @Test
    @DisplayName("CU05: Permite re-consulta si la solicitud previa está atendida")
    void given_solicitudAtendida_when_reconsulta_then_ok() {
        // GIVEN: Un hallazgo (ID 401) cuya solicitud previa ya fue respondida (ATENDIDA)
        Long hallazgoId = 401L;
        solicitudR4.marcarSolicitudComoAtendida();
        solicitudR4.setOrientacionTecnica("Orientación técnica previa emitida por el docente.");
        dadoHallazgoSinSolicitudPendiente(hallazgoId);

        // WHEN: El estudiante formula una re-consulta
        SolicitudDTO reconsultaDto = new SolicitudDTO(hallazgoId, "¿Cómo configuro el archivo .env.example en producción?");

        // THEN: No debe lanzar excepción y debe abrir un nuevo ciclo de feedback
        assertDoesNotThrow(() -> retroService.solicitarRetroalimentacionDeHallazgo(reconsultaDto),
                "Debe permitir una nueva solicitud de retroalimentación si la anterior ya fue ATENDIDA");
        verify(solicitudDAO).crearSolicitudDeRetroalimentacion(any(SolicitudRetroalimentacion.class));
    }

    // ========================================================
    // PRUEBAS EXISTENTES DEL SISTEMA (CU05 & CU06)
    // ========================================================

    @Test
    @DisplayName("CP-CU06-01: Atención exitosa de una retroalimentación solicitada por un Estudiante")
    void testAtencionExitosaCP_CU06_01() {
        String respuestaTecnica = "Se debe eliminar inmediatamente el archivo .env del repositorio remoto y agregarlo al .gitignore. " +
                "Para permitir que otros desarrolladores configuren su entorno, cree un archivo plantilla llamado .env.example en la raíz " +
                "con los nombres de las variables y valores vacíos, documentando su uso en el README.md.";

        SolicitudDTO dto = new SolicitudDTO(null, 10L, null, respuestaTecnica);
        when(solicitudDAO.buscarPorId(10L)).thenReturn(solicitudR4);

        retroService.registrarOrientacionTecnicaEnLaSolicitud(dto);

        verify(solicitudDAO).actualizarSolicitud(solicitudR4);
        assertEquals(EstadoRetroEnum.ATENDIDA, solicitudR4.getEstado(), "El estado debe cambiar a ATENDIDA");
        assertEquals(respuestaTecnica, solicitudR4.getOrientacionTecnica(), "La orientación técnica debe ser la ingresada por el Docente");
    }

    @Test
    @DisplayName("Escenario Alternativo 3: Orientación en blanco o menor a 10 caracteres")
    void testAlternativo3_OrientacionInvalidaCorta() {
        SolicitudDTO dtoVacio = new SolicitudDTO(null, 10L, null, "");
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoVacio));
        assertEquals("Orientación en blanco o inválida", ex1.getMessage());

        SolicitudDTO dtoEspacios = new SolicitudDTO(null, 10L, null, "   ");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoEspacios));
        assertEquals("Orientación en blanco o inválida", ex2.getMessage());

        SolicitudDTO dtoCorta = new SolicitudDTO(null, 10L, null, "Borre eso");
        ExcepcionNegocio ex3 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoCorta));
        assertEquals("Orientación en blanco o inválida", ex3.getMessage());

        SolicitudDTO dtoNull = new SolicitudDTO(null, 10L, null, null);
        ExcepcionNegocio ex4 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoNull));
        assertEquals("Orientación en blanco o inválida", ex4.getMessage());

        verifyNoInteractions(solicitudDAO);
    }

    @Test
    @DisplayName("Escenario Alternativo 3: Orientación mayor a 1000 caracteres")
    void testAlternativo3_OrientacionInvalidaLarga() {
        String textoLargo = "a".repeat(1001);
        SolicitudDTO dto = new SolicitudDTO(null, 10L, null, textoLargo);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dto));
        assertEquals("Orientación en blanco o inválida", ex.getMessage());

        verifyNoInteractions(solicitudDAO);
    }

    @Test
    @DisplayName("Validación: Solicitud inexistente o ya atendida")
    void testSolicitudInexistenteOAtendida() {
        SolicitudDTO dtoInexistente = new SolicitudDTO(null, 999L, null, "Texto de orientación técnica válido con suficiente longitud.");
        when(solicitudDAO.buscarPorId(999L)).thenReturn(null);

        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoInexistente));
        assertEquals("La solicitud de retroalimentación no existe.", ex1.getMessage());

        solicitudR4.marcarSolicitudComoAtendida();
        when(solicitudDAO.buscarPorId(10L)).thenReturn(solicitudR4);

        SolicitudDTO dtoAtendida = new SolicitudDTO(null, 10L, null, "Texto de orientación técnica válido con suficiente longitud.");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoAtendida));
        assertEquals("La solicitud ya ha sido atendida.", ex2.getMessage());
    }

    @Test
    @DisplayName("CU05: Solicitar retroalimentación de hallazgo")
    void testSolicitarRetroalimentacionDeHallazgo() {
        SolicitudDTO nuevoDto = new SolicitudDTO(501L, "Tengo dudas sobre cómo organizar las carpetas en src");
        dadoHallazgoSinSolicitudPendiente(501L);

        retroService.solicitarRetroalimentacionDeHallazgo(nuevoDto);
        verify(solicitudDAO).crearSolicitudDeRetroalimentacion(any(SolicitudRetroalimentacion.class));

        dadoHallazgoConSolicitudPendiente(401L, solicitudR4);
        assertHallazgoEnRevision(new SolicitudDTO(401L, "Otra consulta sobre el mismo hallazgo"));
    }

    @Test
    @DisplayName("CU05 - CP-CU05-01: Solicitud exitosa para el hallazgo de una auditoría")
    void testSolicitudExitosaCP_CU05_01() {
        SolicitudDTO dto = new SolicitudDTO(801L, "La función requiere múltiples estructuras condicionales para el control de errores de entrada");
        dadoHallazgoSinSolicitudPendiente(801L);

        retroService.solicitarRetroalimentacionDeHallazgo(dto);

        ArgumentCaptor<SolicitudRetroalimentacion> captor = ArgumentCaptor.forClass(SolicitudRetroalimentacion.class);
        verify(solicitudDAO).crearSolicitudDeRetroalimentacion(captor.capture());
        SolicitudRetroalimentacion creada = captor.getValue();
        assertNotNull(creada, "La solicitud debe haber sido creada y persistida");
        assertEquals(EstadoRetroEnum.PENDIENTE, creada.getEstado(), "El estado inicial debe ser PENDIENTE");
        assertEquals(dto.getJustificacion(), creada.getJustificacion());
    }

    @Test
    @DisplayName("CU05 - Escenario Alternativo 1: Hallazgo ya en proceso de revisión")
    void testSolicitudHallazgoYaEnRevision() {
        dadoHallazgoConSolicitudPendiente(401L, solicitudR4);
        assertHallazgoEnRevision(new SolicitudDTO(401L, "Otra consulta sobre el mismo hallazgo R4"));
    }

    @Test
    @DisplayName("CU05: Validación de hallazgoId inválido")
    void testSolicitudHallazgoIdInvalido() {
        SolicitudDTO dtoNull = new SolicitudDTO(null, "Justificación con longitud válida");
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoNull));
        assertEquals("El identificador del hallazgo no es válido.", ex1.getMessage());

        SolicitudDTO dtoCero = new SolicitudDTO(0L, "Justificación con longitud válida");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoCero));
        assertEquals("El identificador del hallazgo no es válido.", ex2.getMessage());

        verifyNoInteractions(solicitudDAO);
    }
}
