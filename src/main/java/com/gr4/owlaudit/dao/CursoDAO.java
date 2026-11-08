package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;

import java.util.List;

public interface CursoDAO {

    Curso buscarPorId(Long id);

    Curso buscarPorCodigo(String codigo);

    List<Curso> listarCursosPorEstudiante(Long idEstudiante);

    List<Curso> listarCursosPorDocente(Long idDocente);

    boolean estaEstudianteMatriculado(Long idCurso, Long idEstudiante);

    void guardar(NuevoCursoDTO curso);
}
