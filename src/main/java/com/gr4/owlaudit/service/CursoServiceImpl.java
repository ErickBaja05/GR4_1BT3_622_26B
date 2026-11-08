package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dto.NuevoCursoDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.Usuario;

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

    @Override
    public Curso crearCurso(NuevoCursoDTO dto, String correoDocente) throws ExcepcionNegocio {
        validarCamposObligatorios(dto);

        String codigoNormalizado = normalizarCodigo(dto.getCodigo());
        validarFormatoCodigo(codigoNormalizado);
        validarUnicidadCodigo(codigoNormalizado);

        Docente docente = obtenerDocentePorCorreo(correoDocente);
        Curso curso = Curso.construirNuevoCurso(dto.getNombre(), codigoNormalizado, dto.getDescripcion(), docente);

        cursoDAO.guardar(curso);
        return curso;
    }

    private Docente obtenerDocentePorCorreo(String correoDocente) throws ExcepcionNegocio {
        if (esTextoVacio(correoDocente)) {
            throw new ExcepcionNegocio("El docente especificado no existe en el sistema");
        }
        Usuario usuario = usuarioDAO.buscarPorCorreo(correoDocente.trim());
        if (usuario == null) {
            throw new ExcepcionNegocio("El docente especificado no existe en el sistema");
        }
        if (usuario.getRol() != RolEnum.DOCENTE) {
            throw new ExcepcionNegocio("Solo un usuario con rol de docente puede crear cursos");
        }
        return (usuario instanceof Docente) ? (Docente) usuario : Docente.desdeUsuario(usuario);
    }

    @Override
    public List<Curso> listarCursosPorCorreoDocente(String correoDocente) {
        Usuario usuario = usuarioDAO.buscarPorCorreo(correoDocente);
        if (usuario == null || usuario.getId() == null) {
            return Collections.emptyList();
        }
        return cursoDAO.listarCursosPorDocente(usuario.getId());
    }

    // Mantener los métodos que ya existían de HU-03 para no romper compatibilidad:
    @Override
    public List<Curso> listarCursosPorDocente(Long idDocente) {
        return cursoDAO.listarCursosPorDocente(idDocente);
    }

    @Override
    public List<Curso> listarCursosPorEstudiante(Long idEstudiante) {
        return cursoDAO.listarCursosPorEstudiante(idEstudiante);
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
}
