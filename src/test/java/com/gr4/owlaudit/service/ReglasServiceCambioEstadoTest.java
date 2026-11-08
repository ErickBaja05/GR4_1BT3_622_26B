package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.EstadoVigencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReglasServiceCambioEstadoTest {

    private static final Long ID_REGLA = 5L;
    private static final Long ID_CURSO = 1L;
    private static final Long ID_DOCENTE_TITULAR = 7L;
    private static final Long ID_DOCENTE_AJENO = 99L;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private CursoDAO cursoDAO;

    private ReglaServiceImpl reglaService;

    private ReglaServiceImpl reglaServiceConCurso;

    private Regla reglaExistente;

    @BeforeEach
    void setUp() {
        // Inyectamos explícitamente el mock usando el constructor de 1 parámetro
        reglaService = new ReglaServiceImpl(reglaDAO);
        reglaServiceConCurso = new ReglaServiceImpl(reglaDAO, cursoDAO, new ValidadorRegla());

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

        when(reglaDAO.obtenerPorNombre(nombreRegla)).thenReturn(reglaExistente);
        when(reglaDAO.actualizarEstadoVigencia(nombreRegla, nuevoEstado)).thenReturn(true);

        // Act (Acción)
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

        when(reglaDAO.obtenerPorNombre(nombreRegla)).thenReturn(reglaExistente);

        // Act (Acción)
        boolean resultado = reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, mismoEstado);

        // Assert (Verificación)
        assertFalse(resultado, "No debe permitir el cambio si el estado solicitado es igual al actual");

        verify(reglaDAO, times(1)).obtenerPorNombre(nombreRegla);
        verify(reglaDAO, never()).actualizarEstadoVigencia(anyString(), any(EstadoVigencia.class));
    }

    /** Regla VIGENTE del curso cuyo titular es ID_DOCENTE_TITULAR. Lenient: no todos los rechazos llegan a consultarla. */
    private void prepararReglaDelCursoConDocenteTitular() {
        Regla regla = new Regla();
        regla.setId(ID_REGLA);
        regla.setVigencia(EstadoVigencia.VIGENTE);

        Docente titular = new Docente();
        titular.setId(ID_DOCENTE_TITULAR);
        Curso curso = new Curso();
        curso.setId(ID_CURSO);
        curso.setDocente(titular);

        lenient().when(reglaDAO.obtenerPorIdYCurso(ID_REGLA, ID_CURSO)).thenReturn(regla);
        lenient().when(cursoDAO.buscarPorId(ID_CURSO)).thenReturn(curso);
        lenient().when(reglaDAO.actualizarDescripcionYVigencia(any(), any(), any(), any())).thenReturn(true);
    }

    private void verificarQueNoSeActualizo() {
        verify(reglaDAO, never()).actualizarDescripcionYVigencia(any(), any(), any(), any());
    }

    @Test
    @DisplayName("HU-02 CA1: guarda la nueva descripción y vigencia de la regla identificada por id y curso")
    void modificarDescripcionYVigencia_guardaPorIdYCurso() {
        prepararReglaDelCursoConDocenteTitular();

        reglaServiceConCurso.modificarDescripcionYVigencia(
                ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, "Nueva descripción", EstadoVigencia.NO_VIGENTE);

        verify(reglaDAO).actualizarDescripcionYVigencia(ID_REGLA, ID_CURSO, "Nueva descripción", EstadoVigencia.NO_VIGENTE);
        verify(reglaDAO, never()).actualizarEstadoVigencia(anyString(), any(EstadoVigencia.class));
    }

    @Test
    @DisplayName("HU-02 CA1: permite cambiar solo la descripción manteniendo la misma vigencia")
    void modificarDescripcionYVigencia_guardaAunqueLaVigenciaNoCambie() {
        prepararReglaDelCursoConDocenteTitular();

        reglaServiceConCurso.modificarDescripcionYVigencia(
                ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, "Solo cambia el texto", EstadoVigencia.VIGENTE);

        verify(reglaDAO).actualizarDescripcionYVigencia(ID_REGLA, ID_CURSO, "Solo cambia el texto", EstadoVigencia.VIGENTE);
    }

    @ParameterizedTest(name = "Descripción inválida: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU-02 CA3: rechaza una descripción vacía y conserva la anterior")
    void modificarDescripcionYVigencia_rechazaDescripcionVacia(String descripcionInvalida) {
        prepararReglaDelCursoConDocenteTitular();

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaServiceConCurso.modificarDescripcionYVigencia(
                        ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, descripcionInvalida, EstadoVigencia.VIGENTE));

        assertEquals("La descripción detallada de la regla no puede quedar vacía", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02: rechaza una descripción que supera los 200 caracteres permitidos")
    void modificarDescripcionYVigencia_rechazaDescripcionDemasiadoLarga() {
        prepararReglaDelCursoConDocenteTitular();
        String descripcionLarga = "x".repeat(201);

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaServiceConCurso.modificarDescripcionYVigencia(
                        ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, descripcionLarga, EstadoVigencia.VIGENTE));

        assertEquals("La descripción detallada debe tener entre 1 y 200 caracteres.", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02 Task 5.1: rechaza modificar una regla que no pertenece al curso indicado")
    void modificarDescripcionYVigencia_rechazaReglaDeOtroCurso() {
        prepararReglaDelCursoConDocenteTitular();
        when(reglaDAO.obtenerPorIdYCurso(ID_REGLA, ID_CURSO)).thenReturn(null);

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaServiceConCurso.modificarDescripcionYVigencia(
                        ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, "Descripción válida", EstadoVigencia.VIGENTE));

        assertEquals("La regla no pertenece al curso especificado o no existe.", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02 Task 5.1: rechaza que un docente no titular modifique reglas del curso")
    void modificarDescripcionYVigencia_rechazaDocenteNoTitular() {
        prepararReglaDelCursoConDocenteTitular();

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaServiceConCurso.modificarDescripcionYVigencia(
                        ID_REGLA, ID_CURSO, ID_DOCENTE_AJENO, "Descripción válida", EstadoVigencia.VIGENTE));

        assertEquals("No tiene permisos para gestionar un curso del que no es docente titular.", error.getMessage());
        verificarQueNoSeActualizo();
    }
}
