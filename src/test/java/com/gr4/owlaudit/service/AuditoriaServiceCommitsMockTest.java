package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceCommitsMockTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private ProyectoDAO proyectoDAO;

    private AuditoriaServiceImpl auditoriaService;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaServiceImpl(
                reglaDAO, auditoriaDAO, gitHubClient, new EvaluadorReglaFactory(), proyectoDAO);
    }

    @Test
    @DisplayName("Dado un repositorio con 80% de commits válidos cuando se confirma la ejecución de auditoría entonces cumple la regla y asigna el puntaje completo")
    void given_repository_with_80_percent_valid_commits_when_confirm_audit_execution_then_rule_passes_and_assigns_full_score() {
        // Given
        NuevaReglaDTO reglaDTO = new NuevaReglaDTO();
        reglaDTO.setTipoMotor("CONVENCION_COMMITS");
        reglaDTO.setNombreRepresentativo("Formato Conventional Commits");
        reglaDTO.setDescripcionDetallada("80% de commits deben cumplir el formato");
        reglaDTO.setParametroExacto("^(feat|fix|docs|refactor|test): .+");
        reglaDTO.setNivelSeveridad("MEDIA");
        reglaDTO.setPonderacion(20);
        Regla reglaCommits = new Regla(reglaDTO);

        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(List.of(reglaCommits));
        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString())).thenReturn(List.of("README.md"));

        List<String> commitsSimulados = List.of(
                "feat: agrega validacion de reglas",
                "fix: corrige nulo en servlet",
                "docs: actualiza readme del proyecto",
                "test: agrega prueba parametrizada",
                "subiendo cambios finales" // 4 de 5 válidos = 80% (CUMPLE)
        );
        when(gitHubClient.obtenerMensajesDeCommits(anyString(), eq(10))).thenReturn(commitsSimulados);

        AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
        auditoriaDTO.setProyectoId(1L);
        auditoriaDTO.setUrl("https://github.com/ErickBaja05/GR4_1BT3_622_26B");

        // When
        ResultadoDTO resultado = auditoriaService.confirmarEjecucionDeAuditoria(auditoriaDTO);

        // Then
        verify(gitHubClient).obtenerMensajesDeCommits("https://github.com/ErickBaja05/GR4_1BT3_622_26B", 10);
        assertEquals(20, resultado.getPuntajeObtenido());
        assertEquals(100.0, resultado.getPorcentaje());
        assertTrue(resultado.getDetalles().get(0).isCumple());
    }

    @Test
    @DisplayName("Dado un repositorio con menos del 80% de commits válidos cuando se confirma la ejecución de auditoría entonces incumple la regla y genera hallazgo")
    void given_repository_with_less_than_80_percent_valid_commits_when_confirm_audit_execution_then_rule_fails_and_generates_finding() {
        // Given
        NuevaReglaDTO reglaDTO = new NuevaReglaDTO();
        reglaDTO.setTipoMotor("CONVENCION_COMMITS");
        reglaDTO.setNombreRepresentativo("Formato Conventional Commits");
        reglaDTO.setDescripcionDetallada("80% de commits deben cumplir el formato");
        reglaDTO.setParametroExacto("^(feat|fix|docs|refactor|test): .+");
        reglaDTO.setNivelSeveridad("ALTA");
        reglaDTO.setPonderacion(20);
        Regla reglaCommits = new Regla(reglaDTO);

        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(List.of(reglaCommits));
        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString())).thenReturn(List.of("README.md"));

        List<String> commitsSimulados = List.of(
                "feat: agrega validacion de reglas",
                "cambios varios",
                "avance",
                "arreglando bug",
                "final final ahora si" // Solo 1 de 5 válido = 20% < 80% (NO CUMPLE)
        );
        when(gitHubClient.obtenerMensajesDeCommits(anyString(), eq(10))).thenReturn(commitsSimulados);

        AuditoriaDTO auditoriaDTO = new AuditoriaDTO();
        auditoriaDTO.setProyectoId(1L);
        auditoriaDTO.setUrl("https://github.com/ErickBaja05/GR4_1BT3_622_26B");

        // When
        ResultadoDTO resultado = auditoriaService.confirmarEjecucionDeAuditoria(auditoriaDTO);

        // Then
        verify(gitHubClient).obtenerMensajesDeCommits("https://github.com/ErickBaja05/GR4_1BT3_622_26B", 10);
        assertEquals(0, resultado.getPuntajeObtenido());
        assertEquals(0.0, resultado.getPorcentaje());
        assertFalse(resultado.getDetalles().get(0).isCumple());
        assertNotNull(resultado.getDetalles().get(0).getEvidencia());
    }
}
