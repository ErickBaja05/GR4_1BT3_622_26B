package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Suite para EP-01 >> FT-02 >> HU-01: Crear reglas para el curso.
 * Ciclo 1: CA3 - Escenario 3.1 (Validación de campos obligatorios).
 */
public class CreacionReglaCursoTest {

    private ValidadorRegla validadorRegla;

    @BeforeEach
    void setUp() {
        validadorRegla = new ValidadorRegla();
    }

    @Test
    @DisplayName("CA3 - Escenario 3.1: Given docente en formulario de regla, When omite campos obligatorios o curso, Then bloquea el registro e informa que todos los campos son obligatorios")
    void givenCamposVaciosOSinCurso_whenValidarRegla_thenLanzaExcepcionCamposObligatorios() {
        // Caso A: Sin curso (idCurso nulo)
        NuevaReglaDTO dtoSinCurso = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "Descripción", "README.md", "ALTA", 25);
        dtoSinCurso.setIdCurso(null);
        ExcepcionNegocio ex0 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinCurso)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex0.getMessage(), "Debe exigir curso obligatorio");

        // Caso B: Tipo de motor vacío
        NuevaReglaDTO dtoSinMotor = new NuevaReglaDTO("", "Exigir README", "Descripción", "README.md", "ALTA", 25);
        dtoSinMotor.setIdCurso(10L);
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinMotor)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex1.getMessage(), "Debe exigir tipo de motor obligatorio");

        // Caso C: Nombre representativo vacío o solo espacios
        NuevaReglaDTO dtoSinNombre = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "   ", "Descripción", "README.md", "ALTA", 25);
        dtoSinNombre.setIdCurso(10L);
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinNombre)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex2.getMessage(), "Debe exigir nombre representativo obligatorio");

        // Caso D: Descripción detallada vacía
        NuevaReglaDTO dtoSinDesc = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "", "README.md", "ALTA", 25);
        dtoSinDesc.setIdCurso(10L);
        ExcepcionNegocio ex3 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinDesc)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex3.getMessage(), "Debe exigir descripción obligatoria");

        // Caso E: Parámetro exacto vacío
        NuevaReglaDTO dtoSinParam = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "Descripción", "", "ALTA", 25);
        dtoSinParam.setIdCurso(10L);
        ExcepcionNegocio ex4 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinParam)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex4.getMessage(), "Debe exigir parámetro exacto obligatorio");
    }
}
