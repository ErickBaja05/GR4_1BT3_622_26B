package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Proyecto;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ProyectoDAOImpl implements ProyectoDAO {

    @Override
    public void guardarProyectoAcademico(Proyecto proyecto) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.persist(proyecto);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al guardar el proyecto académico: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
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
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
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
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al listar proyectos académicos por usuario: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
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
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(proyecto);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al actualizar el proyecto: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Proyecto proyecto = session.get(Proyecto.class, id);
            if (proyecto != null) {
                // Limpieza en orden inverso de relaciones de clave foránea para evitar FK violation
                session.createNativeMutationQuery(
                    "DELETE FROM solicitudes_retroalimentacion WHERE hallazgo_id IN (" +
                    "  SELECT h.id FROM hallazgos h " +
                    "  JOIN resultados_regla rr ON h.resultado_regla_id = rr.id " +
                    "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                    "  WHERE a.proyecto_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                session.createNativeMutationQuery(
                    "DELETE FROM hallazgos WHERE resultado_regla_id IN (" +
                    "  SELECT rr.id FROM resultados_regla rr " +
                    "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                    "  WHERE a.proyecto_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                session.createNativeMutationQuery(
                    "DELETE FROM resultados_regla WHERE auditoria_id IN (" +
                    "  SELECT a.id FROM auditorias a WHERE a.proyecto_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                session.createNativeMutationQuery(
                    "DELETE FROM auditorias WHERE proyecto_id = :id"
                ).setParameter("id", id).executeUpdate();

                session.remove(proyecto);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al eliminar el proyecto: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void resecuenciarIdsEstudiante(Long usuarioId) {
        if (usuarioId == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
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
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al resecuenciar IDs de proyectos: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}

