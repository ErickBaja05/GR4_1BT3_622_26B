package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.ListadoEstudiantes;

import java.util.Collections;
import java.util.List;
import com.gr4.owlaudit.model.ListadoEstudiantes;

public class CursoDAOImpl implements CursoDAO {

    @Override
    public Curso buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Curso c " +
                            "LEFT JOIN FETCH c.docente " +
                            "LEFT JOIN FETCH c.listadoEstudiantes le " +
                            "LEFT JOIN FETCH le.estudiante " +
                            "WHERE c.id = :id",
                    Curso.class)
                    .setParameter("id", id)
                    .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Curso buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Curso c " +
                            "LEFT JOIN FETCH c.docente " +
                            "LEFT JOIN FETCH c.listadoEstudiantes le " +
                            "LEFT JOIN FETCH le.estudiante " +
                            "WHERE c.codigo = :codigo",
                    Curso.class)
                    .setParameter("codigo", codigo.trim())
                    .uniqueResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Curso> listarCursosPorEstudiante(Long idEstudiante) {
        if (idEstudiante == null) {
            return Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Curso c " +
                            "JOIN c.listadoEstudiantes le " +
                            "LEFT JOIN FETCH c.docente " +
                            "WHERE le.estudiante.id = :idEstudiante AND le.estado = 'ACTIVO' " +
                            "ORDER BY c.nombre ASC",
                    Curso.class)
                    .setParameter("idEstudiante", idEstudiante)
                    .list();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public List<Curso> listarCursosPorDocente(Long idDocente) {
        if (idDocente == null) {
            return Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT c FROM Curso c " +
                            "LEFT JOIN FETCH c.docente " +
                            "LEFT JOIN FETCH c.listadoEstudiantes " +
                            "WHERE c.docente.id = :idDocente " +
                            "ORDER BY c.nombre ASC",
                    Curso.class)
                    .setParameter("idDocente", idDocente)
                    .list();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void guardar(Curso curso) {
        if (curso == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (curso.getDocente() != null && curso.getDocente().getId() != null) {
                Docente docenteGestionado = session.find(Docente.class, curso.getDocente().getId());
                if (docenteGestionado != null) {
                    curso.setDocente(docenteGestionado);
                }
            }
            if (curso.getPeriodoAcademico() == null || curso.getPeriodoAcademico().isBlank()) {
                curso.setPeriodoAcademico("2026B");
            }
            session.persist(curso);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Error al guardar el curso: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public boolean estaEstudianteMatriculado(Long idCurso, Long idEstudiante) {
        if (idCurso == null || idEstudiante == null) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT COUNT(le) FROM ListadoEstudiantes le " +
                            "WHERE le.curso.id = :idCurso AND le.estudiante.id = :idEstudiante AND le.estado = 'ACTIVO'",
                    Long.class)
                    .setParameter("idCurso", idCurso)
                    .setParameter("idEstudiante", idEstudiante)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void registrarMatricula(ListadoEstudiantes matricula) {
        if (matricula == null || matricula.getId() == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Curso cursoGestionado = session.find(Curso.class, matricula.getId().getIdCurso());
            Estudiante estudianteGestionado = session.find(Estudiante.class, matricula.getId().getIdEstudiante());

            if (cursoGestionado != null && estudianteGestionado != null) {
                matricula.setCurso(cursoGestionado);
                matricula.setEstudiante(estudianteGestionado);
                session.persist(matricula);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Error al registrar la matrícula: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

}
