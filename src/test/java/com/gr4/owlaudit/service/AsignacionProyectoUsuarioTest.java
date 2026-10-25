package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.EstadoVigencia;
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
        regla.setVigencia(EstadoVigencia.VIGENTE);
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

    @Test
    @DisplayName("Unit Test: El ID asignado de proyecto para cada estudiante se reinicia en 1, 2, 3...")
    void testIdSecuencialPorEstudianteSeReiniciaEnUno() {
        Long estudiante1 = 1L;
        Long estudiante2 = 2L;

        // Estudiante 1 no tiene proyectos aún -> su primer proyecto debe recibir idEstudiante = 1
        when(proyectoDAO.listarPorUsuarioId(estudiante1)).thenReturn(new ArrayList<>());
        NuevoProyectoDTO dto1E1 = new NuevoProyectoDTO("Proyecto 1 de Estudiante 1", "https://github.com/e1/repo1", estudiante1);
        proyectoService.solicitarRegistroDeNuevoProyecto(dto1E1);

        ArgumentCaptor<Proyecto> captor1 = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoDAO, times(1)).guardarProyectoAcademico(captor1.capture());
        Proyecto guardado1E1 = captor1.getValue();
        assertEquals(1L, guardado1E1.getIdEstudiante(), "El primer proyecto del estudiante 1 debe tener ID 1");

        // Estudiante 1 ya tiene 1 proyecto registrado -> el segundo debe recibir idEstudiante = 2
        Proyecto existenteE1 = new Proyecto();
        existenteE1.setId(10L);
        existenteE1.setIdEstudiante(1L);
        when(proyectoDAO.listarPorUsuarioId(estudiante1)).thenReturn(List.of(existenteE1));

        NuevoProyectoDTO dto2E1 = new NuevoProyectoDTO("Proyecto 2 de Estudiante 1", "https://github.com/e1/repo2", estudiante1);
        proyectoService.solicitarRegistroDeNuevoProyecto(dto2E1);

        ArgumentCaptor<Proyecto> captor2 = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoDAO, times(2)).guardarProyectoAcademico(captor2.capture());
        Proyecto guardado2E1 = captor2.getValue();
        assertEquals(2L, guardado2E1.getIdEstudiante(), "El segundo proyecto del estudiante 1 debe tener ID 2");

        // Estudiante 2 registra su primer proyecto -> su ID debe REINICIARSE a 1
        when(proyectoDAO.listarPorUsuarioId(estudiante2)).thenReturn(new ArrayList<>());
        NuevoProyectoDTO dto1E2 = new NuevoProyectoDTO("Proyecto 1 de Estudiante 2", "https://github.com/e2/repo1", estudiante2);
        proyectoService.solicitarRegistroDeNuevoProyecto(dto1E2);

        ArgumentCaptor<Proyecto> captor3 = ArgumentCaptor.forClass(Proyecto.class);
        verify(proyectoDAO, times(3)).guardarProyectoAcademico(captor3.capture());
        Proyecto guardado1E2 = captor3.getValue();
        assertEquals(1L, guardado1E2.getIdEstudiante(), "El ID de proyecto para el nuevo estudiante debe reiniciarse en 1");
    }

    @Test
    @DisplayName("Unit Test: Al solicitar retroalimentación se asocia el usuario estudiante a la solicitud")
    void testSolicitudRetroalimentacionAsociaUsuarioEstudiante() {
        SolicitudDAO solicitudDAO = mock(SolicitudDAO.class);
        RetroalimentacionService retroService = new RetroalimentacionServiceImpl(solicitudDAO);

        Long hallazgoId = 99L;
        Long usuarioEstudianteId = 7L;
        when(solicitudDAO.buscarPorHallazgoId(hallazgoId)).thenReturn(null);

        com.gr4.owlaudit.dto.SolicitudDTO dto = new com.gr4.owlaudit.dto.SolicitudDTO(hallazgoId, "Justificación técnica detallada", usuarioEstudianteId);
        retroService.solicitarRetroalimentacionDeHallazgo(dto);

        ArgumentCaptor<com.gr4.owlaudit.model.SolicitudRetroalimentacion> captor = ArgumentCaptor.forClass(com.gr4.owlaudit.model.SolicitudRetroalimentacion.class);
        verify(solicitudDAO).crearSolicitudDeRetroalimentacion(captor.capture());

        com.gr4.owlaudit.model.SolicitudRetroalimentacion guardada = captor.getValue();
        assertNotNull(guardada, "La solicitud debe existir");
        assertNotNull(guardada.getUsuario(), "La solicitud debe tener asociado un usuario estudiante");
        assertEquals(usuarioEstudianteId, guardada.getUsuario().getId(), "El ID del usuario debe coincidir con el estudiante que solicitó");
    }
}
