package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dao.UsuarioDAOImpl;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.Usuario;

import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.ListadoEstudiantes;
import java.time.LocalDateTime;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class CursoServiceImpl implements CursoService {

    private static final Pattern PATRON_CODIGO_CURSO = Pattern.compile("^[A-Za-z0-9-]+$");

    private final CursoDAO cursoDAO;
    private final UsuarioDAO usuarioDAO;

    public CursoServiceImpl(CursoDAO cursoDAO, UsuarioDAO usuarioDAO) {
        this.cursoDAO = cursoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public CursoServiceImpl() {
        this.cursoDAO = new CursoDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();

    }

    @Override
    public Curso crearCurso(NuevoCursoDTO dto, String correoDocente) throws ExcepcionNegocio {
        validarCamposObligatorios(dto);

        String codigoNormalizado = normalizarCodigo(dto.getCodigo());
        validarFormatoCodigo(codigoNormalizado);
        validarUnicidadCodigo(codigoNormalizado);

        Usuario usuario = usuarioDAO.buscarPorCorreo(correoDocente);
        if (usuario == null) {
            throw new ExcepcionNegocio("El docente especificado no existe en el sistema");
        }
        if (usuario.getRol() != RolEnum.DOCENTE) {
            throw new ExcepcionNegocio("Solo un usuario con rol de docente puede crear cursos");
        }

        Docente docente = (usuario instanceof Docente)
                ? (Docente) usuario
                : Docente.desdeUsuario(usuario);

        Curso curso = new Curso();
        curso.setNombre(dto.getNombre().trim());
        curso.setCodigo(codigoNormalizado);
        curso.setDescripcion(dto.getDescripcion().trim());
        curso.setPeriodoAcademico("2026B");
        curso.setDocente(docente);
        cursoDAO.guardar(curso);

        return curso;
    }

    @Override
    public List<Curso> listarCursosPorCorreoDocente(String correoDocente) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correoDocente);
        if (usuario == null || usuario.getId() == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorDocente(usuario.getId());
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

    // Mantener los métodos que ya existían de HU-03 para no romper compatibilidad:
    @Override
    public List<Curso> listarCursosPorDocente(Long idDocente) {
        return cursoDAO.listarCursosPorDocente(idDocente);
    }

    @Override
    public List<Curso> listarCursosPorEstudiante(Long idEstudiante) {
        if (idEstudiante == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorEstudiante(idEstudiante);
    }

    @Override
    public List<Curso> listarCursosPorCorreoEstudiante(String correoEstudiante) {
        if (correoEstudiante == null || correoEstudiante.isBlank()) {
            return Collections.emptyList();
        }
        Usuario usuario = usuarioDAO.buscarPorCorreo(correoEstudiante);
        if (usuario == null || usuario.getId() == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorEstudiante(usuario.getId());
    }

    private void validarCamposObligatorios(NuevoCursoDTO dto) throws ExcepcionNegocio {
        if (dto == null
                || esTextoVacio(dto.getNombre())
                || esTextoVacio(dto.getCodigo())
                || esTextoVacio(dto.getDescripcion())) {
            throw new ExcepcionNegocio(
                    "Todos los campos (nombre, código y descripción) son obligatorios para crear el curso");
        }
    }

    private boolean esTextoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase();
    }

    private void validarFormatoCodigo(String codigo) throws ExcepcionNegocio {
        if (!PATRON_CODIGO_CURSO.matcher(codigo).matches()) {
            throw new ExcepcionNegocio("El código solo admite caracteres alfanuméricos y guiones");
        }
    }

    private void validarUnicidadCodigo(String codigo) throws ExcepcionNegocio {
        if (cursoDAO.buscarPorCodigo(codigo) != null) {
            throw new ExcepcionNegocio("El código de curso ingresado ya se encuentra en uso; ingrese un código único");
        }
    }

    @Override
    public Curso matricularEstudiantePorCodigo(Long idEstudiante, String codigoCurso) throws ExcepcionNegocio {
        Curso curso = obtenerCursoPorCodigoValido(codigoCurso);
        validarEstudianteNoMatriculado(curso, idEstudiante);

        Estudiante estudiante = new Estudiante();
        estudiante.setId(idEstudiante);

        ListadoEstudiantes nuevaMatricula = ListadoEstudiantes.crearNuevaMatricula(curso, estudiante);
        cursoDAO.registrarMatricula(nuevaMatricula);
        return curso;
    }

    private void validarEstudianteNoMatriculado(Curso curso, Long idEstudiante) throws ExcepcionNegocio {
        if (cursoDAO.estaEstudianteMatriculado(curso.getId(), idEstudiante)
                || curso.estaMatriculado(idEstudiante)) {
            throw new ExcepcionNegocio("Ya te encuentras matriculado en el curso " + curso.getNombre());
        }
    }

    private Curso obtenerCursoPorCodigoValido(String codigoCurso) throws ExcepcionNegocio {
        Curso curso = esTextoVacio(codigoCurso)
                ? null
                : cursoDAO.buscarPorCodigo(normalizarCodigo(codigoCurso));
        if (curso == null) {
            throw new ExcepcionNegocio("No existe ningún curso registrado con el código ingresado");
        }
        return curso;
    }
}
