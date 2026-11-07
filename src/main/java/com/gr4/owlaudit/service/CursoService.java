package com.gr4.owlaudit.service;

import com.gr4.owlaudit.model.Curso;

import java.util.List;

public interface CursoService {

    List<Curso> listarCursosPorEstudiante(Long idEstudiante);

    List<Curso> listarCursosPorDocente(Long idDocente);

    Curso buscarPorId(Long idCurso);

    boolean estaEstudianteMatriculado(Long idCurso, Long idEstudiante);
}

