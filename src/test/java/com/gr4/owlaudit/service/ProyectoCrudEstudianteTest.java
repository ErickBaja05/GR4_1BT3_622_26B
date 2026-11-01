package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProyectoCrudEstudianteTest {

    @Mock
    private ProyectoDAO proyectoDAO;

    private ProyectoServiceImpl proyectoService;

    private Usuario estudiantePropietario;
    private Usuario estudianteAjeno;
    private Proyecto proyectoEstudiante;

    @BeforeEach
    void setUp() {
        proyectoService = new ProyectoServiceImpl(proyectoDAO);

        estudiantePropietario = new Usuario();
        estudiantePropietario.setId(10L);
        estudiantePropietario.setNombre("Estudiante Uno");

        estudianteAjeno = new Usuario();
        estudianteAjeno.setId(99L);
        estudianteAjeno.setNombre("Estudiante Dos");

        proyectoEstudiante = new Proyecto();
        proyectoEstudiante.setId(1L);
        proyectoEstudiante.setNombre("Proyecto Inicial");
        proyectoEstudiante.setUrlGithub("https://github.com/estudiante1/inicial");
        proyectoEstudiante.setUsuario(estudiantePropietario);
        proyectoEstudiante.setIdEstudiante(1L);
    }

    @Test
    @DisplayName("Debe actualizar un proyecto cuando el estudiante es el propietario")
    void actualizarProyecto_cuandoEstudianteEsPropietario_actualizaNombreYUrl() {
        when(proyectoDAO.buscarPorId(1L)).thenReturn(proyectoEstudiante);

        NuevoProyectoDTO dto = new NuevoProyectoDTO("Proyecto Modificado", "https://github.com/estudiante1/modificado", 10L);

        proyectoService.actualizarProyecto(1L, dto, 10L);

        verify(proyectoDAO, times(1)).buscarPorId(1L);
        verify(proyectoDAO, times(1)).actualizar(proyectoEstudiante);
        assertEquals("Proyecto Modificado", proyectoEstudiante.getNombre());
        assertEquals("https://github.com/estudiante1/modificado", proyectoEstudiante.getUrlGithub());
    }

    @Test
    @DisplayName("Debe denegar la actualización si el proyecto no pertenece al estudiante")
    void actualizarProyecto_cuandoEstudianteNoEsPropietario_lanzaExcepcionNegocio() {
        when(proyectoDAO.buscarPorId(1L)).thenReturn(proyectoEstudiante);

        NuevoProyectoDTO dto = new NuevoProyectoDTO("Intento Hack", "https://github.com/hacker/repo", 99L);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                proyectoService.actualizarProyecto(1L, dto, 99L)
        );
        assertTrue(ex.getMessage().contains("permiso") || ex.getMessage().contains("propios"));
        verify(proyectoDAO, never()).actualizar(any());
    }

    @Test
    @DisplayName("Debe eliminar un proyecto del estudiante y resecuenciar los IDs restantes")
    void eliminarProyecto_cuandoEstudianteEsPropietario_eliminaYResecuenciaIds() {
        when(proyectoDAO.buscarPorId(1L)).thenReturn(proyectoEstudiante);

        proyectoService.eliminarProyecto(1L, 10L);

        verify(proyectoDAO, times(1)).buscarPorId(1L);
        verify(proyectoDAO, times(1)).eliminar(1L);
        verify(proyectoDAO, times(1)).resecuenciarIdsEstudiante(10L);
    }

    @Test
    @DisplayName("Debe denegar la eliminación si el proyecto pertenece a otro estudiante")
    void eliminarProyecto_cuandoEstudianteNoEsPropietario_lanzaExcepcionNegocio() {
        when(proyectoDAO.buscarPorId(1L)).thenReturn(proyectoEstudiante);

        assertThrows(ExcepcionNegocio.class, () ->
                proyectoService.eliminarProyecto(1L, 99L)
        );
        verify(proyectoDAO, never()).eliminar(any());
        verify(proyectoDAO, never()).resecuenciarIdsEstudiante(any());
    }

    @Test
    @DisplayName("Debe lanzar excepción si el proyecto a eliminar no existe")
    void eliminarProyecto_cuandoProyectoNoExiste_lanzaExcepcionNegocio() {
        when(proyectoDAO.buscarPorId(500L)).thenReturn(null);

        assertThrows(ExcepcionNegocio.class, () ->
                proyectoService.eliminarProyecto(500L, 10L)
        );
        verify(proyectoDAO, never()).eliminar(any());
    }

    @Test
    @DisplayName("Al eliminar un proyecto, los IDs se actualizan para que el próximo registro retome el orden secuencial")
    void secuenciaIds_alEliminarProyectoIntermedio_actualizaIdsYProximoRegistroRetomaNumero() {
        Long usuarioId = 10L;

        // Simulamos que el estudiante tiene 3 proyectos secuenciales (1, 2, 3)
        Proyecto p1 = new Proyecto();
        p1.setId(101L);
        p1.setNombre("Repo 1");
        p1.setIdEstudiante(1L);
        p1.setUsuario(estudiantePropietario);

        Proyecto p2 = new Proyecto();
        p2.setId(102L);
        p2.setNombre("Repo 2");
        p2.setIdEstudiante(2L);
        p2.setUsuario(estudiantePropietario);

        Proyecto p3 = new Proyecto();
        p3.setId(103L);
        p3.setNombre("Repo 3");
        p3.setIdEstudiante(3L);
        p3.setUsuario(estudiantePropietario);

        when(proyectoDAO.buscarPorId(102L)).thenReturn(p2);

        // Eliminamos el proyecto 2
        proyectoService.eliminarProyecto(102L, usuarioId);

        verify(proyectoDAO, times(1)).eliminar(102L);
        verify(proyectoDAO, times(1)).resecuenciarIdsEstudiante(usuarioId);

        // Ahora los proyectos restantes son p1 y p3 con IDs resecuenciados a 1 y 2
        p3.setIdEstudiante(2L);
        List<Proyecto> restantes = List.of(p1, p3);
        when(proyectoDAO.listarPorUsuarioId(usuarioId)).thenReturn(restantes);

        // El estudiante registra un nuevo proyecto: debe recibir el id secuencial 3 (el siguiente disponible)
        NuevoProyectoDTO nuevoDto = new NuevoProyectoDTO("Repo Nuevo", "https://github.com/estudiante1/nuevo", usuarioId);
        proyectoService.solicitarRegistroDeNuevoProyecto(nuevoDto);

        ArgumentCaptor<Proyecto> captor = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoDAO).guardarProyectoAcademico(captor.capture());
        Proyecto guardado = captor.getValue();

        assertEquals(3L, guardado.getIdEstudiante(), "El nuevo proyecto debe retomar el orden secuencial");
    }
}
