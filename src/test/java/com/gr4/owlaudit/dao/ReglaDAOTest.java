
package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite de pruebas unitarias para ReglaDAO.
 * Historia de Usuario: HU-01 (Activar / Desactivar vigencia de reglas).
 * Pruebas unitarias convencionales sin mocks y sin pruebas parametrizadas.
 */
class ReglaDAOTest {

    private ReglaDAO reglaDAO;
    private Regla reglaPrueba;

    @BeforeEach
    void setUp() {
        reglaDAO = new ReglaDAOImpl();
    }

    @AfterEach
    void tearDown() {
        if (reglaPrueba != null && reglaPrueba.getId() != null) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();
                Regla r = session.get(Regla.class, reglaPrueba.getId());
                if (r != null) {
                    session.remove(r);
                }
                tx.commit();
            } catch (Exception e) {
                System.err.println("Error al limpiar regla de prueba: " + e.getMessage());
            }
        }
    }

    @Test
    @DisplayName("Debe actualizar el estado de vigencia de una regla existente a NO_VIGENTE y recuperarla por nombre")
    void actualizarEstadoVigencia_debeCambiarEstadoANoVigente_cuandoReglaExiste() {
        // GIVEN: Una regla en estado VIGENTE registrada previamente en el sistema
        String nombreRegla = "Regla TDD Vigencia " + System.nanoTime();
        reglaPrueba = new Regla();
        reglaPrueba.setNombreRepresentativo(nombreRegla);
        reglaPrueba.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaPrueba.setParametroExacto("README.md");
        reglaPrueba.setNivelSeveridad(NivelSeveridadEnum.MEDIA);
        reglaPrueba.setPonderacion(10);
        reglaPrueba.setActiva(true);
        reglaPrueba.setVigencia(EstadoVigencia.VIGENTE);
        reglaDAO.guardar(reglaPrueba);

        // WHEN: Se solicita actualizar el estado de vigencia a NO_VIGENTE
        boolean actualizado = reglaDAO.actualizarEstadoVigencia(nombreRegla, EstadoVigencia.NO_VIGENTE);

        // THEN: La actualización debe ser confirmada y el estado reflejarse en la base de datos
        assertTrue(actualizado, "El DAO debe retornar true cuando la regla existe y fue actualizada");

        Regla reglaConsultada = reglaDAO.obtenerPorNombre(nombreRegla);
        assertNotNull(reglaConsultada, "La regla debe ser encontrada por su nombre representativo");
        assertEquals(EstadoVigencia.NO_VIGENTE, reglaConsultada.getVigencia(), "La regla debe reflejar el nuevo estado NO_VIGENTE");
        assertFalse(reglaConsultada.isActiva(), "El booleano de activación debe mantenerse consistente (false)");
    }

    @Test
    @DisplayName("Debe consultar únicamente las reglas con estado VIGENTE")
    void consultarReglasDeEvaluacionVigentes_debeRetornarSoloReglasVigentes() {
        // GIVEN: Una regla en estado NO_VIGENTE registrada en el sistema
        String nombreRegla = "Regla No Vigente " + System.nanoTime();
        reglaPrueba = new Regla();
        reglaPrueba.setNombreRepresentativo(nombreRegla);
        reglaPrueba.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaPrueba.setParametroExacto("pom.xml");
        reglaPrueba.setNivelSeveridad(NivelSeveridadEnum.BAJA);
        reglaPrueba.setPonderacion(5);
        reglaPrueba.setActiva(false);
        reglaPrueba.setVigencia(EstadoVigencia.NO_VIGENTE);
        reglaDAO.guardar(reglaPrueba);

        // WHEN: Se consultan las reglas vigentes para el motor de auditorías
        java.util.List<Regla> vigentes = reglaDAO.consultarReglasDeEvaluacionVigentes();

        // THEN: La lista no debe incluir la regla en estado NO_VIGENTE
        boolean contieneNoVigente = vigentes.stream()
                .anyMatch(r -> r.getNombreRepresentativo().equals(nombreRegla));
        assertFalse(contieneNoVigente, "La consulta no debe incluir reglas en estado NO_VIGENTE");
    }
}
