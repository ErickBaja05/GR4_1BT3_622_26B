package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.RolEnum;

/**
 * Refactoring: Extract Class (Martin Fowler)
 * Encapsula la política y validación de autorización de acceso a cursos y asignaciones.
 */
public class ValidadorAccesoCurso {

    public static final String MENSAJE_PERMISO_CURSO =
            "No tienes permisos para acceder a las asignaciones de un curso en el que no estás matriculado";

    private final CursoDAO cursoDAO;

    public ValidadorAccesoCurso() {
        this(null);
    }

    public ValidadorAccesoCurso(CursoDAO cursoDAO) {
        this.cursoDAO = cursoDAO;
    }

    public boolean esDocente(UsuarioDTO usuario) {
        return usuario != null && usuario.getRol() == RolEnum.DOCENTE;
    }

    public boolean esEstudiante(UsuarioDTO usuario) {
        return usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE;
    }

    public boolean cursoExiste(Long cursoId) {
        if (cursoId == null || cursoDAO == null) {
            return false;
        }
        return cursoDAO.buscarPorId(cursoId) != null;
    }

    public boolean esDocenteTitular(Curso curso, Long docenteId) {
        if (curso == null || docenteId == null || curso.getDocente() == null) {
            return false;
        }
        return docenteId.equals(curso.getDocente().getId());
    }

    public boolean esDocenteTitular(Long cursoId, Long docenteId) {
        if (cursoId == null || docenteId == null || cursoDAO == null) {
            return false;
        }
        Curso curso = cursoDAO.buscarPorId(cursoId);
        return esDocenteTitular(curso, docenteId);
    }

    public static final String MENSAJE_PERMISO_DOCENTE_TITULAR =
            "No tienes permisos para acceder a las entregas de un curso del que no eres titular";

    public void validarTitularidadDocenteCurso(Curso curso, Long docenteId) {
        if (!esDocenteTitular(curso, docenteId)) {
            throw new ExcepcionNegocio(MENSAJE_PERMISO_DOCENTE_TITULAR);
        }
    }

    /**
     * Valida si un usuario tiene autorización para acceder al catálogo de reglas de un curso.
     * Solo los docentes titulares del curso pueden acceder; estudiantes y docentes ajenos están bloqueados.
     */
    public boolean puedeAccederACatalogoReglas(UsuarioDTO usuario, Long cursoId) {
        if (!esDocente(usuario) || cursoId == null) {
            return false;
        }
        return esDocenteTitular(cursoId, usuario.getId());
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

