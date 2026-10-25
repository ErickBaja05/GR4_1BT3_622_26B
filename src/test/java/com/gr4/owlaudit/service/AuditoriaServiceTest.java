package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.model.Auditoria;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HU-03 · Ejecutar Auditoria bajo una asignación definida.
 * Pruebas unitarias para ejecutarAuditoriaDeAsignacion en AuditoriaServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

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
    @DisplayName("Escenario 1.2: Sin proyecto válido debe lanzar ExcepcionNegocio y no persistir auditoría")
    void given_proyectoInvalidoONulo_when_ejecutarAuditoriaDeAsignacion_then_lanzaExcepcionNegocio() {
        // GIVEN: DTO sin proyecto o con proyecto inexistente
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setProyectoId(999L);
        dto.setIdAsignacion(1L);

        when(proyectoDAO.buscarPorId(999L)).thenReturn(null);

        // WHEN & THEN
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                auditoriaService.ejecutarAuditoriaDeAsignacion(dto));

        assertEquals("Debe seleccionar un proyecto válido", ex.getMessage());
        verify(auditoriaDAO, never()).registrarAuditoriaEnElHistorial(any());
    }

    @Test
    @DisplayName("Escenario 2.1: Dictamen APROBADA cuando el porcentaje de cumplimiento supera o iguala el umbral")
    void given_evaluacionSuperaUmbral_when_ejecutarAuditoriaDeAsignacion_then_dictamenAprobada() {
        // GIVEN: Proyecto válido y asignación con umbral 70.0
        Long proyectoId = 1L;
        Long idAsignacion = 10L;
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setProyectoId(proyectoId);
        dto.setIdAsignacion(idAsignacion);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(proyectoId);
        proyecto.setUrlGithub("https://github.com/repo/aprobado");
        when(proyectoDAO.buscarPorId(proyectoId)).thenReturn(proyecto);

        when(gitHubClient.obtenerEstructuraDelRepositorio("https://github.com/repo/aprobado"))
                .thenReturn(List.of("src", "README.md"));

        Regla regla = new Regla();
        regla.setNombreRepresentativo("Presencia README");
        regla.setPonderacion(100);
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.BAJA);

        when(asignacionesService.consultarReglasDeAsignacion(idAsignacion)).thenReturn(List.of(regla));
        when(asignacionesService.consultarUmbralAprobacion(idAsignacion)).thenReturn(70.0);

        when(evaluadorFactory.obtenerEvaluador(TipoMotorEnum.PRESENCIA_ARCHIVO)).thenReturn(evaluadorRegla);
        when(evaluadorRegla.evaluar(anyString(), anyList())).thenReturn(true);

        // WHEN
        ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(dto);

        // THEN
        assertNotNull(resultado);
        assertEquals("APROBADA", resultado.getDictamen());
        assertEquals(70.0, resultado.getUmbralAprobacion());
        assertEquals(100.0, resultado.getPorcentaje());
        verify(auditoriaDAO).registrarAuditoriaEnElHistorial(any(Auditoria.class));
    }

    @Test
    @DisplayName("Escenario 2.2: Dictamen NO_APROBADA cuando el porcentaje no alcanza el umbral de la asignación")
    void given_evaluacionMenorAlUmbral_when_ejecutarAuditoriaDeAsignacion_then_dictamenNoAprobada() {
        // GIVEN: Proyecto válido y asignación con umbral 80.0
        Long proyectoId = 2L;
        Long idAsignacion = 20L;
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setProyectoId(proyectoId);
        dto.setIdAsignacion(idAsignacion);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(proyectoId);
        proyecto.setUrlGithub("https://github.com/repo/reprobado");
        when(proyectoDAO.buscarPorId(proyectoId)).thenReturn(proyecto);

        when(gitHubClient.obtenerEstructuraDelRepositorio("https://github.com/repo/reprobado"))
                .thenReturn(Collections.emptyList());

        Regla regla = new Regla();
        regla.setNombreRepresentativo("Presencia README");
        regla.setPonderacion(100);
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.ALTA);

        when(asignacionesService.consultarReglasDeAsignacion(idAsignacion)).thenReturn(List.of(regla));
        when(asignacionesService.consultarUmbralAprobacion(idAsignacion)).thenReturn(80.0);

        when(evaluadorFactory.obtenerEvaluador(TipoMotorEnum.PRESENCIA_ARCHIVO)).thenReturn(evaluadorRegla);
        when(evaluadorRegla.evaluar(anyString(), anyList())).thenReturn(false);

        // WHEN
        ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(dto);

        // THEN
        assertNotNull(resultado);
        assertEquals("NO_APROBADA", resultado.getDictamen());
        assertEquals(80.0, resultado.getUmbralAprobacion());
        assertEquals(0.0, resultado.getPorcentaje());
        verify(auditoriaDAO).registrarAuditoriaEnElHistorial(any(Auditoria.class));
    }
}
