package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionesServiceTest {

    @Mock
    private AsignacionesDAO asignacionesDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private ProyectoDAO proyectoDAO;

    @InjectMocks
    private AsignacionesServiceImpl asignacionesService;

    private Asignacion asignacionPrueba;
    private Regla reglaVigente;
    private Regla reglaNoVigente;
    private Proyecto proyectoPrueba;

    @BeforeEach
    void setUp() {
        reglaVigente = crearReglaFixture(101L, "Cobertura de Pruebas Unitarias", TipoMotorEnum.PRESENCIA_ARCHIVO, "pom.xml", NivelSeveridadEnum.ALTA, 30, EstadoVigencia.VIGENTE);
        reglaNoVigente = crearReglaFixture(102L, "Regla Histórica Desactivada", TipoMotorEnum.CONVENCION_COMMITS, "feat|fix", NivelSeveridadEnum.MEDIA, 20, EstadoVigencia.NO_VIGENTE);
        asignacionPrueba = crearAsignacionFixture(1L, "Asignación Proyecto Final 2026", 80.0, List.of(reglaVigente, reglaNoVigente));
        proyectoPrueba = crearProyectoFixture(501L, "OwlAudit Core Engine", "https://github.com/org/owlaudit-core");
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     * Métodos utilitarios de ensamblado de fixtures de prueba.
     */
    private Regla crearReglaFixture(Long id, String nombre, TipoMotorEnum motor, String param, NivelSeveridadEnum severidad, int ponderacion, EstadoVigencia vigencia) {
        Regla r = new Regla();
        r.setId(id);
        r.setNombreRepresentativo(nombre);
        r.setTipoMotor(motor);
        r.setParametroExacto(param);
        r.setNivelSeveridad(severidad);
        r.setPonderacion(ponderacion);
        r.setVigencia(vigencia);
        return r;
    }

    private Asignacion crearAsignacionFixture(Long id, String nombre, double umbral, List<Regla> reglas) {
        Asignacion a = new Asignacion(nombre, umbral, reglas);
        a.setId(id);
        return a;
    }

    private Proyecto crearProyectoFixture(Long id, String nombre, String url) {
        Proyecto p = new Proyecto();
        p.setId(id);
        p.setNombre(nombre);
        p.setUrlGithub(url);
        return p;
    }

    @Test
    @DisplayName("consultarAsignacionesDisponibles debe delegar al DAO y retornar las asignaciones disponibles")
    void consultarAsignacionesDisponibles_debeRetornarListaDeAsignacionesDesdeDAO() {
        // GIVEN
        List<Asignacion> asignacionesEsperadas = List.of(asignacionPrueba);
        when(asignacionesDAO.consultarAsignacionesDisponibles()).thenReturn(asignacionesEsperadas);

        // WHEN
        List<Asignacion> resultado = asignacionesService.consultarAsignacionesDisponibles();

        // THEN
        assertNotNull(resultado, "El resultado de asignaciones disponibles no debe ser nulo");
        assertEquals(1, resultado.size(), "Debe retornar la cantidad exacta de asignaciones");
        assertEquals("Asignación Proyecto Final 2026", resultado.get(0).getNombre());
        verify(asignacionesDAO, times(1)).consultarAsignacionesDisponibles();
    }

    @Test
    @DisplayName("consultarReglasDeAsignacion debe retornar todas las reglas asociadas sin filtrar vigencia (CA-1)")
    void consultarReglasDeAsignacion_debeRetornarTodasLasReglasIncluyendoNoVigentesSegunCA1() {
        // GIVEN: Una asignación vinculada a reglas vigentes y desactivadas en catálogo
        Long idAsignacion = 1L;
        List<Regla> reglasVinculadas = List.of(reglaVigente, reglaNoVigente);
        when(asignacionesDAO.consultarReglasDeAsignacion(idAsignacion)).thenReturn(reglasVinculadas);

        // WHEN
        List<Regla> resultado = asignacionesService.consultarReglasDeAsignacion(idAsignacion);

        // THEN
        assertNotNull(resultado, "La lista de reglas retornada no debe ser nula");
        assertEquals(2, resultado.size(), "Debe retornar todas las reglas vinculadas históricamente");
        assertTrue(resultado.stream().anyMatch(r -> r.getVigencia() == EstadoVigencia.NO_VIGENTE),
                "Debe incluir reglas con vigencia NO_VIGENTE según Criterio de Aceptación CA-1");
        verify(asignacionesDAO, times(1)).consultarReglasDeAsignacion(idAsignacion);
    }

    @Test
    @DisplayName("consultarUmbralAprobacion debe retornar el umbral configurado consultando al DAO")
    void consultarUmbralAprobacion_debeRetornarUmbralConfiguradoDesdeDAO() {
        // GIVEN
        Long idAsignacion = 1L;
        when(asignacionesDAO.consultarUmbralAprobacion(idAsignacion)).thenReturn(80.0);

        // WHEN
        double umbral = asignacionesService.consultarUmbralAprobacion(idAsignacion);

        // THEN
        assertEquals(80.0, umbral, 0.001, "El umbral de aprobación debe coincidir con el valor configurado");
        verify(asignacionesDAO, times(1)).consultarUmbralAprobacion(idAsignacion);
    }

    @Test
    @DisplayName("generarResumenAsignacion debe armar y retornar ResumenAsignacionDTO con reglas, umbral y proyectos")
    void generarResumenAsignacion_debeRetornarResumenAsignacionDTOConReglasUmbralYProyectos() {
        // GIVEN
        Long idAsignacion = 1L;
        List<Regla> reglasVinculadas = List.of(reglaVigente, reglaNoVigente);
        List<Proyecto> proyectos = List.of(proyectoPrueba);

        when(asignacionesDAO.consultarReglasDeAsignacion(idAsignacion)).thenReturn(reglasVinculadas);
        when(asignacionesDAO.consultarUmbralAprobacion(idAsignacion)).thenReturn(80.0);
        when(asignacionesDAO.consultarAsignacionesDisponibles()).thenReturn(List.of(asignacionPrueba));
        when(proyectoDAO.listarTodos()).thenReturn(proyectos);

        // WHEN
        ResumenAsignacionDTO resumen = asignacionesService.generarResumenAsignacion(idAsignacion);

        // THEN
        assertNotNull(resumen, "El resumen de asignación no debe ser nulo");
        assertEquals(idAsignacion, resumen.getIdAsignacion(), "El ID de la asignación debe coincidir");
        assertEquals("Asignación Proyecto Final 2026", resumen.getNombreAsignacion(), "El nombre de la asignación debe coincidir");
        assertEquals(80.0, resumen.getUmbralAprobacion(), 0.001, "El umbral de aprobación debe coincidir");
        assertEquals(2, resumen.getReglasAsociadas().size(), "Debe contener las reglas asociadas");
        assertEquals(1, resumen.getProyectosEstudiante().size(), "Debe contener los proyectos registrados del estudiante");

        verify(asignacionesDAO, times(1)).consultarReglasDeAsignacion(idAsignacion);
        verify(asignacionesDAO, times(1)).consultarUmbralAprobacion(idAsignacion);
        verify(proyectoDAO, times(1)).listarTodos();
    }

    @Test
    @DisplayName("generarResumenAsignacion cuando la asignación no existe en disponibles debe armar resumen con nombre nulo")
    void generarResumenAsignacion_cuandoAsignacionNoExisteEnDisponibles_debeArmarResumenConNombreNulo() {
        // GIVEN
        Long idAsignacion = 999L;
        when(asignacionesDAO.consultarReglasDeAsignacion(idAsignacion)).thenReturn(List.of());
        when(asignacionesDAO.consultarUmbralAprobacion(idAsignacion)).thenReturn(0.0);
        when(asignacionesDAO.consultarAsignacionesDisponibles()).thenReturn(List.of(asignacionPrueba));
        when(proyectoDAO.listarTodos()).thenReturn(List.of());

        // WHEN
        ResumenAsignacionDTO resumen = asignacionesService.generarResumenAsignacion(idAsignacion);

        // THEN
        assertNotNull(resumen);
        assertEquals(999L, resumen.getIdAsignacion());
        assertNull(resumen.getNombreAsignacion(), "El nombre debe ser nulo si la asignación no se encontró en disponibles");
        assertEquals(0.0, resumen.getUmbralAprobacion());
        assertTrue(resumen.getReglasAsociadas().isEmpty());
        assertTrue(resumen.getProyectosEstudiante().isEmpty());
    }
}
