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
    public List<Proyecto> listarPorUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return java.util.Collections.emptyList();
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            List<Proyecto> lista = session.createQuery(
                "SELECT p FROM Proyecto p WHERE p.usuario.id = :usuarioId ORDER BY p.id ASC", 
                Proyecto.class
            )
            .setParameter("usuarioId", usuarioId)
            .list();
            long sec = 1L;
            boolean necesitaUpdate = false;
            for (Proyecto p : lista) {
                if (p.getIdEstudiante() == null) {
                    p.setIdEstudiante(sec);
                    session.merge(p);
                    necesitaUpdate = true;
                }
                sec++;
            }
            if (necesitaUpdate) {
                tx.commit();
            } else {
                tx.rollback();
            }
            return lista;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al listar proyectos académicos por usuario: " + e.getMessage(), e);
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

    @Override
    public void actualizar(Proyecto proyecto) {
        if (proyecto == null) {
            return;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(proyecto);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al actualizar el proyecto: " + e.getMessage(), e);
        }
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Proyecto proyecto = session.get(Proyecto.class, id);
            if (proyecto != null) {
                session.createMutationQuery("DELETE FROM Auditoria a WHERE a.proyecto.id = :id")
                        .setParameter("id", id)
                        .executeUpdate();
                session.remove(proyecto);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al eliminar el proyecto: " + e.getMessage(), e);
        }
    }

    @Override
    public void resecuenciarIdsEstudiante(Long usuarioId) {
        if (usuarioId == null) {
            return;
        }
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            List<Proyecto> restantes = session.createQuery(
                "SELECT p FROM Proyecto p WHERE p.usuario.id = :usuarioId ORDER BY p.id ASC",
                Proyecto.class
            ).setParameter("usuarioId", usuarioId).list();

            long sec = 1L;
            for (Proyecto p : restantes) {
                p.setIdEstudiante(sec++);
                session.merge(p);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al resecuenciar IDs de proyectos: " + e.getMessage(), e);
        }
    }
}

