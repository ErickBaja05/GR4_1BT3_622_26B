package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
        String nombreVigente = "Regla Vigente Asignacion " + System.nanoTime();
        Regla reglaVigente = new Regla();
        reglaVigente.setNombreRepresentativo(nombreVigente);
        reglaVigente.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaVigente.setParametroExacto("README.md");
        reglaVigente.setNivelSeveridad(NivelSeveridadEnum.MEDIA);
        reglaVigente.setPonderacion(20);
        reglaVigente.setVigencia(EstadoVigencia.VIGENTE);
        reglaDAO.guardar(reglaVigente);
        reglasCreadas.add(reglaVigente);

        String nombreNoVigente = "Regla No Vigente Asignacion " + System.nanoTime();
        Regla reglaNoVigente = new Regla();
        reglaNoVigente.setNombreRepresentativo(nombreNoVigente);
        reglaNoVigente.setTipoMotor(TipoMotorEnum.RESTRICCION_ARCHIVOS);
        reglaNoVigente.setParametroExacto(".env");
        reglaNoVigente.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        reglaNoVigente.setPonderacion(30);
        reglaNoVigente.setVigencia(EstadoVigencia.NO_VIGENTE);
        reglaDAO.guardar(reglaNoVigente);
        reglasCreadas.add(reglaNoVigente);

        // WHEN: Se consultan las reglas vigentes para la creación de una nueva asignación
        List<Regla> vigentes = reglaDAO.consultarReglasVigentes();

        // THEN: La lista no debe ser nula, debe incluir la regla VIGENTE y excluir la NO_VIGENTE
        assertNotNull(vigentes, "La lista de reglas vigentes no debe ser nula");
        boolean contieneVigente = vigentes.stream()
                .anyMatch(r -> nombreVigente.equals(r.getNombreRepresentativo()));
        boolean contieneNoVigente = vigentes.stream()
                .anyMatch(r -> nombreNoVigente.equals(r.getNombreRepresentativo()));

        assertTrue(contieneVigente, "Debe contener la regla registrada como VIGENTE");
        assertFalse(contieneNoVigente, "No debe contener reglas en estado NO_VIGENTE");

        // Validar que todas las entidades retornadas tengan vigencia VIGENTE
        for (Regla r : vigentes) {
            assertEquals(EstadoVigencia.VIGENTE, r.getVigencia(), "Cada regla retornada debe tener estado VIGENTE");
            assertTrue(r.esVigente(), "El método de dominio esVigente() debe ser true");
        }
    }

    @Test
    @DisplayName("NuevaAsignacionDTO debe encapsular nombre, umbral y lista de identificadores de reglas")
    void nuevaAsignacionDTO_debeEncapsularDatosCorrectamente() {
        // GIVEN: Datos para una nueva asignación
        String nombre = "Laboratorio 1 - Buenas Prácticas";
        String umbral = "75.0";
        List<Long> reglasIds = List.of(10L, 20L, 30L);

        // WHEN: Se instancia el DTO
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(nombre, umbral, reglasIds);

        // THEN: Los valores deben recuperarse fielmente
        assertEquals("Laboratorio 1 - Buenas Prácticas", dto.getNombre());
        assertEquals("75.0", dto.getUmbral());
        assertNotNull(dto.getReglasIds());
        assertEquals(3, dto.getReglasIds().size());
        assertTrue(dto.getReglasIds().containsAll(List.of(10L, 20L, 30L)));
    }
}
