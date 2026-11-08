package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceUnicidadCodigoTest {

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private CursoServiceImpl cursoService;

    @Test
    @DisplayName("CA2 - Escenario 2.1: Rechaza creación cuando el código del curso ya existe en BD")
    void crearCurso_cuandoCodigoYaExiste_lanzaExcepcionNegocio() {
        String codigoExistente = "CSW-2026B";
        NuevoCursoDTO dto = new NuevoCursoDTO("Construcción de Software", codigoExistente,
                "Curso de ingeniería de software grupo 4");

        Curso cursoEncontrado = new Curso();
        cursoEncontrado.setId(10L);
        cursoEncontrado.setCodigo(codigoExistente);

        // CursoDAO.buscarPorCodigo retorna Curso directamente cuando existe
        when(cursoDAO.buscarPorCodigo(codigoExistente)).thenReturn(cursoEncontrado);

        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(dto, 1L);
        });

        assertEquals("El código de curso ingresado ya se encuentra en uso; ingrese un código único",
                excepcion.getMessage());
        verify(cursoDAO).buscarPorCodigo(codigoExistente);
        verify(cursoDAO, never()).guardar(any());
    }

    @Test
    @DisplayName("CA2 - Escenario 2.1: Normaliza a mayúsculas y sin espacios antes de consultar unicidad en el DAO")
    void crearCurso_cuandoCodigoCoincideConEspaciosOMinusculas_lanzaExcepcionNegocio() {
        String codigoEnviado = "  csw-2026b  ";
        String codigoNormalizado = "CSW-2026B";
        NuevoCursoDTO dto = new NuevoCursoDTO("Construcción de Software", codigoEnviado, "Descripción válida");

        when(cursoDAO.buscarPorCodigo(codigoNormalizado)).thenReturn(new Curso());

        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(dto, 1L);
        });

        assertEquals("El código de curso ingresado ya se encuentra en uso; ingrese un código único",
                excepcion.getMessage());
        verify(cursoDAO).buscarPorCodigo(codigoNormalizado);
        verify(cursoDAO, never()).guardar(any());
    }
}
