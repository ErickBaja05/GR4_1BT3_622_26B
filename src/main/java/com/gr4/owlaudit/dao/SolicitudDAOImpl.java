package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.dao.SolicitudDAOImpl
 * Implementación DAO utilizando Hibernate ORM para la gestión de persistencia de solicitudes.
 */
public class SolicitudDAOImpl implements SolicitudDAO {

    @Override
    public SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId) {
        if (hallazgoId == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SolicitudRetroalimentacion> query = session.createQuery(
                "SELECT s FROM SolicitudRetroalimentacion s WHERE s.hallazgo.id = :hallazgoId",
                SolicitudRetroalimentacion.class
            );
            query.setParameter("hallazgoId", hallazgoId);
            return query.uniqueResultOptional().orElse(null);
        } catch (Exception e) {
            System.err.println("[SolicitudDAOImpl] Error al buscar solicitud por hallazgoId: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(solicitud);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al crear la solicitud de retroalimentación: " + e.getMessage(), e);
        }
    }

    @Override
    public SolicitudRetroalimentacion buscarPorId(Long solicitudId) {
        if (solicitudId == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(SolicitudRetroalimentacion.class, solicitudId);
        } catch (Exception e) {
            System.err.println("[SolicitudDAOImpl] Error al buscar solicitud por id: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void actualizarSolicitud(SolicitudRetroalimentacion solicitud) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(solicitud);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar la solicitud de retroalimentación: " + e.getMessage(), e);
        }
    }
}
