package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias e integración para consultarReglasVigentes y NuevaAsignacionDTO
 * adaptadas al nuevo modelo por curso (Task 2.3).
 */
@Tag("integracion")
class ReglaConsultarVigentesTest {

    private ReglaDAO reglaDAO;
    private List<Regla> reglasCreadas;
    private Curso cursoPrueba;
    private Docente docentePrueba;

    @BeforeEach
    void setUp() {
        reglaDAO = new ReglaDAOImpl();
        reglasCreadas = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            docentePrueba = new Docente();
            docentePrueba.setNombre("Docente Asignacion Test " + System.nanoTime());
            docentePrueba.setCorreo("docente.asig." + System.nanoTime() + "@epn.edu.ec");
            docentePrueba.setContrasena("12345");
            docentePrueba.setRol(RolEnum.DOCENTE);
            session.persist(docentePrueba);

            cursoPrueba = new Curso(docentePrueba, "ASIG-" + System.nanoTime(), "Curso Asignación", "2026B", "Desc");
            session.persist(cursoPrueba);
            tx.commit();
        }
    }

    @AfterEach
    void tearDown() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (reglasCreadas != null) {
                for (Regla r : reglasCreadas) {
                    if (r != null && r.getId() != null) {
                        Regla persisted = session.get(Regla.class, r.getId());
                        if (persisted != null) {
                            session.remove(persisted);
                        }
                    }
                }
            }
            if (cursoPrueba != null && cursoPrueba.getId() != null) {
                Curso c = session.get(Curso.class, cursoPrueba.getId());
                if (c != null) session.remove(c);
            }
            if (docentePrueba != null && docentePrueba.getId() != null) {
                Docente d = session.get(Docente.class, docentePrueba.getId());
                if (d != null) session.remove(d);
            }
            tx.commit();
        } catch (Exception ignored) {
        }
    }

    @Test
    @DisplayName("consultarReglasVigentes debe retornar únicamente las reglas con estado VIGENTE")
    void consultarReglasVigentes_debeRetornarSoloReglasConEstadoVigente() {
        Regla reglaVigente = crearReglaPrueba("Regla Vigente Asignacion", TipoMotorEnum.PRESENCIA_ARCHIVO, "README.md",
                NivelSeveridadEnum.MEDIA, 20, EstadoVigencia.VIGENTE);
        Regla reglaNoVigente = crearReglaPrueba("Regla No Vigente Asignacion", TipoMotorEnum.RESTRICCION_ARCHIVOS, ".env",
                NivelSeveridadEnum.ALTA, 30, EstadoVigencia.NO_VIGENTE);

        List<Regla> vigentes = reglaDAO.consultarReglasVigentes();

        assertNotNull(vigentes, "La lista de reglas vigentes no debe ser nula");
        assertTrue(contieneReglaPorNombre(vigentes, reglaVigente.getNombreRepresentativo()),
                "Debe contener la regla registrada como VIGENTE");
        assertFalse(contieneReglaPorNombre(vigentes, reglaNoVigente.getNombreRepresentativo()),
                "No debe contener reglas en estado NO_VIGENTE");

        for (Regla r : vigentes) {
            assertEquals(EstadoVigencia.VIGENTE, r.getVigencia(), "Cada regla retornada debe tener estado VIGENTE");
            assertTrue(r.esVigente(), "El método de dominio esVigente() debe ser true");
        }
    }

    private Regla crearReglaPrueba(String prefijo, TipoMotorEnum motor, String parametro,
                                   NivelSeveridadEnum severidad, int ponderacion, EstadoVigencia vigencia) {
        Regla regla = new Regla();
        regla.setNombreRepresentativo(prefijo + " " + System.nanoTime());
        regla.setTipoMotor(motor);
        regla.setParametroExacto(parametro);
        regla.setNivelSeveridad(severidad);
        regla.setPonderacion(ponderacion);
        regla.setVigencia(vigencia);
        regla.setCurso(cursoPrueba);
        reglaDAO.guardar(regla);
        reglasCreadas.add(regla);
        return regla;
    }

    private boolean contieneReglaPorNombre(List<Regla> reglas, String nombre) {
        return reglas.stream().anyMatch(r -> nombre.equals(r.getNombreRepresentativo()));
    }

    @Test
    @DisplayName("NuevaAsignacionDTO debe encapsular nombre, umbral y lista de identificadores de reglas")
    void nuevaAsignacionDTO_debeEncapsularDatosCorrectamente() {
        String nombre = "Laboratorio 1 - Buenas Prácticas";
        Double umbralAprobacion = 75.0;
        List<Long> reglasIds = List.of(10L, 20L, 30L);

        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(nombre, umbralAprobacion, reglasIds);

        assertEquals("Laboratorio 1 - Buenas Prácticas", dto.getNombre());
        assertEquals(75.0, dto.getUmbralAprobacion(), 0.001);
        assertNotNull(dto.getReglasIds());
        assertEquals(3, dto.getReglasIds().size());
        assertTrue(dto.getReglasIds().containsAll(List.of(10L, 20L, 30L)));
        assertTrue(dto.tieneReglas());

        assertThrows(UnsupportedOperationException.class, () -> dto.getReglasIds().add(40L));

        dto.agregarReglaId(40L);
        assertEquals(4, dto.getReglasIds().size());
    }
}
