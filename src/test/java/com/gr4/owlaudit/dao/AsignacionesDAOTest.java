package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Asignacion;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Suite de pruebas unitarias para AsignacionesDAO.
 * Historia de Usuario: HU-02 (Crear asignaciones para auditorías específicas).
 * Pruebas unitarias convencionales sin mocks y sin pruebas parametrizadas.
 */
class AsignacionesDAOTest {

    private AsignacionesDAO asignacionesDAO;
    private ReglaDAO reglaDAO;
    private Asignacion asignacionPrueba;
    private Regla reglaPrueba;

    @BeforeEach
    void setUp() {
        asignacionesDAO = new AsignacionesDAOImpl();
        reglaDAO = new ReglaDAOImpl();
    }

    @AfterEach
    void tearDown() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (asignacionPrueba != null && asignacionPrueba.getId() != null) {
                Asignacion a = session.get(Asignacion.class, asignacionPrueba.getId());
                if (a != null) {
                    session.remove(a);
                }
            }
            if (reglaPrueba != null && reglaPrueba.getId() != null) {
                Regla r = session.get(Regla.class, reglaPrueba.getId());
                if (r != null) {
                    session.remove(r);
                }
            }
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error al limpiar datos de prueba en AsignacionesDAOTest: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Debe persistir una nueva asignación con sus reglas vinculadas y retornar ID generado")
    void crearAsignacion_debePersistirAsignacionYVincularReglas() {
        // GIVEN: Una regla vigente guardada previamente en la base de datos
        reglaPrueba = new Regla();
        reglaPrueba.setNombreRepresentativo("Regla Asignacion TDD " + System.nanoTime());
        reglaPrueba.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaPrueba.setParametroExacto("pom.xml");
        reglaPrueba.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        reglaPrueba.setPonderacion(25);
        reglaPrueba.setVigencia(EstadoVigencia.VIGENTE);
        reglaDAO.guardar(reglaPrueba);

        String nombreAsignacion = "Asignación TDD HU02 " + System.nanoTime();
        List<Regla> reglas = new ArrayList<>();
        reglas.add(reglaPrueba);

        asignacionPrueba = new Asignacion(nombreAsignacion, 75.0, reglas);

        // WHEN: Se invoca al DAO para crear y persistir la asignación
        Asignacion resultado = asignacionesDAO.crearAsignacion(asignacionPrueba);

        // THEN: La asignación debe persistirse, obtener ID autogenerado y conservar sus relaciones
        assertNotNull(resultado, "La asignación persistida no debe ser nula");
        assertNotNull(resultado.getId(), "La asignación debe recibir un ID autogenerado");
        assertEquals(nombreAsignacion, resultado.getNombre(), "El nombre de la asignación debe coincidir");
        assertEquals(75.0, resultado.getUmbralAprobacion(), "El umbral de aprobación debe coincidir");
        assertNotNull(resultado.getReglas(), "La lista de reglas vinculadas no debe ser nula");
        assertEquals(1, resultado.getReglas().size(), "Debe tener 1 regla vinculada en la tabla intermedia");
    }
}
