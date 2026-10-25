package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import java.util.List;

public class ReglaDAOImpl implements ReglaDAO {

    @Override
    public boolean existePorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(r) FROM Regla r WHERE LOWER(TRIM(r.nombreRepresentativo)) = LOWER(TRIM(:nombre))",
                    Long.class);
            query.setParameter("nombre", nombre);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }

    @Override
    public void guardar(Regla regla) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(regla);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar la regla de evaluación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Regla> consultarReglasDeEvaluacionVigentes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT r FROM Regla r WHERE r.vigencia = :vigencia",
                Regla.class
            ).setParameter("vigencia", EstadoVigencia.VIGENTE).list();
        }
    }

    @Override
    public Regla obtenerPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Refactorización: Inline Temp sobre la variable temporal query
            return session.createQuery(
                "SELECT r FROM Regla r WHERE r.nombreRepresentativo = :nombre",
                Regla.class
            ).setParameter("nombre", nombre).uniqueResult();
        }
    }

    @Override
    public boolean actualizarEstadoVigencia(String nombre, EstadoVigencia nuevoEstado) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            MutationQuery query = session.createMutationQuery(
                "UPDATE Regla r SET r.vigencia = :vigencia WHERE r.nombreRepresentativo = :nombre"
            );
            query.setParameter("vigencia", nuevoEstado);
            query.setParameter("nombre", nombre);
            int filasActualizadas = query.executeUpdate();
            tx.commit();
            return filasActualizadas > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {
                }
            }
            return false;
        }
    }
}
