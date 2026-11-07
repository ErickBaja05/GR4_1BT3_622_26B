package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Curso;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Collections;
import java.util.List;

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
                    "WHERE c.id = :id", Curso.class)
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
                    "WHERE c.codigo = :codigo", Curso.class)
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
                    "ORDER BY c.nombre ASC", Curso.class)
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
                    "WHERE c.docente.id = :idDocente " +
                    "ORDER BY c.nombre ASC", Curso.class)
                    .setParameter("idDocente", idDocente)
                    .list();
        } catch (Exception e) {
            return Collections.emptyList();
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
                    "WHERE le.curso.id = :idCurso AND le.estudiante.id = :idEstudiante AND le.estado = 'ACTIVO'", Long.class)
                    .setParameter("idCurso", idCurso)
                    .setParameter("idEstudiante", idEstudiante)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }
}

