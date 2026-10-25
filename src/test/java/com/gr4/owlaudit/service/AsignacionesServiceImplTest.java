package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionesServiceImplTest {

    @Mock
    private AsignacionesDAO asignacionesDAO;

    @Mock
    private ReglaDAO reglaDAO;

    @InjectMocks
    private AsignacionesServiceImpl asignacionesService;

    private List<Regla> catalogoReglasVigentes;

    private Regla crearReglaMock(Long id, String nombre, EstadoVigencia vigencia) {
        Regla r = new Regla();
        r.setId(id);
        r.setNombreRepresentativo(nombre);
        r.setVigencia(vigencia);
        return r;
    }

    @BeforeEach
    void setUp() {
        catalogoReglasVigentes = List.of(
                crearReglaMock(1L, "Complejidad Ciclomática", EstadoVigencia.VIGENTE),
                crearReglaMock(2L, "Cobertura de Pruebas", EstadoVigencia.VIGENTE),
                crearReglaMock(3L, "Acoplamiento de Clases", EstadoVigencia.VIGENTE));
    }

    // 1. Escenarios 1.1 y 2.1: Creación exitosa y valores límite válidos (50% a
    // 100%)
    @ParameterizedTest(name = "Creación exitosa con umbral en límite/rango: ''{0}''%")
    @ValueSource(strings = { "50", "50.0", "50.01", "75", "85.5", "99.99", "100", "100.0" })
    @DisplayName("CA-1.1 y CA-2.1: Debe guardar y retornar la Asignacion cuando reglas y umbral son válidos")
    void debeCrearAsignacionExitosamenteConParametrosValidos(String umbralValido) {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(
                "Auditoría Calidad S1",
                umbralValido,
                List.of(1L, 2L));
        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(catalogoReglasVigentes);
        when(asignacionesDAO.crearAsignacion(any(Asignacion.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Asignacion resultado = asignacionesService.crearAsignacion(dto);

        assertNotNull(resultado, "La asignación retornada no debe ser nula");
        assertEquals("Auditoría Calidad S1", resultado.getNombre());
        assertEquals(Double.parseDouble(umbralValido), resultado.getUmbralAprobacion());
        assertEquals(2, resultado.getReglas().size());

        ArgumentCaptor<Asignacion> captor = ArgumentCaptor.forClass(Asignacion.class);
        verify(asignacionesDAO, times(1)).crearAsignacion(captor.capture());
        assertEquals(2, captor.getValue().getReglas().size());
    }

    // 2. Escenario 1.2: Error por omisión de reglas
    @ParameterizedTest(name = "Lista de reglas inválida: {0}")
    @NullAndEmptySource
    @DisplayName("CA-1.2: Debe lanzar IllegalArgumentException cuando no se seleccionan reglas (null o vacía)")
    void debeRechazarAsignacionSinReglasSeleccionadas(List<Long> reglasVaciasONulas) {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(
                "Auditoría Sin Reglas",
                "80",
                reglasVaciasONulas);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> asignacionesService.crearAsignacion(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("al menos una regla"));
        verifyNoInteractions(asignacionesDAO);
    }

    // 3. Escenario 1.3: Restricción a reglas exclusivamente vigentes
    @ParameterizedTest(name = "ID de regla no vigente o inexistente: {0}")
    @ValueSource(longs = { 99L, 4L, 0L, -1L })
    @DisplayName("CA-1.3: Debe rechazar la creación si alguna regla seleccionada no está VIGENTE")
    void debeRechazarAsignacionConReglasNoVigentes(Long idNoVigente) {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(
                "Auditoría Con Regla Inactiva",
                "80",
                List.of(1L, idNoVigente));
        when(reglaDAO.consultarReglasDeEvaluacionVigentes()).thenReturn(catalogoReglasVigentes);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> asignacionesService.crearAsignacion(dto));

        assertTrue(ex.getMessage().toLowerCase().contains("vigente"));
        verify(asignacionesDAO, never()).crearAsignacion(any());
    }

    // 4. Escenario 2.2: Error por umbral fuera de límites (< 50% o > 100%)
    @ParameterizedTest(name = "Umbral fuera de rango rechazado: ''{0}''%")
    @ValueSource(strings = { "-100", "-0.01", "0", "25", "49", "49.99", "49.999", "100.001", "100.01", "101", "200" })
    @DisplayName("CA-2.2: Debe rechazar la operación si el umbral es menor a 50% o mayor a 100%")
    void debeRechazarUmbralFueraDeLimites(String umbralFueraDeRango) {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(
                "Auditoría Umbral Fuera de Rango",
                umbralFueraDeRango,
                List.of(1L));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> asignacionesService.crearAsignacion(dto));

        assertTrue(ex.getMessage().contains("50%") && ex.getMessage().contains("100%"));
        verifyNoInteractions(asignacionesDAO);
    }

    // 5. Escenario 2.3: Error por umbral vacío o no numérico
    @ParameterizedTest(name = "Umbral vacío o no numérico rechazado: ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = { "   ", "abc", "75%", "ochenta", "75,5", "50..0", "NaN", "Infinity", "@#$" })
    @DisplayName("CA-2.3: Debe impedir el guardado si el umbral es vacío, nulo o no numérico")
    void debeRechazarUmbralVacioONoNumerico(String umbralInvalido) {
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(
                "Auditoría Umbral No Numérico",
                umbralInvalido,
                List.of(1L));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> asignacionesService.crearAsignacion(dto));

        assertTrue(
                ex.getMessage().toLowerCase().contains("umbral") || ex.getMessage().toLowerCase().contains("numérico"));
        verifyNoInteractions(asignacionesDAO);
    }

    @Test
    @DisplayName("listarAsignaciones: debe delegar la consulta al DAO y retornar las asignaciones")
    void debeDelegarListarAsignacionesAlDAO() {
        List<Asignacion> asignacionesEsperadas = List.of(new Asignacion("Asignación Test", 70.0, List.of()));
        when(asignacionesDAO.listarAsignaciones()).thenReturn(asignacionesEsperadas);

        List<Asignacion> resultado = asignacionesService.listarAsignaciones();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Asignación Test", resultado.get(0).getNombre());
        verify(asignacionesDAO, times(1)).listarAsignaciones();
    }
}
