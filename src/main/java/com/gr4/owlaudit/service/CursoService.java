package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;

import java.util.List;

public interface CursoService {

    List<Curso> listarCursosPorEstudiante(Long idEstudiante);

    List<Curso> listarCursosPorDocente(Long idDocente);

    Curso buscarPorId(Long idCurso);

    boolean estaEstudianteMatriculado(Long idCurso, Long idEstudiante);

    Curso crearCurso(NuevoCursoDTO dto, String correoDocente) throws ExcepcionNegocio;

    public List<Curso> listarCursosPorCorreoDocente(String correoDocente);

    Curso matricularEstudiantePorCodigo(Long idEstudiante, String codigoCurso) throws ExcepcionNegocio;
}
