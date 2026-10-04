package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Auditoria;
import org.hibernate.Session;
import org.hibernate.Transaction;

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
}
