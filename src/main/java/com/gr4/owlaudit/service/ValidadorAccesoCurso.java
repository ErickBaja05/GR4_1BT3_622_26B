package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.model.Curso;

/**
 * Refactoring: Extract Class (Martin Fowler)
 * Encapsula la política y validación de autorización de acceso a cursos y asignaciones.
 */
public class ValidadorAccesoCurso {

    public static final String MENSAJE_PERMISO_CURSO =
            "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado";

    private final CursoDAO cursoDAO;

    public ValidadorAccesoCurso(CursoDAO cursoDAO) {
        this.cursoDAO = cursoDAO;
    }

    public boolean estaEstudianteMatriculado(Curso curso, Long estudianteId) {
        if (curso == null || estudianteId == null) {
            return false;
        }
        if (curso.estaMatriculado(estudianteId)) {
            return true;
        }
        return cursoDAO != null && cursoDAO.estaEstudianteMatriculado(curso.getId(), estudianteId);
    }

    public boolean estaEstudianteMatriculado(Long cursoId, Long estudianteId) {
        if (cursoId == null || estudianteId == null) {
            return false;
        }
        if (cursoDAO != null && cursoDAO.estaEstudianteMatriculado(cursoId, estudianteId)) {
            return true;
        }
        Curso curso = (cursoDAO != null) ? cursoDAO.buscarPorId(cursoId) : null;
        return curso != null && curso.estaMatriculado(estudianteId);
    }

    public void validarAccesoEstudianteACurso(Curso curso, Long estudianteId) {
        if (!estaEstudianteMatriculado(curso, estudianteId)) {
            throw new ExcepcionNegocio(MENSAJE_PERMISO_CURSO);
        }
    }

    public void validarAccesoEstudianteACurso(Long cursoId, Long estudianteId) {
        if (!estaEstudianteMatriculado(cursoId, estudianteId)) {
            throw new ExcepcionNegocio(MENSAJE_PERMISO_CURSO);
        }
    }
}

