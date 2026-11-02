package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.Usuario;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.RetroalimentacionService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RetroalimentacionServletFiltrosTest {

    @Mock
    private RetroalimentacionService retroalimentacionService;
    @Mock
    private SolicitudDAO solicitudDAO;
    @Mock
    private ProyectoDAO proyectoDAO;
    @Mock
    private AuditoriaDAO auditoriaDAO;
    @Mock
    private AsignacionesService asignacionesService;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private HttpSession session;
    @Mock
    private RequestDispatcher dispatcher;

    private RetroalimentacionServlet servlet;
    private UsuarioDTO estudianteDTO;
    private Proyecto proyectoEstudiante;

    @BeforeEach
    void setUp() {
        servlet = new RetroalimentacionServlet(retroalimentacionService, solicitudDAO, proyectoDAO, auditoriaDAO, asignacionesService);

        estudianteDTO = new UsuarioDTO();
        estudianteDTO.setId(10L);
        estudianteDTO.setRol(RolEnum.ESTUDIANTE);
        estudianteDTO.setNombre("Estudiante Prueba");

        Usuario estudiante = new Usuario();
        estudiante.setId(10L);

        proyectoEstudiante = new Proyecto();
        proyectoEstudiante.setId(100L);
        proyectoEstudiante.setNombre("Proyecto TDD");
        proyectoEstudiante.setUsuario(estudiante);

        when(request.getSession()).thenReturn(session);
        when(session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)).thenReturn(estudianteDTO);
        when(request.getRequestDispatcher("/WEB-INF/views/solicitarRetroalimentacion.jsp")).thenReturn(dispatcher);
        when(proyectoDAO.listarPorUsuarioId(10L)).thenReturn(List.of(proyectoEstudiante));
    }

    @Test
    @DisplayName("CU05: Servlet pasa filtros de Asignación y Auditoría al servicio")
    void testFiltrosAsignacionYAuditoriaEnServlet() throws Exception {
        when(request.getParameter("accion")).thenReturn("solicitar");
        when(request.getParameter("proyectoId")).thenReturn("100");
        when(request.getParameter("tipoAuditoria")).thenReturn("ASIGNACION");
        when(request.getParameter("asignacionId")).thenReturn("5");
        when(request.getParameter("auditoriaId")).thenReturn("50");

        Asignacion asig = new Asignacion();
        asig.setId(5L);
        asig.setNombre("Taller 1");
        when(asignacionesService.listarAsignaciones()).thenReturn(List.of(asig));

        Auditoria aud = new Auditoria();
        aud.setId(50L);
        aud.setProyecto(proyectoEstudiante);
        aud.setTipoAuditoria("ASIGNACION");
        aud.setAsignacion(asig);
        aud.setNumeroEnAsignacion(1L);

        when(auditoriaDAO.consultarHistorialAuditoriasAsignacion(100L, 5L)).thenReturn(List.of(aud));
        when(retroalimentacionService.obtenerHallazgosDisponibles(100L, 10L, "ASIGNACION", 5L, 50L))
                .thenReturn(List.of(Map.of("hallazgoId", 1L, "codigoVisible", "AA-1")));

        servlet.doGet(request, response);

        verify(retroalimentacionService).obtenerHallazgosDisponibles(100L, 10L, "ASIGNACION", 5L, 50L);
        verify(request).setAttribute(eq("proyectoIdFiltro"), eq(100L));
        verify(request).setAttribute(eq("tipoAuditoriaFiltro"), eq("ASIGNACION"));
        verify(request).setAttribute(eq("asignacionIdFiltro"), eq(5L));
        verify(request).setAttribute(eq("auditoriaIdFiltro"), eq(50L));
        verify(dispatcher).forward(request, response);
    }
}

