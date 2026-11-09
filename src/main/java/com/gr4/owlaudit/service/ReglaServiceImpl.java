package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.ModificacionReglaDTO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import java.util.Collections;
import java.util.List;

public class ReglaServiceImpl implements ReglaService {

    public static final String MENSAJE_DUPLICADO_CURSO = "Ya existe una regla con ese nombre representativo en este curso";
    public static final String MENSAJE_REGLA_AJENA_AL_CURSO = "La regla no pertenece al curso especificado o no existe.";
    public static final String MENSAJE_DOCENTE_NO_TITULAR = "No tiene permisos para gestionar un curso del que no es docente titular.";
    public static final String MENSAJE_ACTUALIZACION_FALLIDA = "No se pudo actualizar la regla. Intente nuevamente.";

    private final ReglaDAO reglaDAO;
    private final CursoDAO cursoDAO;
    private final ValidadorRegla validadorRegla;

    public ReglaServiceImpl() {
        this(new ReglaDAOImpl(), new CursoDAOImpl(), new ValidadorRegla());
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO) {
        this(reglaDAO, new CursoDAOImpl(), new ValidadorRegla());
    }

    public ReglaServiceImpl(ValidadorRegla validadorRegla) {
        this(new ReglaDAOImpl(), new CursoDAOImpl(), validadorRegla);
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO, ValidadorRegla validadorRegla) {
        this(reglaDAO, new CursoDAOImpl(), validadorRegla);
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO, CursoDAO cursoDAO, ValidadorRegla validadorRegla) {
        this.reglaDAO = reglaDAO;
        this.cursoDAO = cursoDAO;
        this.validadorRegla = validadorRegla;
    }

    @Override
    public void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos de la regla no pueden ser nulos.");
        }

        // 1. Validar sintaxis y formato
        validadorRegla.validarSintaxisYFormato(dto);

        // 2. Verificar existencia del curso
        Curso curso = cursoDAO.buscarPorId(dto.getIdCurso());
        if (curso == null) {
            throw new ExcepcionNegocio("El curso especificado no existe.");
        }

        // 3. Verificar unicidad por nombre dentro del curso
        validarUnicidadEnCurso(dto.getNombreRepresentativo(), dto.getIdCurso());

        // 4. Instanciar entidad, asociar al curso y persistir
        Regla regla = new Regla(dto);
        regla.setCurso(curso);
        regla.setVigencia(EstadoVigencia.VIGENTE);
        reglaDAO.guardar(regla);
    }

    private void validarUnicidadEnCurso(String nombre, Long idCurso) {
        if (reglaDAO.existeNombreEnCurso(nombre, idCurso)) {
            throw new ExcepcionNegocio(MENSAJE_DUPLICADO_CURSO);
        }
    }

    @Override
    public void modificarDescripcionYVigencia(ModificacionReglaDTO modificacion) {
        validadorRegla.validarDescripcionAlModificar(modificacion.descripcion());
        verificarReglaDelCurso(modificacion);
        verificarDocenteTitular(modificacion);

        boolean actualizada = reglaDAO.actualizarDescripcionYVigencia(
                modificacion.idRegla(),
                modificacion.idCurso(),
                modificacion.descripcion().trim(),
                modificacion.vigencia());
        if (!actualizada) {
            throw new ExcepcionNegocio(MENSAJE_ACTUALIZACION_FALLIDA);
        }
    }

    /** Refactorización Martin Fowler: Extract Method. */
    private void verificarReglaDelCurso(ModificacionReglaDTO modificacion) {
        if (reglaDAO.obtenerPorIdYCurso(modificacion.idRegla(), modificacion.idCurso()) == null) {
            throw new ExcepcionNegocio(MENSAJE_REGLA_AJENA_AL_CURSO);
        }
    }

    /** Refactorización Martin Fowler: Extract Method. */
    private void verificarDocenteTitular(ModificacionReglaDTO modificacion) {
        Curso curso = cursoDAO.buscarPorId(modificacion.idCurso());
        if (curso == null || !curso.esDocenteTitular(modificacion.idDocente())) {
            throw new ExcepcionNegocio(MENSAJE_DOCENTE_NO_TITULAR);
        }
    }

    @Override
    public List<Regla> listarReglasPorCurso(Long idCurso) {
        if (idCurso == null) {
            return Collections.emptyList();
        }
        return reglaDAO.listarPorCurso(idCurso);
    }

    @Override
    public List<Regla> listarReglasVigentesPorCurso(Long idCurso) {
        if (idCurso == null) {
            return Collections.emptyList();
        }
        return reglaDAO.listarVigentesPorCurso(idCurso);
    }

}
