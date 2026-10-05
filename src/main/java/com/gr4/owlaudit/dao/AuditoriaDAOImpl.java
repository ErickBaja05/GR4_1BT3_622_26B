package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.ResultadoRegla;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrarAuditoriaEnElHistorial(Auditoria auditoria) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // Por cascade se guardan también sus ResultadoRegla y Hallazgo
            session.persist(auditoria);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al registrar la auditoría en el historial: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Auditoria> consultarHistorialDeAuditorias(Long proyectoId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.proyecto.id = :proyectoId " +
                "ORDER BY a.fechaHora ASC",
                Auditoria.class
            )
            .setParameter("proyectoId", proyectoId)
            .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar el historial de auditorías: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ResultadoRegla> consultarResultadosDeReglas(Long baseId, Long comparadaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT r FROM ResultadoRegla r " +
                "JOIN FETCH r.regla " +
                "JOIN FETCH r.auditoria " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE r.auditoria.id IN (:baseId, :comparadaId)",
                ResultadoRegla.class
            )
            .setParameter("baseId", baseId)
            .setParameter("comparadaId", comparadaId)
            .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar los resultados de reglas: " + e.getMessage(), e);
        }
    }
}
