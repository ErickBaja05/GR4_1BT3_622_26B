package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DictamenSeveridadTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private GitHubClient gitHubClient;

    @Spy
    private EvaluadorReglaFactory evaluadorFactory = new EvaluadorReglaFactory();

    @InjectMocks
    private AuditoriaServiceImpl auditoriaService;

    @Test
    @DisplayName("Debe reprobar la auditoría cuando el puntaje es >= 70% pero existe al menos un hallazgo de severidad ALTA")
    void dictamenFinal_fallaPorSeveridadAlta_aPesarDeSuperarUmbralPuntaje() {
        // GIVEN:
        // Regla 1 (Cumplida, ponderación 85, severidad BAJA)
        Regla reglaAprobada = new Regla();
        reglaAprobada.setNombreRepresentativo("Archivo README");
        reglaAprobada.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaAprobada.setParametroExacto("README.md");
        reglaAprobada.setPonderacion(85);
        reglaAprobada.setNivelSeveridad(NivelSeveridadEnum.BAJA);
        reglaAprobada.setActiva(true);

        // Regla 2 (Incumplida, ponderación 15, severidad ALTA)
        Regla reglaFallaAlta = new Regla();
        reglaFallaAlta.setNombreRepresentativo("Sin archivos sensibles");
        reglaFallaAlta.setTipoMotor(TipoMotorEnum.RESTRICCION_ARCHIVOS);
        reglaFallaAlta.setParametroExacto(".env");
        reglaFallaAlta.setPonderacion(15);
        reglaFallaAlta.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        reglaFallaAlta.setActiva(true);

        // Mockeamos la consulta de reglas activas del DAO
        when(reglaDAO.consultarReglasDeEvaluacionVigentes())
                .thenReturn(List.of(reglaAprobada, reglaFallaAlta));

        // Mockeamos la respuesta de la estructura de GitHub (contiene README.md y el archivo prohibido .env)
        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString()))
                .thenReturn(List.of("README.md", ".env"));

        AuditoriaDTO dto = new AuditoriaDTO(1L, "https://github.com/usuario/repo-demo");

        // WHEN: Se ejecuta la auditoría a través de la capa de servicio (Flujo N-Capas)
        ResultadoDTO resultado = auditoriaService.confirmarEjecucionDeAuditoria(dto);

        // THEN:
        // 1. El puntaje obtenido es 85/100 (85.0%), superando el umbral mínimo del 70%
        assertEquals(85.0, resultado.getPorcentaje());

        // 2. Sin embargo, el dictamen devuelto al Servlet/Vista DEBE ser "NO APROBADA" por tener hallazgo ALTO
        assertEquals("NO APROBADA", resultado.getDictamen(),
                "La auditoría debe resultar NO APROBADA si contiene al menos un hallazgo de severidad ALTA");
    }
}
