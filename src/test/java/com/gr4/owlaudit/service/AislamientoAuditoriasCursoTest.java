package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.ListadoEstudiantes;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.servlet.OpcionMenu;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * TDD Suite para EP-01 >> FT-01 >> HU-03: Eliminación auditorías libres
 * y aislamiento de asignaciones al interior de cada curso inscrito.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AislamientoAuditoriasCursoTest {

    private static final String MENSAJE_PERMISO_CURSO =
            "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado";

    @Mock
    private AsignacionesDAO asignacionesDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private GitHubClient gitHubClient;

    private AsignacionesServiceImpl asignacionesService;
    private AuditoriaServiceImpl auditoriaService;

    private Curso cursoConstruccion;
    private Curso cursoArquitectura;
    private Estudiante estudianteMatriculado;
    private Estudiante estudianteNoMatriculado;
    private Asignacion asignacionConstruccion;
    private Asignacion asignacionArquitectura;
    private Proyecto proyectoEstudiante;

    @BeforeEach
    void setUp() {
        Docente docente = new Docente();
        docente.setId(100L);
        docente.setNombre("Docente Titular");

        cursoConstruccion = new Curso(docente, "CS-2026B", "Construcción de Software", "2026-B", "Curso de software");
        cursoConstruccion.setId(1L);

        cursoArquitectura = new Curso(docente, "ARQ-2026B", "Arquitectura de Software", "2026-B", "Curso de arquitectura");
        cursoArquitectura.setId(2L);

        estudianteMatriculado = new Estudiante();
        estudianteMatriculado.setId(10L);
        estudianteMatriculado.setNombre("Estudiante Inscrito");

        estudianteNoMatriculado = new Estudiante();
        estudianteNoMatriculado.setId(99L);
        estudianteNoMatriculado.setNombre("Estudiante No Inscrito");

        ListadoEstudiantes matricula = new ListadoEstudiantes(cursoConstruccion, estudianteMatriculado, null, "ACTIVO");
        cursoConstruccion.getListadoEstudiantes().add(matricula);

        asignacionConstruccion = new Asignacion(cursoConstruccion, "Práctica 1 - Métricas", 75.0, List.of());
        asignacionConstruccion.setId(101L);

        asignacionArquitectura = new Asignacion(cursoArquitectura, "Laboratorio Patrones", 80.0, List.of());
        asignacionArquitectura.setId(202L);

        proyectoEstudiante = new Proyecto();
        proyectoEstudiante.setId(50L);
        proyectoEstudiante.setNombre("Repo Calidad");
        proyectoEstudiante.setUrlGithub("https://github.com/alumno/repo-calidad");
        proyectoEstudiante.setUsuario(estudianteMatriculado);

        asignacionesService = new AsignacionesServiceImpl(asignacionesDAO, reglaDAO, proyectoDAO);
        auditoriaService = new AuditoriaServiceImpl(reglaDAO, auditoriaDAO, gitHubClient, null, proyectoDAO, asignacionesService);
    }

    @Test
    @DisplayName("CA1 - Escenario 1.1: El menú del estudiante no ofrece auditoría libre y provee 'Mis Cursos'")
    void testMenuNoOfreceAuditoriaLibreYProveeMisCursos() {
        OpcionMenu opcionAuditoriaLibre = OpcionMenu.porClave("auditoria");
        assertNull(opcionAuditoriaLibre, "La opción de auditoría libre ya no debe figurar en el catálogo de menú");

        OpcionMenu opcionMisCursos = OpcionMenu.porClave("curso");
        assertNotNull(opcionMisCursos, "Debe existir la opción de menú 'Mis Cursos' con clave 'curso'");
        assertEquals("Mis Cursos", opcionMisCursos.getTituloCorto());
        assertEquals("/curso", opcionMisCursos.getRuta());
        assertTrue(opcionMisCursos.permiteRol(RolEnum.ESTUDIANTE));

        List<OpcionMenu> opcionesEstudiante = OpcionMenu.opcionesPara(RolEnum.ESTUDIANTE);
        assertTrue(opcionesEstudiante.contains(opcionMisCursos),
                "El panel del estudiante debe incluir la opción 'Mis Cursos'");
        assertFalse(opcionesEstudiante.stream().anyMatch(o -> "/auditoria".equals(o.getRuta())),
                "El panel del estudiante no debe tener enlace a la auditoría libre");
    }

    @Test
    @DisplayName("CA1 - Escenario 1.2: Intento de auditoría libre directa sin asignación de curso debe ser bloqueado")
    void testAuditoriaBloqueadaSinAsignacionDeCursoLanzaExcepcion() {
        AuditoriaDTO dtoLibre = new AuditoriaDTO();
        dtoLibre.setProyectoId(proyectoEstudiante.getId());
        dtoLibre.setUrl(proyectoEstudiante.getUrlGithub());
        dtoLibre.setUsuarioId(estudianteMatriculado.getId());
        dtoLibre.setIdAsignacion(null);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> auditoriaService.confirmarEjecucionDeAuditoria(dtoLibre),
                "No debe permitirse ejecutar auditorías sin especificar asignación vinculada a curso");

        assertTrue(ex.getMessage().contains("asignación") || ex.getMessage().contains("curso"),
                "El mensaje debe indicar la obligación de asociar una asignación de curso");
    }

    @Test
    @DisplayName("CA3 - Escenario 3.1: Estudiante no puede consultar detalle de asignación de un curso en el que no está matriculado")
    void testEstudianteNoPuedeConsultarDetalleAsignacionDeCursoAjenoLanzaExcepcionPermisos() {
        when(asignacionesDAO.buscarPorId(asignacionArquitectura.getId())).thenReturn(asignacionArquitectura);
        when(asignacionesDAO.consultarAsignacionesDisponibles()).thenReturn(List.of(asignacionArquitectura));

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> asignacionesService.generarResumenAsignacion(asignacionArquitectura.getId(), estudianteMatriculado.getId()),
                "Debe denegar el acceso a asignaciones de un curso ajeno");

        assertEquals(MENSAJE_PERMISO_CURSO, ex.getMessage());
    }

    @Test
    @DisplayName("CA3 - Escenario 3.2: Estudiante no puede ejecutar auditoría en una asignación de un curso en el que no está matriculado")
    void testEstudianteNoPuedeEjecutarAuditoriaEnAsignacionDeCursoNoMatriculado() {
        when(proyectoDAO.buscarPorId(proyectoEstudiante.getId())).thenReturn(proyectoEstudiante);
        when(asignacionesDAO.buscarPorId(asignacionArquitectura.getId())).thenReturn(asignacionArquitectura);

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(asignacionArquitectura.getId());
        dto.setProyectoId(proyectoEstudiante.getId());
        dto.setUsuarioId(estudianteMatriculado.getId());

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> auditoriaService.ejecutarAuditoriaDeAsignacion(dto),
                "Debe bloquear la auditoría si el estudiante no pertenece al curso de la asignación");

        assertEquals(MENSAJE_PERMISO_CURSO, ex.getMessage());
    }

    @Test
    @DisplayName("CA2 - Escenario 2.1: Estudiante puede consultar asignación perteneciente a su curso matriculado")
    void testEstudiantePuedeConsultarAsignacionDeCursoMatriculado() {
        when(asignacionesDAO.buscarPorId(asignacionConstruccion.getId())).thenReturn(asignacionConstruccion);
        when(asignacionesDAO.consultarAsignacionesDisponibles()).thenReturn(List.of(asignacionConstruccion));
        when(asignacionesDAO.consultarUmbralAprobacion(asignacionConstruccion.getId())).thenReturn(75.0);
        when(asignacionesDAO.consultarReglasDeAsignacion(asignacionConstruccion.getId())).thenReturn(List.of());
        when(proyectoDAO.listarPorUsuarioId(estudianteMatriculado.getId())).thenReturn(List.of(proyectoEstudiante));

        ResumenAsignacionDTO resumen = asignacionesService.generarResumenAsignacion(
                asignacionConstruccion.getId(), estudianteMatriculado.getId());

        assertNotNull(resumen);
        assertEquals(asignacionConstruccion.getId(), resumen.getIdAsignacion());
        assertEquals(75.0, resumen.getUmbralAprobacion());
    }
}
