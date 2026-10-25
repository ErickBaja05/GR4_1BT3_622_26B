package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Asignacion;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AsignacionesDAOImpl implements AsignacionesDAO {

    @Override
    public Asignacion crearAsignacion(Asignacion asignacion) {
        // Refactorización 1: Introduce Explaining Variable para validación defensiva temprana
        boolean tieneDatosValidos = asignacion != null && asignacion.getNombre() != null && !asignacion.getNombre().trim().isEmpty();
        if (!tieneDatosValidos) {
            throw new IllegalArgumentException("La asignación debe contener un nombre válido para ser persistida.");
        }

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(asignacion);
            tx.commit();
            return asignacion;
        } catch (Exception e) {
            // Refactorización 2: Extract Method para la reversión segura de la transacción
            revertirTransaccion(tx);
            throw new RuntimeException("Error al persistir la asignación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Asignacion> listarAsignaciones() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT DISTINCT a FROM Asignacion a LEFT JOIN FETCH a.reglas", Asignacion.class).list();
        }
    }

    private void revertirTransaccion(Transaction tx) {
        if (tx != null && tx.isActive()) {
            tx.rollback();
        }
    }
}
