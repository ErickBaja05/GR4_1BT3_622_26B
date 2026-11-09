package com.gr4.owlaudit.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsignacionTest {

    @Test
    @DisplayName("El constructor vacío debe inicializar la lista de reglas vacía y no nula")
    void constructorVacioDebeInicializarListaReglas() {
        Asignacion asignacion = new Asignacion();

        assertNotNull(asignacion.getReglas(), "La colección de reglas no debe ser null");
        assertTrue(asignacion.getReglas().isEmpty(), "La colección de reglas debe iniciar vacía");
    }

    @ParameterizedTest(name = "Instanciar Asignacion con nombre=''{0}'' y umbral={1}%")
    @CsvSource({
            "Auditoría Sprint 1, 50.0",
            "Auditoría Arquitectura, 75.5",
            "Auditoría Entrega Final, 100.0"
    })
    @DisplayName("Debe construir Asignacion asignando nombre, umbral y reglas correctamente")
    void debeCrearAsignacionConParametrosValidos(String nombre, Double umbral) {
        Regla regla1 = new Regla();
        regla1.setId(1L);
        regla1.setNombreRepresentativo("Regla Test");
        regla1.setVigencia(EstadoVigencia.VIGENTE);

        Asignacion asignacion = new Asignacion(nombre, umbral, List.of(regla1));

        assertEquals(nombre, asignacion.getNombre());
        assertEquals(umbral, asignacion.getUmbralAprobacion());
        assertNotNull(asignacion.getReglas());
        assertEquals(1, asignacion.getReglas().size());
        assertEquals("Regla Test", asignacion.getReglas().get(0).getNombreRepresentativo());
    }

    @Test
    @DisplayName("getFechaLimiteFormateada debe retornar 'Sin fecha límite' cuando fechaLimite es null")
    void debeRetornarMensajePorDefectoCuandoFechaLimiteEsNula() {
        Asignacion asignacion = new Asignacion();
        asignacion.setFechaLimite(null);

        assertEquals("Sin fecha límite", asignacion.getFechaLimiteFormateada());
    }

    @Test
    @DisplayName("getFechaLimiteFormateada debe formatear correctamente fecha límite no nula")
    void debeFormatearFechaLimiteCorrectamente() {
        Asignacion asignacion = new Asignacion();
        asignacion.setFechaLimite(java.time.LocalDateTime.of(2026, 11, 15, 23, 59));

        assertEquals("15/11/2026 23:59", asignacion.getFechaLimiteFormateada());
    }
}
