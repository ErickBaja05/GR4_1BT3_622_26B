package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AsignacionReglaDAO;
import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AsignacionCrudRevalorizacionTest {

    @Mock
    private AsignacionesDAO asignacionesDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private AuditoriaService auditoriaService;

    @Mock
    private GitHubClient gitHubClient;

    @Mock
    private AsignacionReglaDAO asignacionReglaDAO;

    private AsignacionesServiceImpl asignacionesService;

    @BeforeEach
    void setUp() {
        asignacionesService = new AsignacionesServiceImpl(asignacionesDAO, reglaDAO);
        asignacionesService.setAuditoriaService(auditoriaService);
        // Evita que el servicio cree el DAO real y escriba en la base de datos
        asignacionesService.setAsignacionReglaDAO(asignacionReglaDAO);
    }

    @Test
    @DisplayName("Actualizar asignación: valida y revaloriza auditorías existentes ante cambio de reglas/umbral")
    void testActualizarAsignacionDisparaRevalorizacionDeAuditorias() {
        Long asignacionId = 1L;
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO("Taller Refactorizado", 75.0, List.of(10L, 20L));

        Asignacion existente = new Asignacion("Taller Inicial", 60.0, new ArrayList<>());
        existente.setId(asignacionId);

        Regla r1 = new Regla();
        r1.setId(10L);
        r1.setVigencia(com.gr4.owlaudit.model.EstadoVigencia.VIGENTE);

        Regla r2 = new Regla();
        r2.setId(20L);
        r2.setVigencia(com.gr4.owlaudit.model.EstadoVigencia.VIGENTE);

        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(List.of(r1, r2));

        asignacionesService.actualizarAsignacion(asignacionId, dto);

        verify(asignacionesDAO).actualizar(any(Asignacion.class), eq(List.of(10L, 20L)));
        verify(auditoriaService).revalorizarAuditoriasDeAsignacion(asignacionId);
    }

    @Test
    @DisplayName("Actualizar asignación: falla si los datos son inválidos (nombre vacío o umbral fuera de rango)")
    void testActualizarAsignacionConDatosInvalidosLanzaExcepcion() {
        NuevaAsignacionDTO dtoSinNombre = new NuevaAsignacionDTO("", 70.0, List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> asignacionesService.actualizarAsignacion(1L, dtoSinNombre));

        NuevaAsignacionDTO dtoUmbralBajo = new NuevaAsignacionDTO("Asignación Válida", 45.0, List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> asignacionesService.actualizarAsignacion(1L, dtoUmbralBajo));

        NuevaAsignacionDTO dtoUmbralAlto = new NuevaAsignacionDTO("Asignación Válida", 105.0, List.of(1L));
        assertThrows(IllegalArgumentException.class, () -> asignacionesService.actualizarAsignacion(1L, dtoUmbralAlto));

        NuevaAsignacionDTO dtoSinReglas = new NuevaAsignacionDTO("Asignación Válida", 70.0, List.of());
        assertThrows(IllegalArgumentException.class, () -> asignacionesService.actualizarAsignacion(1L, dtoSinReglas));
    }

    @Test
    @DisplayName("Eliminar asignación: realiza cascada y resecuencia mediante DAO")
    void testEliminarAsignacionCascada() {
        Long asignacionId = 1L;
        Asignacion existente = new Asignacion("Asignación a borrar", 70.0, new ArrayList<>());
        existente.setId(asignacionId);

        when(asignacionesDAO.buscarPorId(asignacionId)).thenReturn(existente);

        asignacionesService.eliminarAsignacion(asignacionId);

        verify(asignacionesDAO).eliminar(asignacionId);
    }

    @Test
    @DisplayName("Eliminar asignación: falla si no existe")
    void testEliminarAsignacionInexistenteLanzaExcepcion() {
        when(asignacionesDAO.buscarPorId(999L)).thenReturn(null);

        assertThrows(ExcepcionNegocio.class, () -> asignacionesService.eliminarAsignacion(999L));
    }

    @Test
    @DisplayName("Lógica de revalorización en AuditoriaService: recalcula puntajes, porcentajes y dictamen con nuevo umbral y reglas")
    void testRevalorizacionAuditoriaServiceRecalculaDictamen() {
        AuditoriaServiceImpl audService = new AuditoriaServiceImpl(reglaDAO, auditoriaDAO, gitHubClient, new EvaluadorReglaFactory());
        audService.setAsignacionesService(asignacionesService);

        Long asignacionId = 1L;
        Asignacion asignacion = new Asignacion("Asignacion 1", 80.0, new ArrayList<>());
        asignacion.setId(asignacionId);

        Regla r1 = new Regla();
        r1.setId(1L);
        r1.setNombreRepresentativo("README");
        r1.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        r1.setParametroExacto("README.md");
        r1.setPonderacion(50);

        Regla r2 = new Regla();
        r2.setId(2L);
        r2.setNombreRepresentativo("LICENSE");
        r2.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        r2.setParametroExacto("LICENSE");
        r2.setPonderacion(50);

        asignacion.setReglas(List.of(r1, r2));

        Proyecto proyecto = new Proyecto();
        proyecto.setUrlGithub("https://github.com/user/demo");

        Auditoria aud = new Auditoria();
        aud.setId(100L);
        aud.setProyecto(proyecto);
        aud.setAsignacion(asignacion);

        when(asignacionesDAO.consultarReglasDeAsignacion(asignacionId)).thenReturn(List.of(r1, r2));
        when(asignacionesDAO.consultarUmbralAprobacion(asignacionId)).thenReturn(50.0);
        when(auditoriaDAO.buscarPorAsignacionId(asignacionId)).thenReturn(List.of(aud));
        // Repositorio contiene README.md pero no LICENSE -> r1 cumple (50 pts), r2 no cumple (0 pts) -> 50 / 100 = 50%
        when(gitHubClient.obtenerEstructuraDelRepositorio(anyString())).thenReturn(List.of("README.md"));

        // Se cambia el umbral a 50%
        asignacion.setUmbralAprobacion(50.0);

        audService.revalorizarAuditoriasDeAsignacion(asignacionId);

        assertEquals(50, aud.getPuntajeObtenido());
        assertEquals(100, aud.getPuntajeMaximo());
        assertEquals(50.0, aud.getPorcentaje());
        assertEquals("APROBADA", aud.getDictamen());

        verify(auditoriaDAO).actualizarAuditoria(aud);
    }
}
