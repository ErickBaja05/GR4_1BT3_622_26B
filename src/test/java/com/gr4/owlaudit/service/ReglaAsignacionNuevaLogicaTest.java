package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionReglaDAO;
import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.dto.ReglaAsignacionDTO;
import com.gr4.owlaudit.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReglaAsignacionNuevaLogicaTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private CursoDAO cursoDAO;

    @Mock
    private AsignacionesDAO asignacionesDAO;

    @Mock
    private AsignacionReglaDAO asignacionReglaDAO;

    private ReglaServiceImpl reglaService;
    private AsignacionesServiceImpl asignacionesService;
    private ValidadorRegla validadorRegla;

    private Curso curso1;
    private Curso curso2;

    @BeforeEach
    void setUp() {
        validadorRegla = new ValidadorRegla();
        reglaService = new ReglaServiceImpl(reglaDAO, cursoDAO, validadorRegla);

        asignacionesService = new AsignacionesServiceImpl(asignacionesDAO, reglaDAO);
        asignacionesService.setAsignacionReglaDAO(asignacionReglaDAO);

        Docente docente = new Docente(1L, "Docente Titular", "docente@epn.edu.ec", "pass");
        curso1 = new Curso(docente, "CSW-2026B", "Construcción de Software", "2026B", "Curso 1");
        curso1.setId(10L);

        curso2 = new Curso(docente, "CAL-2026B", "Calidad de Software", "2026B", "Curso 2");
        curso2.setId(20L);

        when(cursoDAO.buscarPorId(10L)).thenReturn(curso1);
        when(cursoDAO.buscarPorId(20L)).thenReturn(curso2);
    }

    @Test
    @DisplayName("Crear regla: docente registra regla únicamente hasta parámetro exacto sin severidad ni ponderación")
    void crearRegla_soloHastaParametroExacto_exitoso() {
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Verificar README", "Exige README", "README.md");
        dto.setIdCurso(10L);

        when(reglaDAO.existeNombreEnCurso("Verificar README", 10L)).thenReturn(false);

        reglaService.registrarReglaEvaluacionTecnica(dto);

        ArgumentCaptor<Regla> reglaCaptor = ArgumentCaptor.forClass(Regla.class);
        verify(reglaDAO).guardar(reglaCaptor.capture());

        Regla guardada = reglaCaptor.getValue();
        assertEquals("Verificar README", guardada.getNombreRepresentativo());
        assertEquals("README.md", guardada.getParametroExacto());
        assertEquals(TipoMotorEnum.PRESENCIA_ARCHIVO, guardada.getTipoMotor());
        assertEquals(curso1, guardada.getCurso());
        assertEquals(EstadoVigencia.VIGENTE, guardada.getVigencia());
        assertNull(guardada.getNivelSeveridad(), "La regla no debe almacenar nivel de severidad");
        assertEquals(0, guardada.getPonderacion(), "La regla no debe almacenar ponderación");
    }

    @Test
    @DisplayName("Crear asignación: guarda la configuración personalizada de severidad y ponderación en AsignacionRegla")
    void crearAsignacion_persisteAsignacionReglaConSeveridadYPonderacionPersonalizadas() {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO();
        dto.setNombre("Taller 1");
        dto.setUmbralAprobacion(70.0);
        dto.setIdCurso(10L);

        Regla r1 = new Regla();
        r1.setId(101L);
        r1.setVigencia(EstadoVigencia.VIGENTE);

        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(List.of(r1));

        ReglaAsignacionDTO rad1 = new ReglaAsignacionDTO(101L, NivelSeveridadEnum.ALTA, 35);
        dto.agregarReglaConfigurada(rad1);

        Asignacion asigCreada = new Asignacion("Taller 1", 70.0, List.of(r1));
        asigCreada.setId(1L);
        when(asignacionesDAO.crearAsignacion(any(Asignacion.class))).thenReturn(asigCreada);

        Asignacion resultado = asignacionesService.crearAsignacion(dto);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AsignacionRegla>> arCaptor = ArgumentCaptor.forClass(List.class);
        verify(asignacionReglaDAO).guardar(arCaptor.capture());

        List<AsignacionRegla> listaGuardada = arCaptor.getValue();
        assertEquals(1, listaGuardada.size());
        AsignacionRegla ar = listaGuardada.get(0);
        assertEquals(1L, ar.getAsignacionId());
        assertEquals(101L, ar.getReglaId());
        assertEquals(NivelSeveridadEnum.ALTA, ar.getNivelSeveridad());
        assertEquals(35, ar.getPonderacion());
    }
}
