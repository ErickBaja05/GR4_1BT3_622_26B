package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AislamientoEstudianteTest {

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private ReglaDAO reglaDAO;

    private ProyectoServiceImpl proyectoService;
    private AuditoriaServiceImpl auditoriaService;
    private EvolucionServiceImpl evolucionService;

    @BeforeEach
    void setUp() {
        proyectoService = new ProyectoServiceImpl(proyectoDAO);
        auditoriaService = new AuditoriaServiceImpl(reglaDAO, auditoriaDAO, null, null, proyectoDAO);
        evolucionService = new EvolucionServiceImpl(auditoriaDAO);
    }

    @Test
    @DisplayName("Aislamiento: Intento de auditoría libre es bloqueado y exige asignación de curso")
    void testEstudianteNoPuedeAuditarLibreSinAsignacionDeCurso() {
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setProyectoId(20L);
        dto.setUrl("https://github.com/userB/repo");
        dto.setUsuarioId(1L);
        dto.setIdAsignacion(null);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> auditoriaService.ejecutarAuditoriaDeAsignacion(dto));

        assertTrue(ex.getMessage().contains("No se permiten auditorías libres"));
    }

    @Test
    @DisplayName("Aislamiento: Estudiante no puede auditar un proyecto ajeno bajo una asignación")
    void testEstudianteNoPuedeAuditarProyectoAjenoEnAuditoriaAsignacion() {
        Long usuarioEstudianteA = 1L;
        Long usuarioEstudianteB = 2L;

        Proyecto proyectoB = new Proyecto();
        proyectoB.setId(20L);
        Usuario usuarioB = new Usuario();
        usuarioB.setId(usuarioEstudianteB);
        proyectoB.setUsuario(usuarioB);

        when(proyectoDAO.buscarPorId(20L)).thenReturn(proyectoB);

        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(5L);
        dto.setProyectoId(20L);
        dto.setUsuarioId(usuarioEstudianteA);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> auditoriaService.ejecutarAuditoriaDeAsignacion(dto));

        assertEquals("No tiene permisos para auditar un proyecto que no le pertenece", ex.getMessage());
    }

    @Test
    @DisplayName("Eliminación segura: borrar proyecto #1 del Estudiante A no borra el proyecto #1 del Estudiante B")
    void testEliminarProyectoEstudianteNoAfectaProyectoDeOtroEstudianteConMismoIdEstudiante() {
        Long usuarioA = 1L;
        Long usuarioB = 2L;

        Proyecto proyA = new Proyecto();
        proyA.setId(10L); // DB id
        proyA.setIdEstudiante(1L); // Estudiante A #1
        Usuario uA = new Usuario();
        uA.setId(usuarioA);
        proyA.setUsuario(uA);

        // Estudiante A elimina su proyecto #1 (que internamente tiene id = 10)
        when(proyectoDAO.buscarPorIdYUsuarioId(10L, usuarioA)).thenReturn(proyA);

        proyectoService.eliminarProyecto(10L, usuarioA);

        // Verifica que se eliminó el ID de A y se resecuenciaron solo los proyectos de A
        verify(proyectoDAO).eliminar(10L);
        verify(proyectoDAO).resecuenciarIdsEstudiante(usuarioA);

        // Nunca se tocó ni resecuenció el usuario B
        verify(proyectoDAO, never()).eliminar(20L);
        verify(proyectoDAO, never()).resecuenciarIdsEstudiante(usuarioB);
    }

    @Test
    @DisplayName("Evolución: muestra números secuenciales relativos al repositorio (1..N) en lugar de IDs globales dispersos")
    void testEvolucionMuestraNumerosSecuencialesPorRepositorio() {
        Long proyectoId = 10L;

        Proyecto proyecto = new Proyecto();
        proyecto.setId(proyectoId);

        Auditoria a1 = new Auditoria();
        a1.setId(101L); // ID global en BD
        a1.setNumeroEnProyecto(1L);
        a1.setProyecto(proyecto);
        a1.setFechaHora(LocalDateTime.of(2026, 11, 1, 10, 0));
        a1.setPuntajeObtenido(60);
        a1.setPuntajeMaximo(100);
        a1.setPorcentaje(60.0);
        a1.setDictamen("NO APROBADA");

        Auditoria a2 = new Auditoria();
        a2.setId(145L); // ID global en BD de otra transacción
        a2.setNumeroEnProyecto(2L);
        a2.setProyecto(proyecto);
        a2.setFechaHora(LocalDateTime.of(2026, 11, 1, 12, 0));
        a2.setPuntajeObtenido(90);
        a2.setPuntajeMaximo(100);
        a2.setPorcentaje(90.0);
        a2.setDictamen("APROBADA");

        Regla r1 = new Regla();
        r1.setId(1L);
        r1.setNombreRepresentativo("Regla de Prueba");
        r1.setPonderacion(100);

        ResultadoRegla res1 = new ResultadoRegla(false, 60, r1, a1);
        ResultadoRegla res2 = new ResultadoRegla(true, 90, r1, a2);

        when(auditoriaDAO.consultarHistorialDeAuditorias(proyectoId)).thenReturn(List.of(a1, a2));
        when(auditoriaDAO.consultarResultadosDeReglas(101L, 145L)).thenReturn(List.of(res1, res2));

        // Comparar a1 vs a2
        EvolucionDTO dto = new EvolucionDTO(proyectoId, 101L, 145L);
        EvolucionFinalDTO evolucion = evolucionService.indicarDosAuditoriasAComparar(dto);

        // El ID visible para el repositorio debe ser secuencial 1 y 2 (no 101 vs 145)
        assertEquals(1L, evolucion.getNumeroBase());
        assertEquals(2L, evolucion.getNumeroComparada());
        assertEquals(1L, evolucion.getIdVisibleBase());
        assertEquals(2L, evolucion.getIdVisibleComparada());
    }
}
