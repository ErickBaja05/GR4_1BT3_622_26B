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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProyectoValidacionUnicidadTest {

    @Mock
    private ProyectoDAO proyectoDAO;

    private ProyectoServiceImpl proyectoService;

    @BeforeEach
    void setUp() {
        proyectoService = new ProyectoServiceImpl(proyectoDAO);
    }

    @Test
    @DisplayName("No permite registrar dos proyectos con el mismo nombre para el mismo estudiante")
    void testNoPermiteMismoNombreParaElMismoEstudiante() {
        Long usuarioId = 1L;

        Proyecto existente = new Proyecto();
        existente.setId(10L);
        existente.setNombre("Proyecto Alpha");
        existente.setUrlGithub("https://github.com/user/alpha");
        Usuario u = new Usuario();
        u.setId(usuarioId);
        existente.setUsuario(u);

        when(proyectoDAO.listarPorUsuarioId(usuarioId)).thenReturn(List.of(existente));

        NuevoProyectoDTO dto = new NuevoProyectoDTO("proyecto alpha", "https://github.com/user/beta");
        dto.setUsuarioId(usuarioId);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> proyectoService.solicitarRegistroDeNuevoProyecto(dto));

        assertEquals("Ya tiene un proyecto registrado con este nombre.", ex.getMessage());
    }

    @Test
    @DisplayName("No permite registrar dos proyectos con la misma URL (normalizada) para el mismo estudiante")
    void testNoPermiteMismaUrlParaElMismoEstudiante() {
        Long usuarioId = 1L;

        Proyecto existente = new Proyecto();
        existente.setId(10L);
        existente.setNombre("Proyecto Alpha");
        existente.setUrlGithub("https://github.com/user/alpha/");
        Usuario u = new Usuario();
        u.setId(usuarioId);
        existente.setUsuario(u);

        when(proyectoDAO.listarPorUsuarioId(usuarioId)).thenReturn(List.of(existente));

        // Mismo repositorio sin slash final y con diferente nombre
        NuevoProyectoDTO dto = new NuevoProyectoDTO("Proyecto Beta", "https://github.com/user/alpha");
        dto.setUsuarioId(usuarioId);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> proyectoService.solicitarRegistroDeNuevoProyecto(dto));

        assertEquals("Ya tiene un proyecto registrado con este enlace de repositorio.", ex.getMessage());
    }

    @Test
    @DisplayName("Permite que dos estudiantes distintos registren proyectos con el mismo nombre o URL")
    void testPermiteMismoNombreOUrlEntreEstudiantesDistintos() {
        Long estudianteB = 2L;

        // Estudiante B no tiene proyectos registrados
        when(proyectoDAO.listarPorUsuarioId(estudianteB)).thenReturn(List.of());

        // Estudiante B registra el mismo nombre y URL que tiene el estudiante A
        NuevoProyectoDTO dtoB = new NuevoProyectoDTO("Proyecto Alpha", "https://github.com/user/alpha");
        dtoB.setUsuarioId(estudianteB);

        proyectoService.solicitarRegistroDeNuevoProyecto(dtoB);

        verify(proyectoDAO).guardarProyectoAcademico(any(Proyecto.class));
    }

    @Test
    @DisplayName("Al actualizar, no permite asignar un nombre o URL que ya pertenezca a otro de sus proyectos")
    void testActualizarRechazaColisionConOtroProyectoDelMismoEstudiante() {
        Long usuarioId = 1L;

        Proyecto p1 = new Proyecto();
        p1.setId(1L);
        p1.setNombre("Proyecto Uno");
        p1.setUrlGithub("https://github.com/user/uno");
        Usuario u = new Usuario();
        u.setId(usuarioId);
        p1.setUsuario(u);

        Proyecto p2 = new Proyecto();
        p2.setId(2L);
        p2.setNombre("Proyecto Dos");
        p2.setUrlGithub("https://github.com/user/dos");
        p2.setUsuario(u);

        when(proyectoDAO.listarPorUsuarioId(usuarioId)).thenReturn(List.of(p1, p2));

        // Se intenta actualizar p2 para que tenga el nombre de p1
        NuevoProyectoDTO dtoUpdate = new NuevoProyectoDTO("Proyecto Uno", "https://github.com/user/dos");

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class,
                () -> proyectoService.actualizarProyecto(2L, dtoUpdate, usuarioId));

        assertEquals("Ya tiene un proyecto registrado con este nombre.", ex.getMessage());
    }
}
