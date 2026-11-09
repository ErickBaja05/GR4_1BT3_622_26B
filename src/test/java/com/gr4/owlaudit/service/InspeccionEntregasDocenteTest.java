package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaResumenDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.Proyecto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InspeccionEntregasDocenteTest {

    @Mock
    private AuditoriaDAO auditoriaDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private ProyectoDAO proyectoDAO;

    @Mock
    private AsignacionesService asignacionesService;

    @Mock
    private ValidadorAccesoCurso validadorAccesoCurso;

    private AuditoriaServiceImpl auditoriaService;

    private Asignacion asignacionMock;
    private Curso cursoMock;
    private Docente docenteTitular;

    @BeforeEach
    void setUp() {
        auditoriaService = new AuditoriaServiceImpl(
                reglaDAO,
                auditoriaDAO,
                null,
                null,
                proyectoDAO,
                asignacionesService,
                validadorAccesoCurso
        );

        docenteTitular = new Docente();
        docenteTitular.setId(10L);
        docenteTitular.setNombre("Profesor Xavier");
        docenteTitular.setCorreo("docente@owlaudit.com");

        cursoMock = new Curso();
        cursoMock.setId(100L);
        cursoMock.setNombre("Metodologías Ágiles");
        cursoMock.setDocente(docenteTitular);

        asignacionMock = new Asignacion();
        asignacionMock.setId(1L);
        asignacionMock.setNombre("Práctica XP - TDD");
        asignacionMock.setCurso(cursoMock);
    }

    @Test
    @DisplayName("CA-3 Escenario 3.1: Docente ajeno intenta consultar entregas y se deniega acceso")
    void testConsultarEntregas_DocenteNoEsTitular_LanzaExcepcionNegocio() {
        Long asignacionId = 1L;
        Long docenteAjenoId = 99L;

        when(asignacionesService.buscarPorId(asignacionId)).thenReturn(asignacionMock);
        doThrow(new ExcepcionNegocio("No tienes permisos para acceder a las entregas de un curso del que no eres titular"))
                .when(validadorAccesoCurso).validarTitularidadDocenteCurso(cursoMock, docenteAjenoId);

        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class, () -> {
            auditoriaService.consultarEntregasDeAsignacionParaDocente(asignacionId, docenteAjenoId);
        });

        assertTrue(excepcion.getMessage().contains("No tienes permisos"));
        verify(auditoriaDAO, never()).buscarPorAsignacionId(anyLong());
    }

    @Test
    @DisplayName("CA-1 Escenario 1.1: Docente titular consulta asignación con entregas y obtiene listado mapeado")
    void testConsultarEntregas_ConEntregas_RetornaListaMapeadaCorrectamente() {
        Long asignacionId = 1L;
        Long docenteId = 10L;

        when(asignacionesService.buscarPorId(asignacionId)).thenReturn(asignacionMock);

        Estudiante estudiante = new Estudiante(20L, "Juan Pérez", "juan.perez@epn.edu.ec", "pass");
        Proyecto proyecto = new Proyecto();
        proyecto.setId(5L);
        proyecto.setNombre("Proyecto Final XP");
        proyecto.setUrlGithub("https://github.com/juanperez/proyecto-xp");

        Auditoria entrega = new Auditoria();
        entrega.setId(101L);
        entrega.setNumeroEnAsignacion(1L);
        entrega.setAsignacion(asignacionMock);
        entrega.setEstudiante(estudiante);
        entrega.setProyecto(proyecto);
        entrega.setFechaHora(LocalDateTime.of(2026, 11, 8, 14, 30));
        entrega.setPuntajeObtenido(90);
        entrega.setPuntajeMaximo(100);
        entrega.setPorcentaje(90.0);
        entrega.setDictamen("APROBADA");
        entrega.setTipoAuditoria("ASIGNACION");

        when(auditoriaDAO.buscarPorAsignacionId(asignacionId)).thenReturn(Collections.singletonList(entrega));

        List<AuditoriaResumenDTO> resultado = auditoriaService.consultarEntregasDeAsignacionParaDocente(asignacionId, docenteId);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());

        AuditoriaResumenDTO dto = resultado.get(0);
        assertEquals(101L, dto.getId());
        assertEquals("Juan Pérez", dto.getNombreEstudiante());
        assertEquals("juan.perez@epn.edu.ec", dto.getCorreoEstudiante());
        assertEquals("Proyecto Final XP", dto.getNombreProyecto());
        assertEquals("https://github.com/juanperez/proyecto-xp", dto.getUrlRepositorio());
        assertEquals(1L, dto.getNumeroEnAsignacion());
        assertEquals(90, dto.getPuntajeObtenido());
        assertEquals(100, dto.getPuntajeMaximo());
        assertEquals(90.0, dto.getPorcentaje());
        assertEquals("APROBADA", dto.getDictamen());
    }

    @Test
    @DisplayName("CA-1 Escenario 1.2: Asignación sin entregas retorna lista vacía")
    void testConsultarEntregas_SinEntregas_RetornaListaVacia() {
        Long asignacionId = 1L;
        Long docenteId = 10L;

        when(asignacionesService.buscarPorId(asignacionId)).thenReturn(asignacionMock);
        when(auditoriaDAO.buscarPorAsignacionId(asignacionId)).thenReturn(Collections.emptyList());

        List<AuditoriaResumenDTO> resultado = auditoriaService.consultarEntregasDeAsignacionParaDocente(asignacionId, docenteId);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
