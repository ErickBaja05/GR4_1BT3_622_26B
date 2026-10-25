package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias de la entidad de dominio Regla y del enum EstadoVigencia
 * correspondientes a la HU-01 (Activar / Desactivar vigencia de reglas - ANDRES).
 */
class ReglaVigenciaTest {

    @Test
    @DisplayName("Una nueva regla debe inicializarse con estado VIGENTE por defecto")
    void regla_porDefecto_tieneEstadoVigente() {
        // GIVEN: Una nueva regla instanciada
        Regla regla = new Regla();

        // THEN: La vigencia por defecto debe ser VIGENTE
        assertEquals(EstadoVigencia.VIGENTE, regla.getVigencia(),
                "La regla recién creada debe poseer vigencia VIGENTE por defecto");
    }

    @Test
    @DisplayName("Una regla creada a partir de NuevaReglaDTO debe inicializarse con estado VIGENTE")
    void reglaDesdeDTO_porDefecto_tieneEstadoVigente() {
        // GIVEN: Un DTO válido de creación de regla
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Regla Archivo Base",
                "Verifica presencia de archivo", "README.md", "MEDIA", 15);

        // WHEN: Se instancia la regla a partir del DTO
        Regla regla = new Regla(dto);

        // THEN: La vigencia debe ser VIGENTE
        assertEquals(EstadoVigencia.VIGENTE, regla.getVigencia(),
                "La regla construida desde DTO debe poseer vigencia VIGENTE");
    }

    @Test
    @DisplayName("Debe permitir cambiar el estado de vigencia a NO_VIGENTE")
    void regla_cambiarVigenciaANoVigente_actualizaEstadoCorrectamente() {
        // GIVEN: Una regla existente
        Regla regla = new Regla();

        // WHEN: Se desactiva la regla asignando NO_VIGENTE
        regla.setVigencia(EstadoVigencia.NO_VIGENTE);

        // THEN: El estado retornado debe ser NO_VIGENTE
        assertEquals(EstadoVigencia.NO_VIGENTE, regla.getVigencia(),
                "El estado de la regla debe actualizarse a NO_VIGENTE");
    }

    @Test
    @DisplayName("Debe permitir reactivar el estado de vigencia a VIGENTE")
    void regla_reactivarVigenciaAVigente_actualizaEstadoCorrectamente() {
        // GIVEN: Una regla en estado NO_VIGENTE
        Regla regla = new Regla();
        regla.setVigencia(EstadoVigencia.NO_VIGENTE);

        // WHEN: Se reactiva la regla asignando VIGENTE
        regla.setVigencia(EstadoVigencia.VIGENTE);

        // THEN: El estado de la regla debe ser VIGENTE
        assertEquals(EstadoVigencia.VIGENTE, regla.getVigencia(),
                "El estado de la regla debe reactivarse a VIGENTE");
    }

    @Test
    @DisplayName("El enum EstadoVigencia debe contener únicamente los valores permitidos VIGENTE y NO_VIGENTE")
    void estadoVigencia_soloContieneValoresPermitidos() {
        // GIVEN / WHEN: Los valores definidos en el enumerado
        EstadoVigencia[] valores = EstadoVigencia.values();

        // THEN: Debe contener exactamente 2 valores: VIGENTE y NO_VIGENTE
        assertEquals(2, valores.length, "EstadoVigencia debe contener exactamente 2 valores");
        assertArrayEquals(new EstadoVigencia[]{EstadoVigencia.VIGENTE, EstadoVigencia.NO_VIGENTE}, valores,
                "Los únicos valores permitidos son VIGENTE y NO_VIGENTE");
    }

    @Test
    @DisplayName("Asignar un estado de vigencia nulo debe lanzar IllegalArgumentException")
    void regla_asignarVigenciaNula_lanzaExcepcion() {
        // GIVEN: Una regla existente
        Regla regla = new Regla();

        // WHEN / THEN: Asignar null debe ser rechazado
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            regla.setVigencia(null);
        }, "No se debe permitir asignar un estado de vigencia nulo a la regla");

        assertEquals("El estado de vigencia no puede ser nulo.", ex.getMessage());
    }
}
