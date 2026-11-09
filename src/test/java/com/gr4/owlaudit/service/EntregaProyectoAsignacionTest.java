package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResultadoDTO.DetalleResultadoDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaStrategy;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EP-02 >> FT-04 >> HU-01: Entregar proyecto académico dentro de una asignación específica por el estudiante.
 * Batería de pruebas unitarias con Mocks y Pruebas Parametrizadas (TDD).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EntregaProyectoAsignacionTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private EvaluadorReglaFactory evaluadorFactory;

    @Mock
    private EvaluadorReglaStrategy evaluadorRegla;

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private AsignacionesService asignacionesService;

    private AuditoriaServiceImpl auditoriaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaServiceImpl(
                reglaDAO,
                auditoriaDAO,
                gitHubClient,
                evaluadorFactory,
                proyectoDAO,
                asignacionesService
        );
    }

    @Test
    @DisplayName("CA-2 Escenario 2.1 [RED]: Rechazar entrega cuando la fecha límite de la asignación está vencida")
    void test1_deberiaRechazarEntregaCuandoFechaLimiteHaExpirado() {
        // GIVEN: Asignación cuya fecha límite ya pasó (ayer)
        Long idAsignacion = 100L;
        Long idProyecto = 10L;
        Long idUsuario = 1L;

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(idAsignacion);
        dto.setProyectoId(idProyecto);
        dto.setUsuarioId(idUsuario);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(idProyecto);
        proyecto.setUrlGithub("https://github.com/estudiante/repo-vencido");
        Estudiante est = new Estudiante();
        est.setId(idUsuario);
        proyecto.setEstudiante(est);
        when(proyectoDAO.buscarPorId(idProyecto)).thenReturn(proyecto);

        Asignacion asignacionVencida = new Asignacion();
        asignacionVencida.setId(idAsignacion);
        asignacionVencida.setFechaLimite(LocalDateTime.now().minusDays(1));
        when(asignacionesService.buscarPorId(idAsignacion)).thenReturn(asignacionVencida);

        // WHEN & THEN: Lanza ExcepcionNegocio
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                auditoriaService.ejecutarAuditoriaDeAsignacion(dto));

        assertTrue(ex.getMessage().contains("fecha límite") || ex.getMessage().contains("expirado") || ex.getMessage().contains("cerrada"),
                "El mensaje debe indicar que la fecha límite ha expirado o que la asignación está cerrada");

        // Verificación de comportamiento: NUNCA se invoca GitHub ni se persiste auditoría
        verify(gitHubClient, never()).obtenerEstructuraDelRepositorio(anyString());
        verify(auditoriaDAO, never()).registrarAuditoriaEnElHistorial(any());
    }

    @Test
    @DisplayName("CA-1 Escenario 1.2 [RED]: Bloquear entrega cuando el estudiante ha agotado los intentos permitidos")
    void test2_deberiaBloquearEntregaCuandoSeAgotaronIntentosPermitidos() {
        // GIVEN: Asignación vigente pero con 2 intentos máximos ya consumidos
        Long idAsignacion = 101L;
        Long idProyecto = 11L;
        Long idUsuario = 2L;

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(idAsignacion);
        dto.setProyectoId(idProyecto);
        dto.setUsuarioId(idUsuario);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(idProyecto);
        proyecto.setUrlGithub("https://github.com/estudiante/repo-intentos");
        Estudiante est = new Estudiante();
        est.setId(idUsuario);
        proyecto.setEstudiante(est);
        when(proyectoDAO.buscarPorId(idProyecto)).thenReturn(proyecto);

        Asignacion asignacion = new Asignacion();
        asignacion.setId(idAsignacion);
        asignacion.setFechaLimite(LocalDateTime.now().plusDays(3)); // Vigente
        asignacion.setIntentosPermitidos(2);
        when(asignacionesService.buscarPorId(idAsignacion)).thenReturn(asignacion);

        // Simula que el estudiante ya realizó 2 auditorías en esta asignación
        when(auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(idAsignacion, idUsuario))
                .thenReturn(List.of(new Auditoria(), new Auditoria()));

        // WHEN & THEN: Lanza ExcepcionNegocio por límite de intentos
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                auditoriaService.ejecutarAuditoriaDeAsignacion(dto));

        assertTrue(ex.getMessage().contains("intentos") || ex.getMessage().contains("límite"),
                "El mensaje debe indicar que se alcanzó el límite de intentos permitidos");

        verify(gitHubClient, never()).obtenerEstructuraDelRepositorio(anyString());
        verify(auditoriaDAO, never()).registrarAuditoriaEnElHistorial(any());
    }

    @ParameterizedTest(name = "Umbral: {0}%, Cumple Regla: {1} -> Dictamen Esperado: {2}")
    @CsvSource({
            "70.0, true, APROBADA",
            "70.0, false, NO APROBADA",
            "85.0, true, APROBADA",
            "85.0, false, NO APROBADA"
    })
    @DisplayName("CA-1 Escenario 1.1 [RED]: Otorgar dictamen según el umbral configurado de la asignación")
    void test3_deberiaEvaluarDictamenSegunUmbralConfiguradoConParameterizedTest(
            double umbralAsignacion, boolean cumpleRegla, String dictamenEsperado) {

        Long idAsignacion = 102L;
        Long idProyecto = 12L;
        Long idUsuario = 3L;

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(idAsignacion);
        dto.setProyectoId(idProyecto);
        dto.setUsuarioId(idUsuario);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(idProyecto);
        proyecto.setUrlGithub("https://github.com/estudiante/repo-umbral");
        Estudiante est = new Estudiante();
        est.setId(idUsuario);
        proyecto.setEstudiante(est);
        when(proyectoDAO.buscarPorId(idProyecto)).thenReturn(proyecto);

        Asignacion asignacion = new Asignacion();
        asignacion.setId(idAsignacion);
        asignacion.setFechaLimite(LocalDateTime.now().plusDays(2)); // Vigente
        asignacion.setIntentosPermitidos(3);
        asignacion.setUmbralAprobacion(umbralAsignacion);
        when(asignacionesService.buscarPorId(idAsignacion)).thenReturn(asignacion);

        when(auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(idAsignacion, idUsuario))
                .thenReturn(Collections.emptyList()); // 0 intentos consumidos, tiene disponibles

        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString()))
                .thenReturn(List.of("src", "README.md"));

        Regla regla = new Regla();
        regla.setNombreRepresentativo("Presencia README");
        regla.setPonderacion(100);
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.MEDIA);

        when(asignacionesService.consultarReglasDeAsignacion(idAsignacion)).thenReturn(List.of(regla));
        when(asignacionesService.consultarUmbralAprobacion(idAsignacion)).thenReturn(umbralAsignacion);

        when(evaluadorFactory.obtenerEvaluador(TipoMotorEnum.PRESENCIA_ARCHIVO)).thenReturn(evaluadorRegla);
        when(evaluadorRegla.evaluar(anyString(), anyList())).thenReturn(cumpleRegla);

        // WHEN
        ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(dto);

        // THEN
        assertNotNull(resultado);
        assertEquals(dictamenEsperado, resultado.getDictamen());
        verify(auditoriaDAO).registrarAuditoriaEnElHistorial(any(Auditoria.class));
    }

    @Test
    @DisplayName("CA-3 Escenario 3.1 [RED]: Presentar evidencia, severidad y recomendación en los hallazgos de incumplimiento")
    void test4_deberiaIncluirEvidenciaSeveridadYRecomendacionEnHallazgos() {
        Long idAsignacion = 103L;
        Long idProyecto = 13L;
        Long idUsuario = 4L;

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(idAsignacion);
        dto.setProyectoId(idProyecto);
        dto.setUsuarioId(idUsuario);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(idProyecto);
        proyecto.setUrlGithub("https://github.com/estudiante/repo-hallazgos");
        Estudiante est = new Estudiante();
        est.setId(idUsuario);
        proyecto.setEstudiante(est);
        when(proyectoDAO.buscarPorId(idProyecto)).thenReturn(proyecto);

        Asignacion asignacion = new Asignacion();
        asignacion.setId(idAsignacion);
        asignacion.setFechaLimite(LocalDateTime.now().plusDays(5));
        asignacion.setIntentosPermitidos(3);
        asignacion.setUmbralAprobacion(80.0);
        when(asignacionesService.buscarPorId(idAsignacion)).thenReturn(asignacion);

        when(auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(idAsignacion, idUsuario))
                .thenReturn(Collections.emptyList());

        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString()))
                .thenReturn(List.of("src"));

        Regla reglaCritica = new Regla();
        reglaCritica.setNombreRepresentativo("Presencia pom.xml");
        reglaCritica.setPonderacion(50);
        reglaCritica.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaCritica.setParametroExacto("pom.xml");
        reglaCritica.setNivelSeveridad(NivelSeveridadEnum.ALTA);

        when(asignacionesService.consultarReglasDeAsignacion(idAsignacion)).thenReturn(List.of(reglaCritica));
        when(asignacionesService.consultarUmbralAprobacion(idAsignacion)).thenReturn(80.0);

        when(evaluadorFactory.obtenerEvaluador(TipoMotorEnum.PRESENCIA_ARCHIVO)).thenReturn(evaluadorRegla);
        when(evaluadorRegla.evaluar(anyString(), anyList())).thenReturn(false); // No cumple

        // WHEN
        ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(dto);

        // THEN
        assertNotNull(resultado);
        assertNotNull(resultado.getDetalles());
        assertFalse(resultado.getDetalles().isEmpty());

        DetalleResultadoDTO detalle = resultado.getDetalles().get(0);
        assertFalse(detalle.isCumple());
        assertEquals(NivelSeveridadEnum.ALTA, detalle.getNivelSeveridad());
        assertNotNull(detalle.getEvidencia());
        assertTrue(detalle.getEvidencia().contains("pom.xml"));
        assertNotNull(detalle.getRecomendacion());
        assertTrue(detalle.getRecomendacion().contains("pom.xml"));
    }
}
