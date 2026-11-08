package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.AsignacionRegla;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

public class AsignacionReglaDAOImpl implements AsignacionReglaDAO {

    @Override
    public void guardar(List<AsignacionRegla> asignacionesReglas) {
        if (asignacionesReglas == null || asignacionesReglas.isEmpty()) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            for (AsignacionRegla ar : asignacionesReglas) {
                if (ar != null && ar.getAsignacionId() != null && ar.getReglaId() != null) {
                    String sev = (ar.getNivelSeveridad() != null) ? ar.getNivelSeveridad().name() : "MEDIA";
                    session.createNativeMutationQuery(
                        "INSERT INTO asignacion_regla (asignacion_id, regla_id, nivel_severidad, ponderacion) " +
                        "VALUES (:asigId, :reglaId, :severidad, :ponderacion) " +
                        "ON DUPLICATE KEY UPDATE nivel_severidad = :severidad, ponderacion = :ponderacion"
                    )
                    .setParameter("asigId", ar.getAsignacionId())
                    .setParameter("reglaId", ar.getReglaId())
                    .setParameter("severidad", sev)
                    .setParameter("ponderacion", ar.getPonderacion())
                    .executeUpdate();
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al guardar asignaciones de reglas: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void guardar(AsignacionRegla asignacionRegla) {
        if (asignacionRegla == null) {
            return;
        }
        guardar(List.of(asignacionRegla));
    }

    @Override
    public List<AsignacionRegla> listarPorAsignacion(Long asignacionId) {
        if (asignacionId == null) {
            return Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT ar FROM AsignacionRegla ar JOIN FETCH ar.regla WHERE ar.asignacionId = :asigId",
                AsignacionRegla.class
            ).setParameter("asigId", asignacionId).list();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar asignaciones de reglas: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminarPorAsignacion(Long asignacionId) {
        if (asignacionId == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.createMutationQuery(
                "DELETE FROM AsignacionRegla ar WHERE ar.asignacionId = :asigId"
            ).setParameter("asigId", asignacionId).executeUpdate();
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al eliminar asignaciones de reglas: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
