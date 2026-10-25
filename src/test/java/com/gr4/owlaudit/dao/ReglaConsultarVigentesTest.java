package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias e integración para HU-02: Crear Asignación (Módulo Andrés).
 * Cubre:
 * - consultarReglasVigentes() en ReglaDAO.
 * - NuevaAsignacionDTO (encapsulamiento de nombre, umbral y reglas seleccionadas).
 */
@Tag("integracion")
class ReglaConsultarVigentesTest {

    private ReglaDAO reglaDAO;
    private List<Regla> reglasCreadas;

    @BeforeEach
    void setUp() {
        reglaDAO = new ReglaDAOImpl();
        reglasCreadas = new ArrayList<>();
    }

    @AfterEach
    void tearDown() {
        if (reglasCreadas != null && !reglasCreadas.isEmpty()) {
            try (org.hibernate.Session session = com.gr4.owlaudit.common.config.HibernateUtil.getSessionFactory().openSession()) {
                org.hibernate.Transaction tx = session.beginTransaction();
                for (Regla r : reglasCreadas) {
                    if (r != null && r.getId() != null) {
                        Regla persisted = session.get(Regla.class, r.getId());
                        if (persisted != null) {
                            session.remove(persisted);
                        }
                    }
                }
                tx.commit();
            } catch (Exception ignored) {
            }
        }
    }

    @Test
    @DisplayName("consultarReglasVigentes debe retornar únicamente las reglas con estado VIGENTE")
    void consultarReglasVigentes_debeRetornarSoloReglasConEstadoVigente() {
        // GIVEN: Una regla VIGENTE y una regla NO_VIGENTE registradas en la base de datos
        // Refactorización: Extract Method crearReglaPrueba(...)
        Regla reglaVigente = crearReglaPrueba("Regla Vigente Asignacion", TipoMotorEnum.PRESENCIA_ARCHIVO, "README.md",
                NivelSeveridadEnum.MEDIA, 20, EstadoVigencia.VIGENTE);
        Regla reglaNoVigente = crearReglaPrueba("Regla No Vigente Asignacion", TipoMotorEnum.RESTRICCION_ARCHIVOS, ".env",
                NivelSeveridadEnum.ALTA, 30, EstadoVigencia.NO_VIGENTE);

        // WHEN: Se consultan las reglas vigentes para la creación de una nueva asignación
        List<Regla> vigentes = reglaDAO.consultarReglasVigentes();

        // THEN: La lista no debe ser nula, debe incluir la regla VIGENTE y excluir la NO_VIGENTE
        assertNotNull(vigentes, "La lista de reglas vigentes no debe ser nula");

        // Refactorización: Replace Temp with Query (contieneReglaPorNombre)
        assertTrue(contieneReglaPorNombre(vigentes, reglaVigente.getNombreRepresentativo()),
                "Debe contener la regla registrada como VIGENTE");
        assertFalse(contieneReglaPorNombre(vigentes, reglaNoVigente.getNombreRepresentativo()),
                "No debe contener reglas en estado NO_VIGENTE");

        // Validar que todas las entidades retornadas tengan vigencia VIGENTE
        for (Regla r : vigentes) {
            assertEquals(EstadoVigencia.VIGENTE, r.getVigencia(), "Cada regla retornada debe tener estado VIGENTE");
            assertTrue(r.esVigente(), "El método de dominio esVigente() debe ser true");
        }
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods)
     * Extrae la creación, parametrización y persistencia de reglas de prueba.
     */
    private Regla crearReglaPrueba(String prefijo, TipoMotorEnum motor, String parametro,
                                   NivelSeveridadEnum severidad, int ponderacion, EstadoVigencia vigencia) {
        Regla regla = new Regla();
        regla.setNombreRepresentativo(prefijo + " " + System.nanoTime());
        regla.setTipoMotor(motor);
        regla.setParametroExacto(parametro);
        regla.setNivelSeveridad(severidad);
        regla.setPonderacion(ponderacion);
        regla.setVigencia(vigencia);
        reglaDAO.guardar(regla);
        reglasCreadas.add(regla);
        return regla;
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods)
     * Reemplaza variables temporales booleanas por una consulta directa sobre la colección.
     */
    private boolean contieneReglaPorNombre(List<Regla> reglas, String nombre) {
        return reglas.stream().anyMatch(r -> nombre.equals(r.getNombreRepresentativo()));
    }

    @Test
    @DisplayName("NuevaAsignacionDTO debe encapsular nombre, umbral y lista de identificadores de reglas")
    void nuevaAsignacionDTO_debeEncapsularDatosCorrectamente() {
        // GIVEN: Datos para una nueva asignación
        String nombre = "Laboratorio 1 - Buenas Prácticas";
        Double umbral = 75.0;
        List<Long> idsReglas = List.of(10L, 20L, 30L);

        // WHEN: Se instancia el DTO
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(nombre, umbral, idsReglas);

        // THEN: Los valores deben recuperarse fielmente
        assertEquals("Laboratorio 1 - Buenas Prácticas", dto.getNombre());
        assertEquals(75.0, dto.getUmbralAprobacion(), 0.001);
        assertNotNull(dto.getIdsReglas());
        assertEquals(3, dto.getIdsReglas().size());
        assertTrue(dto.getIdsReglas().containsAll(List.of(10L, 20L, 30L)));
        assertTrue(dto.tieneReglas());

        // Fowler: Encapsulate Collection (la lista retornada debe ser inmutable hacia el exterior)
        assertThrows(UnsupportedOperationException.class, () -> dto.getIdsReglas().add(40L));

        // Manipulación controlada mediante métodos de dominio
        dto.agregarReglaId(40L);
        assertEquals(4, dto.getIdsReglas().size());
    }
}
