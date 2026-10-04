package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de integración: requiere MariaDB encendida con la base owl_auditv1.
 * Guarda una auditoría con 2 resultados (uno con hallazgo), verifica las tres
 * tablas y al final borra los datos que creó.
 */
@Tag("integracion")
class AuditoriaDAOImplTest {

    @Test
    void registraAuditoriaConResultadosYHallazgoEnCascada() {
        // Regla de apoyo (la FK regla_id exige que exista en la tabla reglas)
        Regla regla = new Regla();
        regla.setNombreRepresentativo("Regla prueba CU03 " + System.nanoTime());
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto("README.md");
        regla.setNivelSeveridad(NivelSeveridadEnum.BAJA);
        regla.setPonderacion(5);
        regla.setActiva(false); // inactiva para no afectar auditorías reales
        new ReglaDAOImpl().guardar(regla);

        Auditoria auditoria = new Auditoria();
        auditoria.asignarPonderacionCompleta(regla);
        auditoria.registrarHallazgoDeIncumplimiento(regla);
        auditoria.setPuntajeObtenido(5);
        auditoria.setPuntajeMaximo(10);
        auditoria.setPorcentaje(50.0);

        try {
            new AuditoriaDAOImpl().registrarAuditoriaEnElHistorial(auditoria);
            assertNotNull(auditoria.getId());

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Long auditorias = session.createQuery(
                    "SELECT COUNT(a) FROM Auditoria a WHERE a.id = :id", Long.class)
                    .setParameter("id", auditoria.getId()).uniqueResult();
                Long resultados = session.createQuery(
                    "SELECT COUNT(r) FROM ResultadoRegla r WHERE r.auditoria.id = :id", Long.class)
                    .setParameter("id", auditoria.getId()).uniqueResult();
                Long hallazgos = session.createQuery(
                    "SELECT COUNT(h) FROM Hallazgo h WHERE h.resultadoRegla.auditoria.id = :id", Long.class)
                    .setParameter("id", auditoria.getId()).uniqueResult();

                System.out.println("[BD] auditoria id=" + auditoria.getId() + " -> auditorias=" + auditorias
                    + ", resultados_regla=" + resultados + ", hallazgos=" + hallazgos);
                assertEquals(1L, auditorias);
                assertEquals(2L, resultados);
                assertEquals(1L, hallazgos);
            }
        } finally {
            limpiar(auditoria, regla);
        }
    }

    private void limpiar(Auditoria auditoria, Regla regla) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (auditoria.getId() != null) {
                Auditoria guardada = session.get(Auditoria.class, auditoria.getId());
                if (guardada != null) {
                    session.remove(guardada); // cascade borra resultados y hallazgos
                }
            }
            Regla reglaGuardada = session.get(Regla.class, regla.getId());
            if (reglaGuardada != null) {
                session.remove(reglaGuardada);
            }
            tx.commit();
        }
    }
}
