package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Usuario;

import java.util.regex.Pattern;

public class ProyectoServiceImpl implements ProyectoService {

    private static final Pattern GITHUB_REPO_PATTERN = Pattern.compile(
        "^https?://(www\\.)?github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?$"
    );

    private final ProyectoDAO proyectoDAO;

    public ProyectoServiceImpl() {
        this(new ProyectoDAOImpl());
    }

    public ProyectoServiceImpl(ProyectoDAO proyectoDAO) {
        this.proyectoDAO = proyectoDAO;
    }

    @Override
    public void solicitarRegistroDeNuevoProyecto(NuevoProyectoDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos del proyecto no pueden ser nulos.");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ExcepcionNegocio("El nombre del proyecto es obligatorio.");
        }

        // Validación del enlace según secuencia2.puml
        validarEnlace(dto.getUrlGithub());

        // Instanciación y persistencia
        Proyecto proyecto = new Proyecto(dto);
        if (dto.getUsuarioId() != null) {
            Usuario u = new Usuario();
            u.setId(dto.getUsuarioId());
            proyecto.setUsuario(u);

            java.util.List<Proyecto> existentes = proyectoDAO.listarPorUsuarioId(dto.getUsuarioId());
            long proximoId = (existentes != null ? existentes.size() : 0) + 1L;
            proyecto.setIdEstudiante(proximoId);
            dto.setIdEstudiante(proximoId);
        } else {
            proyecto.setIdEstudiante(1L);
            dto.setIdEstudiante(1L);
        }
        proyectoDAO.guardarProyectoAcademico(proyecto);
    }

    private void validarEnlace(String urlGithub) {
        if (urlGithub == null || urlGithub.trim().isEmpty()) {
            throw new ExcepcionNegocio("Enlace no válido");
        }
        String urlTrim = urlGithub.trim();
        if (!GITHUB_REPO_PATTERN.matcher(urlTrim).matches()) {
            throw new ExcepcionNegocio("Enlace no válido");
        }
    }

    @Override
    public void actualizarProyecto(Long proyectoId, NuevoProyectoDTO dto, Long usuarioId) {
        if (proyectoId == null || dto == null || usuarioId == null) {
            throw new ExcepcionNegocio("Los datos para actualizar el proyecto son incompletos.");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ExcepcionNegocio("El nombre del proyecto es obligatorio.");
        }
        validarEnlace(dto.getUrlGithub());

        Proyecto proyecto = proyectoDAO.buscarPorId(proyectoId);
        if (proyecto == null) {
            throw new ExcepcionNegocio("El proyecto especificado no existe.");
        }
        if (proyecto.getUsuario() == null || !usuarioId.equals(proyecto.getUsuario().getId())) {
            throw new ExcepcionNegocio("No tiene permisos para modificar este proyecto. Solo puede gestionar sus propios proyectos.");
        }

        proyecto.setNombre(dto.getNombre().trim());
        proyecto.setUrlGithub(dto.getUrlGithub().trim());
        proyectoDAO.actualizar(proyecto);
    }

    @Override
    public void eliminarProyecto(Long proyectoId, Long usuarioId) {
        if (proyectoId == null || usuarioId == null) {
            throw new ExcepcionNegocio("Identificador de proyecto o usuario no válido.");
        }
        Proyecto proyecto = proyectoDAO.buscarPorId(proyectoId);
        if (proyecto == null) {
            throw new ExcepcionNegocio("El proyecto a eliminar no existe.");
        }
        if (proyecto.getUsuario() == null || !usuarioId.equals(proyecto.getUsuario().getId())) {
            throw new ExcepcionNegocio("No tiene permisos para eliminar este proyecto. Solo puede gestionar sus propios proyectos.");
        }

        proyectoDAO.eliminar(proyectoId);
        proyectoDAO.resecuenciarIdsEstudiante(usuarioId);
    }

    @Override
    public Proyecto buscarPorId(Long proyectoId) {
        if (proyectoId == null) {
            return null;
        }
        return proyectoDAO.buscarPorId(proyectoId);
    }

    @Override
    public java.util.List<Proyecto> listarProyectosDeEstudiante(Long usuarioId) {
        if (usuarioId == null) {
            return java.util.Collections.emptyList();
        }
        return proyectoDAO.listarPorUsuarioId(usuarioId);
    }
}
