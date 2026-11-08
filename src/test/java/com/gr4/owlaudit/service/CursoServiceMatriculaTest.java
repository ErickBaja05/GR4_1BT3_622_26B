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
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.ListadoEstudiantes;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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

    @Test
    void deberiaRechazarMatriculaCuandoEstudianteYaEstaInscritoEnElCurso() {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setCodigo("CSW-2026B");
        curso.setNombre("Construcción de Software");

        when(cursoDAO.buscarPorCodigo("CSW-2026B")).thenReturn(curso);
        when(cursoDAO.estaEstudianteMatriculado(1L, 10L)).thenReturn(true);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> cursoService.matricularEstudiantePorCodigo(10L, "CSW-2026B"));

        assertEquals("Ya te encuentras matriculado en el curso Construcción de Software", ex.getMessage());
        verify(cursoDAO, never()).registrarMatricula(any());
    }

    @Test
    void deberiaRegistrarMatriculaExitosamenteCuandoCodigoEsValidoYNoEstaInscrito() throws ExcepcionNegocio {
        Curso curso = new Curso();
        curso.setId(1L);
        curso.setCodigo("CSW-2026B");
        curso.setNombre("Construcción de Software");

        when(cursoDAO.buscarPorCodigo("CSW-2026B")).thenReturn(curso);
        when(cursoDAO.estaEstudianteMatriculado(1L, 10L)).thenReturn(false);

        Curso resultado = cursoService.matricularEstudiantePorCodigo(10L, " csw-2026b ");

        assertEquals("Construcción de Software", resultado.getNombre());

        ArgumentCaptor<ListadoEstudiantes> captor = ArgumentCaptor.forClass(ListadoEstudiantes.class);
        verify(cursoDAO).registrarMatricula(captor.capture());

        ListadoEstudiantes matriculaGuardada = captor.getValue();
        assertNotNull(matriculaGuardada.getId());
        assertEquals(1L, matriculaGuardada.getId().getIdCurso());
        assertEquals(10L, matriculaGuardada.getId().getIdEstudiante());
        assertEquals("ACTIVO", matriculaGuardada.getEstado());
        assertNotNull(matriculaGuardada.getFechaMatricula());
    }
}
