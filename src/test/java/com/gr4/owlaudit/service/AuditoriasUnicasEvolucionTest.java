package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuditoriasUnicasEvolucionTest {

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private AsignacionesService asignacionesService;

    private EvolucionServiceImpl evolucionService;
    private AuditoriaServiceImpl auditoriaService;

    @BeforeEach
    void setUp() {
        evolucionService = new EvolucionServiceImpl(auditoriaDAO);
        auditoriaService = new AuditoriaServiceImpl(reglaDAO, auditoriaDAO, null, null, proyectoDAO, asignacionesService);
    }

    @Test
    @DisplayName("Codificación y formato de IDs visibles único: General (AG-X) vs Asignación (AA-X)")
    void testFormatoCodigoVisibleAuditorias() {
        Auditoria audGeneral = new Auditoria();
        audGeneral.setId(10L);
        audGeneral.setTipoAuditoria("GENERAL");
        audGeneral.setNumeroEnProyecto(1L);

        assertTrue(audGeneral.esAuditoriaGeneral());
        assertFalse(audGeneral.esAuditoriaAsignacion());
        assertEquals("AG-1", audGeneral.getCodigoVisible());
        assertEquals("Auditoría General #1", audGeneral.getEtiquetaVisible());

        Asignacion asig = new Asignacion();
        asig.setId(5L);
        asig.setNombre("Sprint 1");

        Auditoria audAsignacion = new Auditoria();
        audAsignacion.setId(25L);
        audAsignacion.setTipoAuditoria("ASIGNACION");
        audAsignacion.setAsignacion(asig);
        audAsignacion.setNumeroEnAsignacion(1L);

        assertTrue(audAsignacion.esAuditoriaAsignacion());
        assertFalse(audAsignacion.esAuditoriaGeneral());
        assertEquals("AA-1", audAsignacion.getCodigoVisible());
        assertEquals("Auditoría Asignación #1 (Sprint 1)", audAsignacion.getEtiquetaVisible());
    }

    @Test
    @DisplayName("Evolución: consultar historial filtra correctamente por ámbito General")
    void testConsultarHistorialGenerales() {
        Long proyectoId = 1L;
        Auditoria ag1 = new Auditoria();
        ag1.setId(101L);
        ag1.setNumeroEnProyecto(1L);
        ag1.setFechaHora(LocalDateTime.now().minusDays(2));
        ag1.setPuntajeObtenido(70);
        ag1.setPuntajeMaximo(100);
        ag1.setPorcentaje(70.0);

        Auditoria ag2 = new Auditoria();
        ag2.setId(102L);
        ag2.setNumeroEnProyecto(2L);
        ag2.setFechaHora(LocalDateTime.now().minusDays(1));
        ag2.setPuntajeObtenido(85);
        ag2.setPuntajeMaximo(100);
        ag2.setPorcentaje(85.0);

        when(auditoriaDAO.consultarHistorialAuditoriasGenerales(proyectoId)).thenReturn(List.of(ag1, ag2));

        EvolucionDTO dto = new EvolucionDTO(proyectoId);
        dto.setTipoAuditoria("GENERAL");

        ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);

        assertNotNull(resumen);
        assertEquals(2, resumen.getAuditorias().size());
        assertEquals("AG-1", resumen.getAuditorias().get(0).getCodigoVisible());
        assertEquals("AG-2", resumen.getAuditorias().get(1).getCodigoVisible());
        verify(auditoriaDAO).consultarHistorialAuditoriasGenerales(proyectoId);
    }

    @Test
    @DisplayName("Evolución: consultar historial filtra correctamente por ámbito Asignación")
    void testConsultarHistorialPorAsignacion() {
        Long proyectoId = 1L;
        Long asignacionId = 7L;

        Asignacion asig = new Asignacion();
        asig.setId(asignacionId);
        asig.setNombre("Entrega Final");

        Auditoria aa1 = new Auditoria();
        aa1.setId(201L);
        aa1.setAsignacion(asig);
        aa1.setTipoAuditoria("ASIGNACION");
        aa1.setNumeroEnAsignacion(1L);
        aa1.setFechaHora(LocalDateTime.now().minusDays(3));
        aa1.setPuntajeObtenido(60);
        aa1.setPuntajeMaximo(100);
        aa1.setPorcentaje(60.0);

        Auditoria aa2 = new Auditoria();
        aa2.setId(202L);
        aa2.setAsignacion(asig);
        aa2.setTipoAuditoria("ASIGNACION");
        aa2.setNumeroEnAsignacion(2L);
        aa2.setFechaHora(LocalDateTime.now().minusDays(1));
        aa2.setPuntajeObtenido(90);
        aa2.setPuntajeMaximo(100);
        aa2.setPorcentaje(90.0);

        when(auditoriaDAO.consultarHistorialAuditoriasAsignacion(proyectoId, asignacionId)).thenReturn(List.of(aa1, aa2));

        EvolucionDTO dto = new EvolucionDTO(proyectoId);
        dto.setTipoAuditoria("ASIGNACION");
        dto.setAsignacionId(asignacionId);

        ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);

        assertNotNull(resumen);
        assertEquals(2, resumen.getAuditorias().size());
        assertEquals("AA-1", resumen.getAuditorias().get(0).getCodigoVisible());
        assertEquals("AA-2", resumen.getAuditorias().get(1).getCodigoVisible());
        verify(auditoriaDAO).consultarHistorialAuditoriasAsignacion(proyectoId, asignacionId);
    }

    @Test
    @DisplayName("Evolución: rechaza comparar una auditoría general con una de asignación")
    void testRechazaCompararGeneralConAsignacion() {
        Auditoria audGen = new Auditoria();
        audGen.setId(10L);
        audGen.setTipoAuditoria("GENERAL");
        audGen.setNumeroEnProyecto(1L);
        audGen.setFechaHora(LocalDateTime.now().minusDays(2));

        Asignacion asig = new Asignacion();
        asig.setId(5L);
        asig.setNombre("Tarea 1");

        Auditoria audAsig = new Auditoria();
        audAsig.setId(20L);
        audAsig.setTipoAuditoria("ASIGNACION");
        audAsig.setAsignacion(asig);
        audAsig.setNumeroEnAsignacion(1L);
        audAsig.setFechaHora(LocalDateTime.now().minusDays(1));

        when(auditoriaDAO.buscarPorId(10L)).thenReturn(audGen);
        when(auditoriaDAO.buscarPorId(20L)).thenReturn(audAsig);

        EvolucionDTO dto = new EvolucionDTO(1L, 10L, 20L);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                evolucionService.indicarDosAuditoriasAComparar(dto));

        assertEquals("No se pueden comparar auditorías generales con auditorías de asignación.", ex.getMessage());
    }

    @Test
    @DisplayName("Evolución: rechaza comparar dos auditorías de asignaciones distintas")
    void testRechazaCompararAuditoriasDeDiferentesAsignaciones() {
        Asignacion asig1 = new Asignacion();
        asig1.setId(100L);
        asig1.setNombre("Asignacion A");

        Asignacion asig2 = new Asignacion();
        asig2.setId(200L);
        asig2.setNombre("Asignacion B");

        Auditoria aud1 = new Auditoria();
        aud1.setId(51L);
        aud1.setTipoAuditoria("ASIGNACION");
        aud1.setAsignacion(asig1);
        aud1.setNumeroEnAsignacion(1L);
        aud1.setFechaHora(LocalDateTime.now().minusDays(3));

        Auditoria aud2 = new Auditoria();
        aud2.setId(52L);
        aud2.setTipoAuditoria("ASIGNACION");
        aud2.setAsignacion(asig2);
        aud2.setNumeroEnAsignacion(1L);
        aud2.setFechaHora(LocalDateTime.now().minusDays(1));

        when(auditoriaDAO.buscarPorId(51L)).thenReturn(aud1);
        when(auditoriaDAO.buscarPorId(52L)).thenReturn(aud2);

        EvolucionDTO dto = new EvolucionDTO(1L, 51L, 52L);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                evolucionService.indicarDosAuditoriasAComparar(dto));

        assertEquals("Solo se pueden comparar auditorías pertenecientes a la misma asignación académica.", ex.getMessage());
    }

    @Test
    @DisplayName("Evolución: permite comparar dos auditorías de la MISMA asignación y muestra IDs AA-X")
    void testPermiteCompararAuditoriasDeMismaAsignacion() {
        Asignacion asig = new Asignacion();
        asig.setId(300L);
        asig.setNombre("Proyecto Final");

        Auditoria aud1 = new Auditoria();
        aud1.setId(81L);
        aud1.setTipoAuditoria("ASIGNACION");
        aud1.setAsignacion(asig);
        aud1.setNumeroEnAsignacion(1L);
        aud1.setFechaHora(LocalDateTime.now().minusDays(4));
        aud1.setPuntajeObtenido(50);
        aud1.setPuntajeMaximo(100);
        aud1.setPorcentaje(50.0);

        Auditoria aud2 = new Auditoria();
        aud2.setId(82L);
        aud2.setTipoAuditoria("ASIGNACION");
        aud2.setAsignacion(asig);
        aud2.setNumeroEnAsignacion(2L);
        aud2.setFechaHora(LocalDateTime.now().minusDays(1));
        aud2.setPuntajeObtenido(80);
        aud2.setPuntajeMaximo(100);
        aud2.setPorcentaje(80.0);

        Regla r = new Regla();
        r.setId(1L);
        r.setNombreRepresentativo("README");
        r.setPonderacion(100);
        r.setNivelSeveridad(NivelSeveridadEnum.MEDIA);

        ResultadoRegla res1 = new ResultadoRegla(false, 0, r, aud1);
        ResultadoRegla res2 = new ResultadoRegla(true, 100, r, aud2);

        when(auditoriaDAO.buscarPorId(81L)).thenReturn(aud1);
        when(auditoriaDAO.buscarPorId(82L)).thenReturn(aud2);
        when(auditoriaDAO.consultarResultadosDeReglas(81L, 82L)).thenReturn(List.of(res1, res2));

        EvolucionDTO dto = new EvolucionDTO(1L, 81L, 82L);
        dto.setTipoAuditoria("ASIGNACION");
        dto.setAsignacionId(300L);

        EvolucionFinalDTO evolucion = evolucionService.indicarDosAuditoriasAComparar(dto);

        assertNotNull(evolucion);
        assertEquals("AA-1", evolucion.getCodigoVisibleBase());
        assertEquals("AA-2", evolucion.getCodigoVisibleComparada());
        assertEquals("Proyecto Final", evolucion.getNombreAsignacion());
        assertEquals(30, evolucion.getVariacionPuntaje());
        assertEquals(1, evolucion.getTotalCorregidos());
    }

    @Test
    @DisplayName("Detalle de auditoría: convierte entidad Auditoria a ResultadoDTO para visualización detallada")
    void testObtenerDetalleAuditoria() {
        Asignacion asig = new Asignacion();
        asig.setId(10L);
        asig.setNombre("Asignacion 1");
        asig.setUmbralAprobacion(75.0);

        Auditoria aud = new Auditoria();
        aud.setId(99L);
        aud.setTipoAuditoria("ASIGNACION");
        aud.setAsignacion(asig);
        aud.setNumeroEnAsignacion(3L);
        aud.setPuntajeObtenido(80);
        aud.setPuntajeMaximo(100);
        aud.setPorcentaje(80.0);
        aud.setDictamen("APROBADA");

        Regla r = new Regla();
        r.setId(2L);
        r.setNombreRepresentativo("Estructura");
        r.setPonderacion(50);
        r.setNivelSeveridad(NivelSeveridadEnum.ALTA);

        ResultadoRegla rr = new ResultadoRegla(true, 50, r, aud);
        aud.setResultados(new ArrayList<>(List.of(rr)));

        when(auditoriaDAO.buscarPorId(99L)).thenReturn(aud);

        ResultadoDTO detalle = auditoriaService.obtenerDetalleAuditoria(99L);

        assertNotNull(detalle);
        assertEquals(99L, detalle.getIdAuditoria());
        assertEquals(3L, detalle.getNumeroEnAsignacion());
        assertEquals("AA-3", detalle.getCodigoVisible());
        assertEquals("APROBADA", detalle.getDictamen());
        assertEquals(75.0, detalle.getUmbralAprobacion());
        assertEquals(1, detalle.getDetalles().size());
        assertEquals("Estructura", detalle.getDetalles().get(0).getNombreRegla());
    }
}
