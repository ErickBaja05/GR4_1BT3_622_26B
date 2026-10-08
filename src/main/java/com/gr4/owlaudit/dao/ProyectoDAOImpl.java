package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Proyecto;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

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

    @Override
    public List<Proyecto> listarTodos() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT p FROM Proyecto p ORDER BY p.id ASC", Proyecto.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Error al listar proyectos académicos: " + e.getMessage(), e);
        }
    }

    @Override
    public Proyecto buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Proyecto.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar proyecto académico por ID: " + e.getMessage(), e);
        }
    }
}

