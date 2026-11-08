package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.ReglaAsignacionDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la fase roja (Test 🔴) de la HU-02.
 * Se verifica que la firma del método exista y que el comportamiento aún no esté implementado.
 */
@ExtendWith(MockitoExtension.class)
class AsignacionesServiceReglaTest {

    @Mock
    private com.gr4.owlaudit.dao.AsignacionesDAO asignacionesDAO;

    @Mock
    private com.gr4.owlaudit.dao.ReglaDAO reglaDAO;

    @InjectMocks
    private AsignacionesServiceImpl asignacionesService;

    @Test
    @DisplayName("Escenario exitoso: parametriza severidad y ponderación > 0")
    void asociarReglasAsignacion_Exitoso() {
        // Arrange
        ReglaAsignacionDTO dto = new ReglaAsignacionDTO(100L, NivelSeveridadEnum.ALTA, 10);
        List<ReglaAsignacionDTO> reglas = List.of(dto);
        // Act (expected to fail because logic not implemented yet)
        asignacionesService.asociarReglasAsignacion(1L, reglas);
        // No assertions: test will fail if an exception is thrown (red phase)
    }

    @Test
    @DisplayName("Escenario fallido: ponderación <= 0 debe lanzar IllegalArgumentException")
    void asociarReglasAsignacion_PonderacionInvalida() {
        // Arrange
        ReglaAsignacionDTO dto = new ReglaAsignacionDTO(101L, NivelSeveridadEnum.MEDIA, 0);
        List<ReglaAsignacionDTO> reglas = List.of(dto);
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            asignacionesService.asociarReglasAsignacion(2L, reglas);
        });
    }
}

