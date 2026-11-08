package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class CursoServiceMatriculaTest {

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    private CursoServiceImpl cursoService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cursoService = new CursoServiceImpl(cursoDAO, usuarioDAO);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "\t", "XXX-9999", "NO-EXISTE", "csw-9999" })
    void deberiaLanzarExcepcionCuandoCodigoCursoNoExisteOEsVacio(String codigoInvalido) {
        lenient().when(cursoDAO.buscarPorCodigo(anyString())).thenReturn(null);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> cursoService.matricularEstudiantePorCodigo(5L, codigoInvalido));

        assertEquals("No existe ningún curso registrado con el código ingresado", ex.getMessage());
        verify(cursoDAO, never()).registrarMatricula(any());
    }
}
