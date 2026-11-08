package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CursoServiceCamposObligatoriosTest {

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private CursoServiceImpl cursoService;

    @ParameterizedTest(name = "[{index}] nombre=''{0}'', codigo=''{1}'', descripcion=''{2}''")
    @CsvSource(nullValues = { "NULL" }, value = {
            // Casos donde el nombre es inválido
            "NULL,               CSW-2026B,  Descripción válida",
            "'',                 CSW-2026B,  Descripción válida",
            "'   ',              CSW-2026B,  Descripción válida",
            // Casos donde el código es inválido
            "Construcción SW,    NULL,       Descripción válida",
            "Construcción SW,    '',         Descripción válida",
            "Construcción SW,    '   ',      Descripción válida",
            // Casos donde la descripción es inválida
            "Construcción SW,    CSW-2026B,  NULL",
            "Construcción SW,    CSW-2026B,  ''",
            "Construcción SW,    CSW-2026B,  '   '"
    })
    @DisplayName("CA3 - Escenario 3.1: Rechaza creación si algún campo obligatorio está ausente o vacío")
    void crearCurso_cuandoFaltaCampoObligatorio_lanzaExcepcionNegocio(String nombre, String codigo,
            String descripcion) {
        NuevoCursoDTO dto = new NuevoCursoDTO(nombre, codigo, descripcion);

        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(dto, "correo@correo.com");
        });

        assertEquals("Todos los campos (nombre, código y descripción) son obligatorios para crear el curso",
                excepcion.getMessage());
        verifyNoInteractions(cursoDAO);
    }

    @Test
    @DisplayName("CA3 - Escenario 3.1: Rechaza creación si el objeto DTO es completamente nulo")
    void crearCurso_cuandoDtoEsNulo_lanzaExcepcionNegocio() {
        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(null, "correo@correo.com");
        });

        assertEquals("Todos los campos (nombre, código y descripción) son obligatorios para crear el curso",
                excepcion.getMessage());
        verifyNoInteractions(cursoDAO);
    }
}
