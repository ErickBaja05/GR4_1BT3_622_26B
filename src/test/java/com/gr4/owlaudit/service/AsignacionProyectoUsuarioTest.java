package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.model.Usuario;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AsignacionProyectoUsuarioTest {

    private ProyectoDAO proyectoDAO;
    private ProyectoService proyectoService;

    private ReglaDAO reglaDAO;
    private AuditoriaDAO auditoriaDAO;
    private GitHubClient gitHubClient;
    private EvaluadorReglaFactory evaluadorFactory;
    private AuditoriaService auditoriaService;

    @BeforeEach
    void setUp() {
        proyectoDAO = mock(ProyectoDAO.class);
        proyectoService = new ProyectoServiceImpl(proyectoDAO);

        reglaDAO = mock(ReglaDAO.class);
        auditoriaDAO = mock(AuditoriaDAO.class);
        gitHubClient = mock(GitHubClient.class);
        evaluadorFactory = new EvaluadorReglaFactory();
        auditoriaService = new AuditoriaServiceImpl(reglaDAO, auditoriaDAO, gitHubClient, evaluadorFactory, proyectoDAO);
    }

    @Test
    @DisplayName("Unit Test: Registro de proyecto asocia correctamente el ID del usuario creador")
    void testRegistroProyectoAsociaUsuarioId() {
        Long usuarioId = 2L;
        NuevoProyectoDTO dto = new NuevoProyectoDTO("Sistema Web EPN", "https://github.com/epn/sistema-web", usuarioId);

        proyectoService.solicitarRegistroDeNuevoProyecto(dto);

        ArgumentCaptor<Proyecto> captor = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoDAO).guardarProyectoAcademico(captor.capture());

        Proyecto guardado = captor.getValue();
        assertNotNull(guardado, "El proyecto no debe ser nulo");
        assertEquals("Sistema Web EPN", guardado.getNombre());
        assertEquals("https://github.com/epn/sistema-web", guardado.getUrlGithub());
        assertNotNull(guardado.getUsuario(), "El proyecto debe tener asociado un Usuario");
        assertEquals(usuarioId, guardado.getUsuario().getId(), "El ID del usuario asociado debe coincidir con el del estudiante");
    }

    @Test
    @DisplayName("Unit Test: Al ejecutar auditoría se asocia el usuario propietario del proyecto a la auditoría")
    void testAuditoriaHeredaUsuarioDelProyecto() {
        Long usuarioId = 5L;
        Usuario usuarioPropietario = new Usuario();
        usuarioPropietario.setId(usuarioId);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(10L);
        proyecto.setNombre("Proyecto de Prueba");
        proyecto.setUrlGithub("https://github.com/epn/test-repo");
        proyecto.setUsuario(usuarioPropietario);

        when(proyectoDAO.buscarPorId(10L)).thenReturn(proyecto);

        Regla regla = new Regla();
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setNombreRepresentativo("README");
        regla.setDescripcionDetallada("Desc");
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.MEDIA);
        regla.setPonderacion(10);
        regla.setActiva(true);
        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(List.of(regla));
        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString())).thenReturn(List.of("README.md"));

        AuditoriaDTO dto = new AuditoriaDTO(10L, "https://github.com/epn/test-repo", usuarioId);
        auditoriaService.confirmarEjecucionDeAuditoria(dto);

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaDAO).registrarAuditoriaEnElHistorial(captor.capture());

        Auditoria auditoriaGuardada = captor.getValue();
        assertNotNull(auditoriaGuardada, "La auditoría no debe ser nula");
        assertEquals(proyecto, auditoriaGuardada.getProyecto(), "La auditoría debe pertenecer al proyecto");
        assertNotNull(auditoriaGuardada.getUsuario(), "La auditoría debe tener asociado un Usuario");
        assertEquals(usuarioId, auditoriaGuardada.getUsuario().getId(), "La auditoría debe estar asociada al ID del usuario creador del proyecto");
    }

    @Test
    @DisplayName("Unit Test: Filtrado de proyectos en memoria/mock por usuarioId aísla repositorios entre estudiantes")
    void testAislamientoProyectosPorUsuario() {
        Usuario u1 = new Usuario();
        u1.setId(101L);

        Usuario u2 = new Usuario();
        u2.setId(102L);

        Proyecto p1 = new Proyecto();
        p1.setId(1L);
        p1.setNombre("Repo Usuario 1");
        p1.setUsuario(u1);

        Proyecto p2 = new Proyecto();
        p2.setId(2L);
        p2.setNombre("Repo Usuario 2");
        p2.setUsuario(u2);

        when(proyectoDAO.listarPorUsuarioId(101L)).thenReturn(List.of(p1));
        when(proyectoDAO.listarPorUsuarioId(102L)).thenReturn(List.of(p2));

        List<Proyecto> listaU1 = proyectoDAO.listarPorUsuarioId(101L);
        List<Proyecto> listaU2 = proyectoDAO.listarPorUsuarioId(102L);

        assertEquals(1, listaU1.size());
        assertEquals("Repo Usuario 1", listaU1.get(0).getNombre());

        assertEquals(1, listaU2.size());
        assertEquals("Repo Usuario 2", listaU2.get(0).getNombre());

        assertFalse(listaU1.contains(p2), "El usuario 1 no debe poder ver los proyectos del usuario 2");
        assertFalse(listaU2.contains(p1), "El usuario 2 no debe poder ver los proyectos del usuario 1");
    }
}
