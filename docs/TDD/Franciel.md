# Registro de Evidencias TDD - Funcionalidad: Dictamen final con fallo por severidad ALTA
**Desarrollador:** Franciel  
**Fecha:** 16/10/2026  

---

## 🔴 FASE 1: ROJO (Test Fallido por Aserción)

### 1. Objetivo de la prueba
Diseñar una prueba unitaria en la capa de servicios (`AuditoriaService`) usando **Mockito** que valide la regla de negocio de seguridad en una arquitectura multicapa (N-Tier): una auditoría no puede resultar aprobada en su `ResultadoDTO` si presenta al menos un hallazgo de severidad ALTA (`NivelSeveridadEnum.ALTA`), a pesar de haber obtenido un puntaje igual o superior al umbral mínimo del 70%.

### 2. Archivo de prueba creado
- **Ruta:** `src/test/java/com/gr4/owlaudit/service/DictamenSeveridadTest.java`
- **Capa:** Capa de Servicio (`com.gr4.owlaudit.service`)
- **Método de prueba:** `dictamenFinal_fallaPorSeveridadAlta_aPesarDeSuperarUmbralPuntaje()`

### 3. Código del Test (con Mockito)
```java
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

        when(reglaDAO.consultarReglasDeEvaluacionVigentes())
                .thenReturn(List.of(reglaAprobada, reglaFallaAlta));

        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString()))
                .thenReturn(List.of("README.md", ".env"));

        AuditoriaDTO dto = new AuditoriaDTO(1L, "https://github.com/usuario/repo-demo");

        // WHEN: Se ejecuta la auditoría a través del servicio
        ResultadoDTO resultado = auditoriaService.confirmarEjecucionDeAuditoria(dto);

        // THEN:
        // El porcentaje alcanza 85.0% (supera el 70%)
        assertEquals(85.0, resultado.getPorcentaje());

        // Pero el dictamen debe ser NO APROBADA debido al hallazgo de severidad ALTA
        assertEquals("NO APROBADA", resultado.getDictamen(),
                "La auditoría debe resultar NO APROBADA si contiene al menos un hallazgo de severidad ALTA");
    }
}
```

### 4. Ejecución y resultado (Barra Roja por fallo de aserción)
- **Comando:** `mvn test -Dtest=DictamenSeveridadTest`
- **Resultado en JUnit:**
```text
[INFO] Running com.gr4.owlaudit.service.DictamenSeveridadTest
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 1.834 s <<< FAILURE! -- in com.gr4.owlaudit.service.DictamenSeveridadTest
[ERROR] com.gr4.owlaudit.service.DictamenSeveridadTest.dictamenFinal_fallaPorSeveridadAlta_aPesarDeSuperarUmbralPuntaje -- Time elapsed: 1.769 s <<< FAILURE!
org.opentest4j.AssertionFailedError: La auditoría debe resultar NO APROBADA si contiene al menos un hallazgo de severidad ALTA ==> 
Expected :NO APROBADA
Actual   :null
	at org.junit.jupiter.api.Assertions.assertEquals(Assertions.java:1156)
	at com.gr4.owlaudit.service.DictamenSeveridadTest.dictamenFinal_fallaPorSeveridadAlta_aPesarDeSuperarUmbralPuntaje(DictamenSeveridadTest.java:84)
[INFO] 
[INFO] Results:
[INFO] 
[ERROR] Failures: 
[ERROR]   DictamenSeveridadTest.dictamenFinal_fallaPorSeveridadAlta_aPesarDeSuperarUmbralPuntaje:84 
          La auditoría debe resultar NO APROBADA si contiene al menos un hallazgo de severidad ALTA ==> expected: <NO APROBADA> but was: <null>
[INFO] 
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
```

### 5. Análisis del fallo
- El código compila al 100% gracias a que se incluyó la firma base vacía en `ResultadoDTO` (`getDictamen()` devuelve `null`) y en `Auditoria` (`determinarDictamen()` devuelve `null`).
- No existe lógica de negocio implementada en esta fase.
- La prueba corre en JUnit y falla con **Barra Roja** por aserción incumplida (`expected: <NO APROBADA> but was: <null>`), cumpliendo estrictamente con el ciclo Red-Green-Refactor.
