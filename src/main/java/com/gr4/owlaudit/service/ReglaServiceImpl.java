package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import java.util.List;

public class ReglaServiceImpl implements ReglaService {

    public static final String MENSAJE_DUPLICADO_CURSO = "Ya existe una regla con ese nombre representativo en este curso";

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
    public boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado) {
        // 1. Obtener la regla actual desde el DAO
        Regla reglaActual = reglaDAO.obtenerPorNombre(nombreRegla);

        // Validación defensiva: si la regla no existe o el estado enviado es nulo
        if (reglaActual == null || nuevoEstado == null) {
            return false;
        }

        // 2. Validar que el estado solicitado sea distinto al actual (Escenario 1.3)
        if (reglaActual.getVigencia() == nuevoEstado) {
            return false;
        }

        // 3. Actualizar el estado en el objeto de dominio y persistir invocando al DAO
        reglaActual.setVigencia(nuevoEstado);
        return reglaDAO.actualizarEstadoVigencia(nombreRegla, nuevoEstado);
    }

    @Override
    public List<Regla> listarReglasPorCurso(Long idCurso) {
        return null;
    }

    @Override
    public List<Regla> listarReglasVigentesPorCurso(Long idCurso) {
        return null;
    }

}
