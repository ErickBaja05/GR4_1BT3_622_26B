package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import org.hibernate.Session;
import org.hibernate.Transaction;
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
            Query<Regla> query = session.createQuery(
                    "SELECT r FROM Regla r WHERE r.activa = true",
                    Regla.class);
            return query.list();
        }
    }

    @Override
    public Regla obtenerPorNombre(String nombre) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Regla> query = session.createQuery(
                "SELECT r FROM Regla r WHERE r.nombreRepresentativo = :nombre",
                Regla.class
            );
            query.setParameter("nombre", nombre);
            return query.uniqueResult();
        }
    }

    @Override
    public boolean actualizarEstadoVigencia(String nombre, EstadoVigencia nuevoEstado) {
        boolean esActiva = false;
        if (nuevoEstado == EstadoVigencia.VIGENTE) {
            esActiva = true;
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query<?> query = session.createQuery(
                "UPDATE Regla r SET r.vigencia = :vigencia, r.activa = :activa WHERE r.nombreRepresentativo = :nombre"
            );
            query.setParameter("vigencia", nuevoEstado);
            query.setParameter("activa", esActiva);
            query.setParameter("nombre", nombre);
            int filasActualizadas = query.executeUpdate();
            tx.commit();
            return filasActualizadas > 0;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            return false;
        }
    }

}
