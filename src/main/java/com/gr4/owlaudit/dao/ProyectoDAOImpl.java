package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Proyecto;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class ProyectoDAOImpl implements ProyectoDAO {

    @Override
    public void guardarProyectoAcademico(Proyecto proyecto) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(proyecto);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar el proyecto académico: " + e.getMessage(), e);
        }
    }
}
