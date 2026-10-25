package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.EstadoVigencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReglasServiceCambioEstadoTest {

    @Mock
    private ReglaDAOImpl reglaDAO;

    @InjectMocks
    private ReglaServiceImpl reglaService;

    private Regla reglaExistente;

    @BeforeEach
    void setUp() {
        // Preparamos una regla base en estado VIGENTE antes de cada test
        reglaExistente = new Regla();
        reglaExistente.setNombreRepresentativo("Complejidad Ciclomática");
        reglaExistente.setVigencia(EstadoVigencia.VIGENTE);
    }

    @Test
    @DisplayName("Debe cambiar el estado de vigencia cuando el nuevo estado es distinto al actual")
    void given_reglaVigente_when_cambiarEstadoANoVigente_then_actualizaYRetornaTrue() {
        // Arrange (Preparación)
        String nombreRegla = "Complejidad Ciclomática";
        EstadoVigencia nuevoEstado = EstadoVigencia.NO_VIGENTE;

        // 1. Necesitamos obtener el objeto Regla para que el Service pueda leer su
        // getVigencia() actual
        when(reglaDAO.obtenerPorNombre(nombreRegla)).thenReturn(reglaExistente);
        // 2. Simulamos que el UPDATE en la base de datos es exitoso
        when(reglaDAO.actualizarEstadoVigencia(nombreRegla, nuevoEstado)).thenReturn(true);

        // Act (Acción - Fase RED)
        boolean resultado = reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, nuevoEstado);

        // Assert (Verificación)
        assertTrue(resultado, "El cambio de estado debería retornar true al ser válido");
        assertEquals(EstadoVigencia.NO_VIGENTE, reglaExistente.getVigencia());

        verify(reglaDAO, times(1)).obtenerPorNombre(nombreRegla);
        verify(reglaDAO, times(1)).actualizarEstadoVigencia(nombreRegla, nuevoEstado);
    }

    @Test
    @DisplayName("Escenario 1.3: No debe actualizar el estado cuando el estado solicitado es igual al actual")
    void given_reglaVigente_when_cambiarAlMismoEstadoVigente_then_noActualizaYRetornaFalse() {
        // Arrange (Preparación)
        String nombreRegla = "Complejidad Ciclomática";
        EstadoVigencia mismoEstado = EstadoVigencia.VIGENTE;

        // Devuelve la regla que ya está en estado VIGENTE
        when(reglaDAO.obtenerPorNombre(nombreRegla)).thenReturn(reglaExistente);

        // Act (Acción - Fase RED)
        boolean resultado = reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, mismoEstado);

        // Assert (Verificación)
        assertFalse(resultado, "No debe permitir el cambio si el estado solicitado es igual al actual");

        // Se consultó la regla para ver su estado, pero NUNCA se llamó al UPDATE
        verify(reglaDAO, times(1)).obtenerPorNombre(nombreRegla);
        verify(reglaDAO, never()).actualizarEstadoVigencia(anyString(), any(EstadoVigencia.class));
    }
}
