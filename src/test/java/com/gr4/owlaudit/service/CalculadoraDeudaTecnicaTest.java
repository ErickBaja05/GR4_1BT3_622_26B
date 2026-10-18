package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.DeudaTecnicaDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * [FASE ROJA - TDD] Pruebas unitarias para CalculadoraDeudaTecnica.
 *
 * Reglas de negocio:
 *   - Hallazgo ALTA  --> 120 minutos (2 horas)
 *   - Hallazgo MEDIA -->  60 minutos (1 hora)
 *   - Hallazgo BAJA  -->  30 minutos
 *
 * ESTAS PRUEBAS DEBEN FALLAR al ejecutar 'mvn test' porque las clases
 * CalculadoraDeudaTecnica y DeudaTecnicaDTO aun no existen en src/main.
 *
 * Commit esperado: TEST: agrega prueba fallida para funcionalidad_calculadora_deuda_tecnica
 */
@DisplayName("CU-DEUDA: Calculadora de Deuda Tecnica Estimada")
class CalculadoraDeudaTecnicaTest {

    // =========================================================
    // SUT (System Under Test) -- aun no existe en src/main --> RED
    // =========================================================
    private CalculadoraDeudaTecnica calculadora;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraDeudaTecnica();
    }

    // =========================================================
    // TC-01: Lista vacia -- sin hallazgos, deuda = 0
    // =========================================================
    @Test
    @DisplayName("TC-01 | Lista vacia -> deuda total de 0 minutos")
    void calcularConListaVacia() {
        DeudaTecnicaDTO resultado = calculadora.calcular(Collections.emptyList());

        assertNotNull(resultado, "El DTO no debe ser nulo aunque no haya hallazgos.");
        assertEquals(0, resultado.getTotalMinutos(),
                "Con lista vacia, la deuda total debe ser 0 minutos.");
        assertEquals(0, resultado.getCantidadAlta(),
                "No debe haber hallazgos de severidad ALTA.");
        assertEquals(0, resultado.getCantidadMedia(),
                "No debe haber hallazgos de severidad MEDIA.");
        assertEquals(0, resultado.getCantidadBaja(),
                "No debe haber hallazgos de severidad BAJA.");
    }

    // =========================================================
    // TC-02: Un solo hallazgo ALTA -> 120 min
    // =========================================================
    @Test
    @DisplayName("TC-02 | 1 hallazgo ALTA -> 120 minutos (2h 0min)")
    void calcularConUnHallazgoAlta() {
        List<NivelSeveridadEnum> hallazgos = List.of(NivelSeveridadEnum.ALTA);

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals(120, resultado.getTotalMinutos(),
                "1 hallazgo ALTA debe sumar exactamente 120 minutos.");
        assertEquals(1, resultado.getCantidadAlta(),
                "Debe contabilizarse 1 hallazgo de severidad ALTA.");
        assertEquals(0, resultado.getCantidadMedia());
        assertEquals(0, resultado.getCantidadBaja());
    }

    // =========================================================
    // TC-03: Un solo hallazgo MEDIA -> 60 min
    // =========================================================
    @Test
    @DisplayName("TC-03 | 1 hallazgo MEDIA -> 60 minutos (1h 0min)")
    void calcularConUnHallazgoMedia() {
        List<NivelSeveridadEnum> hallazgos = List.of(NivelSeveridadEnum.MEDIA);

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals(60, resultado.getTotalMinutos(),
                "1 hallazgo MEDIA debe sumar exactamente 60 minutos.");
        assertEquals(0, resultado.getCantidadAlta());
        assertEquals(1, resultado.getCantidadMedia(),
                "Debe contabilizarse 1 hallazgo de severidad MEDIA.");
        assertEquals(0, resultado.getCantidadBaja());
    }

    // =========================================================
    // TC-04: Un solo hallazgo BAJA -> 30 min
    // =========================================================
    @Test
    @DisplayName("TC-04 | 1 hallazgo BAJA -> 30 minutos (0h 30min)")
    void calcularConUnHallazgoBaja() {
        List<NivelSeveridadEnum> hallazgos = List.of(NivelSeveridadEnum.BAJA);

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals(30, resultado.getTotalMinutos(),
                "1 hallazgo BAJA debe sumar exactamente 30 minutos.");
        assertEquals(0, resultado.getCantidadAlta());
        assertEquals(0, resultado.getCantidadMedia());
        assertEquals(1, resultado.getCantidadBaja(),
                "Debe contabilizarse 1 hallazgo de severidad BAJA.");
    }

    // =========================================================
    // TC-05: Mix de hallazgos -- calculo acumulado
    //   1 ALTA (120) + 2 MEDIA (120) + 3 BAJA (90) = 330 min
    // =========================================================
    @Test
    @DisplayName("TC-05 | Mix: 1 ALTA + 2 MEDIA + 3 BAJA -> 330 minutos (5h 30min)")
    void calcularConHallazgosMixtos() {
        List<NivelSeveridadEnum> hallazgos = List.of(
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.MEDIA,
                NivelSeveridadEnum.MEDIA,
                NivelSeveridadEnum.BAJA,
                NivelSeveridadEnum.BAJA,
                NivelSeveridadEnum.BAJA
        );

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals(330, resultado.getTotalMinutos(),
                "1 ALTA + 2 MEDIA + 3 BAJA = 120 + 120 + 90 = 330 minutos.");
        assertEquals(1, resultado.getCantidadAlta());
        assertEquals(2, resultado.getCantidadMedia());
        assertEquals(3, resultado.getCantidadBaja());
    }

    // =========================================================
    // TC-06: Verificar el formato legible de la deuda
    //   Formato esperado: "Xh Ymin"
    // =========================================================
    @Test
    @DisplayName("TC-06 | Formato de salida legible: '5h 30min' para 330 minutos")
    void formatoDeudaLegibleParaMixto() {
        List<NivelSeveridadEnum> hallazgos = List.of(
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.MEDIA,
                NivelSeveridadEnum.MEDIA,
                NivelSeveridadEnum.BAJA,
                NivelSeveridadEnum.BAJA,
                NivelSeveridadEnum.BAJA
        );

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals("5h 30min", resultado.getTiempoFormateado(),
                "330 minutos deben formatearse como '5h 30min'.");
    }

    // =========================================================
    // TC-07: Formato para tiempos exactos en horas
    // =========================================================
    @Test
    @DisplayName("TC-07 | Formato '2h 0min' para exactamente 120 minutos")
    void formatoDeudaExactaEnHoras() {
        List<NivelSeveridadEnum> hallazgos = List.of(NivelSeveridadEnum.ALTA);

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals("2h 0min", resultado.getTiempoFormateado(),
                "120 minutos deben formatearse como '2h 0min'.");
    }

    // =========================================================
    // TC-08: El resumen textual debe contener informacion de todos los niveles
    // =========================================================
    @Test
    @DisplayName("TC-08 | El resumen textual incluye informacion de los tres niveles")
    void resumenTextualContieneInfoDeTodosLosNiveles() {
        List<NivelSeveridadEnum> hallazgos = List.of(
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.MEDIA,
                NivelSeveridadEnum.BAJA
        );

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);
        String resumen = resultado.getResumenTexto();

        assertNotNull(resumen, "El resumen textual no debe ser nulo.");
        assertTrue(resumen.contains("ALTA"),
                "El resumen debe mencionar los hallazgos de severidad ALTA.");
        assertTrue(resumen.contains("MEDIA"),
                "El resumen debe mencionar los hallazgos de severidad MEDIA.");
        assertTrue(resumen.contains("BAJA"),
                "El resumen debe mencionar los hallazgos de severidad BAJA.");
        assertTrue(resumen.contains("3h 30min"),
                "El resumen debe incluir el tiempo total formateado (3h 30min).");
    }

    // =========================================================
    // TC-09: Multiples hallazgos solo de severidad ALTA
    // =========================================================
    @Test
    @DisplayName("TC-09 | 5 hallazgos ALTA -> 600 minutos (10h 0min)")
    void calcularVariosHallazgosAlta() {
        List<NivelSeveridadEnum> hallazgos = List.of(
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.ALTA,
                NivelSeveridadEnum.ALTA
        );

        DeudaTecnicaDTO resultado = calculadora.calcular(hallazgos);

        assertEquals(600, resultado.getTotalMinutos(),
                "5 hallazgos ALTA deben sumar 600 minutos.");
        assertEquals("10h 0min", resultado.getTiempoFormateado(),
                "600 minutos deben formatearse como '10h 0min'.");
        assertEquals(5, resultado.getCantidadAlta());
    }
}
