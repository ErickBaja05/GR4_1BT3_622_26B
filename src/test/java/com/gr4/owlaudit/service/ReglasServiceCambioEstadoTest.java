package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.ModificacionReglaDTO;
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

    @BeforeEach
    void setUp() {
        reglaService = new ReglaServiceImpl(reglaDAO, cursoDAO, new ValidadorRegla());
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

    private ModificacionReglaDTO modificacionDelTitular(String descripcion, EstadoVigencia vigencia) {
        return new ModificacionReglaDTO(ID_REGLA, ID_CURSO, ID_DOCENTE_TITULAR, descripcion, vigencia);
    }

    private void verificarQueNoSeActualizo() {
        verify(reglaDAO, never()).actualizarDescripcionYVigencia(any(), any(), any(), any());
    }

    @Test
    @DisplayName("HU-02 CA1: guarda la nueva descripción y vigencia de la regla identificada por id y curso")
    void modificarDescripcionYVigencia_guardaPorIdYCurso() {
        prepararReglaDelCursoConDocenteTitular();

        reglaService.modificarDescripcionYVigencia(modificacionDelTitular("Nueva descripción", EstadoVigencia.NO_VIGENTE));

        verify(reglaDAO).actualizarDescripcionYVigencia(ID_REGLA, ID_CURSO, "Nueva descripción", EstadoVigencia.NO_VIGENTE);
    }

    @Test
    @DisplayName("HU-02 CA1: permite cambiar solo la descripción manteniendo la misma vigencia")
    void modificarDescripcionYVigencia_guardaAunqueLaVigenciaNoCambie() {
        prepararReglaDelCursoConDocenteTitular();

        reglaService.modificarDescripcionYVigencia(modificacionDelTitular("Solo cambia el texto", EstadoVigencia.VIGENTE));

        verify(reglaDAO).actualizarDescripcionYVigencia(ID_REGLA, ID_CURSO, "Solo cambia el texto", EstadoVigencia.VIGENTE);
    }

    @ParameterizedTest(name = "Descripción inválida: \"{0}\"")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU-02 CA3: rechaza una descripción vacía y conserva la anterior")
    void modificarDescripcionYVigencia_rechazaDescripcionVacia(String descripcionInvalida) {
        prepararReglaDelCursoConDocenteTitular();

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaService.modificarDescripcionYVigencia(modificacionDelTitular(descripcionInvalida, EstadoVigencia.VIGENTE)));

        assertEquals("La descripción detallada de la regla no puede quedar vacía", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02: rechaza una descripción que supera los 200 caracteres permitidos")
    void modificarDescripcionYVigencia_rechazaDescripcionDemasiadoLarga() {
        prepararReglaDelCursoConDocenteTitular();
        String descripcionLarga = "x".repeat(201);

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaService.modificarDescripcionYVigencia(modificacionDelTitular(descripcionLarga, EstadoVigencia.VIGENTE)));

        assertEquals("La descripción detallada debe tener entre 1 y 200 caracteres.", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02 Task 5.1: rechaza modificar una regla que no pertenece al curso indicado")
    void modificarDescripcionYVigencia_rechazaReglaDeOtroCurso() {
        prepararReglaDelCursoConDocenteTitular();
        when(reglaDAO.obtenerPorIdYCurso(ID_REGLA, ID_CURSO)).thenReturn(null);

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaService.modificarDescripcionYVigencia(modificacionDelTitular("Descripción válida", EstadoVigencia.VIGENTE)));

        assertEquals("La regla no pertenece al curso especificado o no existe.", error.getMessage());
        verificarQueNoSeActualizo();
    }

    @Test
    @DisplayName("HU-02 Task 5.1: rechaza que un docente no titular modifique reglas del curso")
    void modificarDescripcionYVigencia_rechazaDocenteNoTitular() {
        prepararReglaDelCursoConDocenteTitular();
        ModificacionReglaDTO modificacionAjena = new ModificacionReglaDTO(
                ID_REGLA, ID_CURSO, ID_DOCENTE_AJENO, "Descripción válida", EstadoVigencia.VIGENTE);

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class, () ->
                reglaService.modificarDescripcionYVigencia(modificacionAjena));

        assertEquals("No tiene permisos para gestionar un curso del que no es docente titular.", error.getMessage());
        verificarQueNoSeActualizo();
    }
}
