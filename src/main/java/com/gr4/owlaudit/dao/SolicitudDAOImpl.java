package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class SolicitudDAOImpl implements SolicitudDAO {

    @Override
    public SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT s FROM SolicitudRetroalimentacion s " +
                "LEFT JOIN FETCH s.hallazgo h " +
                "LEFT JOIN FETCH h.resultadoRegla r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "WHERE s.hallazgo.id = :hallazgoId",
                SolicitudRetroalimentacion.class
            )
            .setParameter("hallazgoId", hallazgoId)
            .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar solicitud por hallazgo ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            if (solicitud.getHallazgo() != null && solicitud.getHallazgo().getId() != null) {
                com.gr4.owlaudit.model.Hallazgo h = session.get(com.gr4.owlaudit.model.Hallazgo.class, solicitud.getHallazgo().getId());
                if (h != null) {
                    solicitud.setHallazgo(h);
                } else {
                    throw new com.gr4.owlaudit.common.exception.ExcepcionNegocio("El hallazgo especificado no existe en el sistema.");
                }
            }
            session.persist(solicitud);
            tx.commit();
        } catch (com.gr4.owlaudit.common.exception.ExcepcionNegocio e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al crear solicitud de retroalimentación: " + e.getMessage(), e);
        }
    }

    @Override
    public SolicitudRetroalimentacion buscarPorId(Long solicitudId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT s FROM SolicitudRetroalimentacion s " +
                "LEFT JOIN FETCH s.hallazgo h " +
                "LEFT JOIN FETCH h.resultadoRegla r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "WHERE s.id = :solicitudId",
                SolicitudRetroalimentacion.class
            )
            .setParameter("solicitudId", solicitudId)
            .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar solicitud por ID: " + e.getMessage(), e);
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

    @Override
    public java.util.List<SolicitudRetroalimentacion> consultarSolicitudesPendientes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT s FROM SolicitudRetroalimentacion s " +
                "LEFT JOIN FETCH s.hallazgo h " +
                "LEFT JOIN FETCH h.resultadoRegla r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "WHERE s.estado = :estado " +
                "ORDER BY s.id DESC",
                SolicitudRetroalimentacion.class
            )
            .setParameter("estado", com.gr4.owlaudit.model.EstadoRetroEnum.PENDIENTE)
            .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar solicitudes pendientes: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<SolicitudRetroalimentacion> buscarPendientes() {
        return consultarSolicitudesPendientes();
    }

    @Override
    public com.gr4.owlaudit.model.Hallazgo buscarHallazgoPorId(Long hallazgoId) {
        if (hallazgoId == null) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT h FROM Hallazgo h " +
                "LEFT JOIN FETCH h.resultadoRegla r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "WHERE h.id = :id",
                com.gr4.owlaudit.model.Hallazgo.class
            )
            .setParameter("id", hallazgoId)
            .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar hallazgo por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public java.util.List<SolicitudRetroalimentacion> consultarTodasLasSolicitudes() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT s FROM SolicitudRetroalimentacion s " +
                "LEFT JOIN FETCH s.hallazgo h " +
                "LEFT JOIN FETCH h.resultadoRegla r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "ORDER BY s.id DESC",
                SolicitudRetroalimentacion.class
            ).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar todas las solicitudes: " + e.getMessage(), e);
        }
    }
}

