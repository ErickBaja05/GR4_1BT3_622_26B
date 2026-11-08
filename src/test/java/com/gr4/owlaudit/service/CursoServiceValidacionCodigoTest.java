package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CursoServiceValidacionCodigoTest {

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private CursoServiceImpl cursoService;

    @ParameterizedTest(name = "El código \"{0}\" debe ser rechazado por formato inválido")
    @ValueSource(strings = {
            "CSW 2026!",
            "CSW 2026",
            "CSW_2026",
            "CSW#2026",
            "@CURSO-1",
            "CSW.2026B"
    })
    @DisplayName("CA2 - Escenario 2.2: Rechaza códigos con caracteres especiales o espacios intermedios")
    void crearCurso_cuandoCodigoTieneFormatoInvalido_lanzaExcepcionNegocio(String codigoInvalido) {
        NuevoCursoDTO dto = new NuevoCursoDTO("Construcción de Software", codigoInvalido, "Descripción del curso");

        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(dto, "correo@correo.com");
        });

        assertEquals("El código solo admite caracteres alfanuméricos y guiones", excepcion.getMessage());
        verifyNoInteractions(cursoDAO);
    }
}
