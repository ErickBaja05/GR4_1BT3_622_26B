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
```

---

## 🟢 FASE 2: VERDE (Código Mínimo para Pasar la Prueba)

### 1. Solución implementada
Se implementó el código indispensable para satisfacer la nueva regla de seguridad:
- En la entidad de dominio `Auditoria`, se implementó la lógica en el método `determinarDictamen()`:
  1. Si `porcentaje < 70.0`, el dictamen es `"NO APROBADA"`.
  2. Si existe al menos un `Hallazgo` en los resultados con severidad `NivelSeveridadEnum.ALTA`, el dictamen es `"NO APROBADA"`.
  3. En caso contrario, el dictamen es `"APROBADA"`.
- En el servicio `AuditoriaServiceImpl`, durante la ejecución de la auditoría (`confirmarEjecucionDeAuditoria`), se asigna el dictamen calculado al `ResultadoDTO` retornado.
- En la vista JSP (`evaluarRepositorio.jsp`), se visualiza el dictamen final con insignias de color.

### 2. Ejecución y resultado (Barra Verde)
- **Comando:** `mvn test -Dtest=DictamenSeveridadTest`
- **Resultado obtenido:**
```text
[INFO] Running com.gr4.owlaudit.service.DictamenSeveridadTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.810 s -- in com.gr4.owlaudit.service.DictamenSeveridadTest
[INFO] Results:
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 🔵 FASE 3: REFACTOR (Optimización y Buenas Prácticas)

### 1. Diagnóstico de Olores de Código Identificados
1. **Magic Literals / Numbers:** Uso de números mágicos (`70.0`) y cadenas duplicadas (`"APROBADA"`, `"NO APROBADA"`) dispersas en el código.
2. **Método con Múltiples Responsabilidades / Bucle imperativo anidado:** Verificación manual de la lista con múltiples niveles de anidamiento condicional (`for` e `if`).

### 2. Patrones de Refactorización Aplicados (Catálogo Martin Fowler)
1. **Replace Magic Literal with Symbolic Constant:**
   Se crearon constantes autodescriptivas en `Auditoria.java`:
   - `UMBRAL_MINIMO_APROBACION = 70.0`
   - `DICTAMEN_APROBADA = "APROBADA"`
   - `DICTAMEN_NO_APROBADA = "NO APROBADA"`
2. **Extract Method + Substitute Algorithm (Java Stream API):**
   Se extrajo la lógica de búsqueda de severidad ALTA a un método privado `tieneHallazgosDeSeveridadAlta()`, reemplazando el bucle imperativo por operaciones declarativas y seguras ante nulos (`map`, `filter`, `anyMatch`).

### 3. Código Antes vs. Código Después

#### 🔴 ANTES (Fase Verde - Código Mínimo):
```java
public String determinarDictamen() {
    if (this.porcentaje < 70.0) {
        return "NO APROBADA";
    }
    if (this.resultados != null) {
        for (ResultadoRegla r : this.resultados) {
            if (r.getHallazgo() != null && r.getHallazgo().getNivelSeveridad() == NivelSeveridadEnum.ALTA) {
                return "NO APROBADA";
            }
        }
    }
    return "APROBADA";
}
```

#### 🔵 DESPUÉS (Fase Refactor - Clean Code & Streams):
```java
public static final double UMBRAL_MINIMO_APROBACION = 70.0;
public static final String DICTAMEN_APROBADA = "APROBADA";
public static final String DICTAMEN_NO_APROBADA = "NO APROBADA";

public String determinarDictamen() {
    if (this.porcentaje < UMBRAL_MINIMO_APROBACION || tieneHallazgosDeSeveridadAlta()) {
        return DICTAMEN_NO_APROBADA;
    }
    return DICTAMEN_APROBADA;
}

private boolean tieneHallazgosDeSeveridadAlta() {
    if (this.resultados == null) {
        return false;
    }
    return this.resultados.stream()
            .map(ResultadoRegla::getHallazgo)
            .filter(Objects::nonNull)
            .anyMatch(hallazgo -> hallazgo.getNivelSeveridad() == NivelSeveridadEnum.ALTA);
}
```

### 4. Verificación de No Regresión (Barra Verde Continua)
- **Comando:** `mvn test`
- **Resultado:**
```text
[INFO] Results:
[INFO] 
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
Todas las 46 pruebas unitarias y de integración pasan con éxito, garantizando que la refactorización preservó el 100% del comportamiento externo del sistema.
