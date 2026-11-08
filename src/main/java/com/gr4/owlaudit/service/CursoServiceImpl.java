package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CursoServiceImpl implements CursoService {

    private final CursoDAO cursoDAO;
    private static final Pattern PATRON_CODIGO_CURSO = Pattern.compile("^[A-Za-z0-9-]+$");

    public CursoServiceImpl() {
        this(new CursoDAOImpl());
    }

    public CursoServiceImpl(CursoDAO cursoDAO) {
        this.cursoDAO = cursoDAO;
    }

    @Override
    public List<Curso> listarCursosPorEstudiante(Long idEstudiante) {
        if (idEstudiante == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorEstudiante(idEstudiante);
    }

    @Override
    public List<Curso> listarCursosPorDocente(Long idDocente) {
        if (idDocente == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorDocente(idDocente);
    }

    @Override
    public Curso buscarPorId(Long idCurso) {
        if (idCurso == null) {
            return null;
        }
        return cursoDAO.buscarPorId(idCurso);
    }

    @Override
    public boolean estaEstudianteMatriculado(Long idCurso, Long idEstudiante) {
        if (idCurso == null || idEstudiante == null) {
            return false;
        }
        return cursoDAO.estaEstudianteMatriculado(idCurso, idEstudiante);
    }

    @Override
    public Curso crearCurso(NuevoCursoDTO dto, Long idDocente) throws ExcepcionNegocio {
        validarFormatoCodigo(dto.getCodigo());
        return null;
    }

    private void validarFormatoCodigo(String codigo) throws ExcepcionNegocio {
        if (codigo != null) {
            String codigoNormalizado = codigo.trim();
            if (!PATRON_CODIGO_CURSO.matcher(codigoNormalizado).matches()) {
                throw new ExcepcionNegocio("El código solo admite caracteres alfanuméricos y guiones");
            }
        }
    }
}
