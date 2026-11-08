package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Suite de pruebas unitarias/integración para ReglaDAO adaptada al modelo por curso.
 * Cubre:
 * - Persistencia y actualización de vigencia con asociación obligatoria a Curso.
 * - Consultas de aislamiento de catálogo por curso (Task 2.2).
 */
@Tag("integracion")
class ReglaDAOTest {

    private ReglaDAO reglaDAO;
    private Docente docentePrueba;
    private Curso cursoPrueba;
    private Curso cursoOtro;
    private final List<Regla> reglasCreadas = new ArrayList<>();

    @BeforeEach
    void setUp() {
        reglaDAO = new ReglaDAOImpl();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            docentePrueba = new Docente();
            docentePrueba.setNombre("Docente Regla DAO " + System.nanoTime());
            docentePrueba.setCorreo("docente.dao." + System.nanoTime() + "@epn.edu.ec");
            docentePrueba.setContrasena("12345");
            docentePrueba.setRol(RolEnum.DOCENTE);
            session.persist(docentePrueba);

            cursoPrueba = new Curso(docentePrueba, "CSW-" + System.nanoTime(), "Construcción Software", "2026B", "Desc");
            session.persist(cursoPrueba);

            cursoOtro = new Curso(docentePrueba, "CAL-" + System.nanoTime(), "Calidad Software", "2026B", "Desc");
            session.persist(cursoOtro);

            tx.commit();
        }
    }

    @AfterEach
    void tearDown() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            for (Regla r : reglasCreadas) {
                if (r != null && r.getId() != null) {
                    Regla persisted = session.get(Regla.class, r.getId());
                    if (persisted != null) {
                        session.remove(persisted);
                    }
                }
            }
            if (cursoPrueba != null && cursoPrueba.getId() != null) {
                Curso c = session.get(Curso.class, cursoPrueba.getId());
                if (c != null) session.remove(c);
            }
            if (cursoOtro != null && cursoOtro.getId() != null) {
                Curso c = session.get(Curso.class, cursoOtro.getId());
                if (c != null) session.remove(c);
            }
            if (docentePrueba != null && docentePrueba.getId() != null) {
                Docente d = session.get(Docente.class, docentePrueba.getId());
                if (d != null) session.remove(d);
            }
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error al limpiar regla de prueba: " + e.getMessage());
        }
    }

    private Regla crearYGuardarRegla(String nombre, Curso curso, EstadoVigencia vigencia) {
        return crearYGuardarRegla(nombre, curso, vigencia, null);
    }

    private Regla crearYGuardarRegla(String nombre, Curso curso, EstadoVigencia vigencia, String descripcion) {
        Regla regla = new Regla();
        regla.setNombreRepresentativo(nombre);
        regla.setDescripcionDetallada(descripcion);
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.MEDIA);
        regla.setPonderacion(10);
        regla.setVigencia(vigencia);
        regla.setCurso(curso);
        reglaDAO.guardar(regla);
        reglasCreadas.add(regla);
        return regla;
    }

    @Test
    @DisplayName("Debe actualizar el estado de vigencia de una regla existente a NO_VIGENTE y recuperarla por nombre")
    void actualizarEstadoVigencia_debeCambiarEstadoANoVigente_cuandoReglaExiste() {
        String nombreRegla = "Regla TDD Vigencia " + System.nanoTime();
        Regla regla = crearYGuardarRegla(nombreRegla, cursoPrueba, EstadoVigencia.VIGENTE);

        boolean actualizado = reglaDAO.actualizarEstadoVigencia(nombreRegla, EstadoVigencia.NO_VIGENTE);

        assertTrue(actualizado, "El DAO debe retornar true cuando la regla existe y fue actualizada");
        Regla reglaConsultada = reglaDAO.obtenerPorNombre(nombreRegla);
        assertNotNull(reglaConsultada);
        assertEquals(EstadoVigencia.NO_VIGENTE, reglaConsultada.getVigencia());
        assertFalse(reglaConsultada.esVigente());
    }

    @Test
    @DisplayName("Debe consultar únicamente las reglas con estado VIGENTE")
    void consultarReglasDeEvaluacionVigentes_debeRetornarSoloReglasVigentes() {
        String nombreRegla = "Regla No Vigente " + System.nanoTime();
        crearYGuardarRegla(nombreRegla, cursoPrueba, EstadoVigencia.NO_VIGENTE);

        List<Regla> vigentes = reglaDAO.consultarReglasDeEvaluacionVigentes();

        boolean contieneNoVigente = vigentes.stream()
                .anyMatch(r -> r.getNombreRepresentativo().equals(nombreRegla));
        assertFalse(contieneNoVigente, "La consulta no debe incluir reglas en estado NO_VIGENTE");
    }

    @Test
    @DisplayName("Task 2.2: listarPorCurso debe retornar únicamente las reglas pertenecientes al curso especificado")
    void listarPorCurso_debeAislarReglasPorCurso() {
        Regla r1 = crearYGuardarRegla("Regla Curso 1 - " + System.nanoTime(), cursoPrueba, EstadoVigencia.VIGENTE);
        Regla r2 = crearYGuardarRegla("Regla Curso 2 - " + System.nanoTime(), cursoOtro, EstadoVigencia.VIGENTE);

        List<Regla> reglasCursoPrueba = reglaDAO.listarPorCurso(cursoPrueba.getId());

        assertNotNull(reglasCursoPrueba);
        assertTrue(reglasCursoPrueba.stream().anyMatch(r -> r.getId().equals(r1.getId())));
        assertFalse(reglasCursoPrueba.stream().anyMatch(r -> r.getId().equals(r2.getId())));
    }

    @Test
    @DisplayName("Task 2.2: existeNombreEnCurso debe validar unicidad dentro del curso")
    void existeNombreEnCurso_debeDetectarDuplicadosDentroDelMismoCurso() {
        String nombre = "Regla Unica - " + System.nanoTime();
        crearYGuardarRegla(nombre, cursoPrueba, EstadoVigencia.VIGENTE);

        assertTrue(reglaDAO.existeNombreEnCurso(nombre, cursoPrueba.getId()));
        assertFalse(reglaDAO.existeNombreEnCurso(nombre, cursoOtro.getId()));
        assertFalse(reglaDAO.existeNombreEnCurso("Otro Nombre", cursoPrueba.getId()));
    }

    @Test
    @DisplayName("HU-02: modificar una regla solo afecta a la del curso indicado, aunque otro curso tenga una regla con el mismo nombre")
    void actualizarDescripcionYVigencia_soloModificaLaReglaDelCursoIndicado() {
        String nombreCompartido = "HOLA " + System.nanoTime();
        Regla reglaDelCurso = crearYGuardarRegla(nombreCompartido, cursoPrueba, EstadoVigencia.VIGENTE, "Descripción original");
        Regla reglaDeOtroCurso = crearYGuardarRegla(nombreCompartido, cursoOtro, EstadoVigencia.VIGENTE, "Descripción de otro curso");

        boolean actualizada = reglaDAO.actualizarDescripcionYVigencia(
                reglaDelCurso.getId(), cursoPrueba.getId(), "Descripción nueva", EstadoVigencia.NO_VIGENTE);
        boolean actualizadaConCursoAjeno = reglaDAO.actualizarDescripcionYVigencia(
                reglaDeOtroCurso.getId(), cursoPrueba.getId(), "Intento ajeno", EstadoVigencia.NO_VIGENTE);

        assertTrue(actualizada, "Debe actualizar la regla que pertenece al curso indicado");
        assertFalse(actualizadaConCursoAjeno, "No debe actualizar una regla que pertenece a otro curso");

        Regla consultada = reglaDAO.obtenerPorIdYCurso(reglaDelCurso.getId(), cursoPrueba.getId());
        assertEquals("Descripción nueva", consultada.getDescripcionDetallada());
        assertEquals(EstadoVigencia.NO_VIGENTE, consultada.getVigencia());

        Regla otraIntacta = reglaDAO.obtenerPorIdYCurso(reglaDeOtroCurso.getId(), cursoOtro.getId());
        assertEquals("Descripción de otro curso", otraIntacta.getDescripcionDetallada());
        assertEquals(EstadoVigencia.VIGENTE, otraIntacta.getVigencia());
    }
}
