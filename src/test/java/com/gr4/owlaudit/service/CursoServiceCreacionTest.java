package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.RolEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceCreacionTest {

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private UsuarioDAO usuarioDAO;

    @InjectMocks
    private CursoServiceImpl cursoService;

    @Test
    @DisplayName("CA1 - Escenario 1.1: Busca al docente por correo, lo vincula al curso y lo guarda en BD")
    void crearCurso_conDatosValidosYCorreoDocente_guardaCursoYAsociaDocente() throws ExcepcionNegocio {
        String correoDocente = "docente@epn.edu.ec";
        Docente docente = new Docente();
        docente.setId(5L);
        docente.setNombre("Ing. Carlos Pérez");
        docente.setCorreo(correoDocente);
        docente.setRol(RolEnum.DOCENTE);

        NuevoCursoDTO dto = new NuevoCursoDTO(
                "Construcción de Software",
                "csw-2026b",
                "Curso de ingeniería de software grupo 4");

        when(cursoDAO.buscarPorCodigo("CSW-2026B")).thenReturn(null);
        when(usuarioDAO.buscarPorCorreo(correoDocente)).thenReturn(docente);

        Curso resultado = cursoService.crearCurso(dto, correoDocente);

        assertNotNull(resultado);
        assertEquals("Construcción de Software", resultado.getNombre());
        assertEquals("CSW-2026B", resultado.getCodigo());
        assertEquals("Curso de ingeniería de software grupo 4", resultado.getDescripcion());
        assertEquals(docente, resultado.getDocente());

        ArgumentCaptor<Curso> captor = ArgumentCaptor.forClass(Curso.class);
        verify(cursoDAO).guardar(captor.capture());

        Curso cursoPersistido = captor.getValue();
        assertEquals("CSW-2026B", cursoPersistido.getCodigo());
        assertEquals(docente, cursoPersistido.getDocente());
    }

    @Test
    @DisplayName("Seguridad: Rechaza la creación si el correo pertenece a un Estudiante")
    void crearCurso_cuandoCorreoPerteneceAEstudiante_lanzaExcepcionNegocio() {
        String correoEstudiante = "estudiante@epn.edu.ec";
        Estudiante estudiante = new Estudiante();
        estudiante.setId(8L);
        estudiante.setCorreo(correoEstudiante);
        estudiante.setRol(RolEnum.ESTUDIANTE);

        NuevoCursoDTO dto = new NuevoCursoDTO("Construcción SW", "CSW-2026B", "Descripción");

        when(cursoDAO.buscarPorCodigo("CSW-2026B")).thenReturn(null);
        when(usuarioDAO.buscarPorCorreo(correoEstudiante)).thenReturn(estudiante);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> {
            cursoService.crearCurso(dto, correoEstudiante);
        });

        assertEquals("Solo un usuario con rol de docente puede crear cursos", ex.getMessage());
        verify(cursoDAO, never()).guardar(any(Curso.class));
    }

    @Test
    @DisplayName("CA1 - Escenario 1.2: Lista únicamente los cursos asociados al correo del docente")
    void listarCursosPorCorreoDocente_retornaCursosDelDocente() {
        String correoDocente = "docente@epn.edu.ec";
        Docente docente = new Docente();
        docente.setId(5L);
        docente.setCorreo(correoDocente);
        docente.setRol(RolEnum.DOCENTE);

        Curso curso = new Curso();
        curso.setCodigo("CSW-2026B");
        curso.setDocente(docente);

        when(usuarioDAO.buscarPorCorreo(correoDocente)).thenReturn(docente);
        when(cursoDAO.listarCursosPorDocente(5L)).thenReturn(List.of(curso));

        List<Curso> cursos = cursoService.listarCursosPorCorreoDocente(correoDocente);

        assertEquals(1, cursos.size());
        assertEquals("CSW-2026B", cursos.get(0).getCodigo());
        verify(cursoDAO).listarCursosPorDocente(5L);
    }
}
