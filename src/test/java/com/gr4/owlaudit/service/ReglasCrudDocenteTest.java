package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReglasCrudDocenteTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private ValidadorRegla validadorRegla;

    private ReglaServiceImpl reglaService;

    private Regla reglaExistente;

    @BeforeEach
    void setUp() {
        reglaService = new ReglaServiceImpl(reglaDAO, validadorRegla);

        reglaExistente = new Regla();
        reglaExistente.setId(1L);
        reglaExistente.setNombreRepresentativo("Regla Original");
        reglaExistente.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaExistente.setParametroExacto("README.md");
        reglaExistente.setNivelSeveridad(NivelSeveridadEnum.MEDIA);
        reglaExistente.setPonderacion(15);
        reglaExistente.setVigencia(EstadoVigencia.VIGENTE);
    }

    @Test
    @DisplayName("Debe eliminar una regla existente correctamente")
    void given_reglaExistente_when_eliminarRegla_then_llamaDAOEliminar() {
        when(reglaDAO.buscarPorId(1L)).thenReturn(reglaExistente);

        reglaService.eliminarRegla(1L);

        verify(reglaDAO, times(1)).buscarPorId(1L);
        verify(reglaDAO, times(1)).eliminar(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción al intentar eliminar una regla inexistente")
    void given_reglaInexistente_when_eliminarRegla_then_lanzaExcepcion() {
        when(reglaDAO.buscarPorId(99L)).thenReturn(null);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> reglaService.eliminarRegla(99L));
        assertTrue(ex.getMessage().contains("No se encontró la regla"));
        verify(reglaDAO, never()).eliminar(anyLong());
    }

    @Test
    @DisplayName("Debe actualizar una regla existente con datos válidos")
    void given_datosValidos_when_actualizarRegla_then_modificaYGuardaEnDAO() {
        when(reglaDAO.buscarPorId(1L)).thenReturn(reglaExistente);

        NuevaReglaDTO dtoActualizado = new NuevaReglaDTO(
                "RESTRICCION_ARCHIVOS",
                "Regla Modificada",
                "Nueva descripción",
                ".env",
                "ALTA",
                25
        );
        when(reglaDAO.existePorNombre("Regla Modificada")).thenReturn(false);

        reglaService.actualizarRegla(1L, dtoActualizado);

        verify(validadorRegla, times(1)).validarSintaxisYFormato(dtoActualizado);
        verify(reglaDAO, times(1)).actualizar(reglaExistente);

        assertEquals("Regla Modificada", reglaExistente.getNombreRepresentativo());
        assertEquals(TipoMotorEnum.RESTRICCION_ARCHIVOS, reglaExistente.getTipoMotor());
        assertEquals(".env", reglaExistente.getParametroExacto());
        assertEquals(NivelSeveridadEnum.ALTA, reglaExistente.getNivelSeveridad());
        assertEquals(25, reglaExistente.getPonderacion());
    }

    @Test
    @DisplayName("Debe lanzar excepción al actualizar si la regla no existe")
    void given_reglaInexistente_when_actualizarRegla_then_lanzaExcepcion() {
        when(reglaDAO.buscarPorId(99L)).thenReturn(null);
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "file.txt", "BAJA", 10);

        assertThrows(ExcepcionNegocio.class, () -> reglaService.actualizarRegla(99L, dto));
        verify(reglaDAO, never()).actualizar(any());
    }

    @Test
    @DisplayName("Debe consultar regla por id")
    void given_idRegla_when_obtenerReglaPorId_then_retornaRegla() {
        when(reglaDAO.buscarPorId(1L)).thenReturn(reglaExistente);

        Regla r = reglaService.obtenerReglaPorId(1L);

        assertNotNull(r);
        assertEquals(1L, r.getId());
    }
}
