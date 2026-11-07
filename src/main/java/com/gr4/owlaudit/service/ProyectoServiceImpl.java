package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.Proyecto;

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

        // Validación de unicidad de nombre y URL para el mismo estudiante
        validarUnicidadParaEstudiante(dto.getUsuarioId(), dto.getNombre(), dto.getUrlGithub(), null);

        // Instanciación y persistencia
        Proyecto proyecto = new Proyecto(dto);
        if (dto.getUsuarioId() != null) {
            Estudiante e = new Estudiante();
            e.setId(dto.getUsuarioId());
            proyecto.setEstudiante(e);

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
        validarDatosActualizacion(proyectoId, dto, usuarioId);
        validarEnlace(dto.getUrlGithub());
        validarUnicidadParaEstudiante(usuarioId, dto.getNombre(), dto.getUrlGithub(), proyectoId);

        Proyecto proyecto = obtenerProyectoValido(proyectoId, usuarioId, "modificar");
        validarPropietario(proyecto, usuarioId, "modificar");

        // Patrón Martin Fowler: Move Method (actualizarDesdeDTO en Proyecto)
        proyecto.actualizarDesdeDTO(dto);
        proyectoDAO.actualizar(proyecto);
    }

    private void validarUnicidadParaEstudiante(Long usuarioId, String nombre, String urlGithub, Long proyectoIdExcluir) {
        if (usuarioId == null) return;
        java.util.List<Proyecto> existentes = proyectoDAO.listarPorUsuarioId(usuarioId);
        if (existentes == null || existentes.isEmpty()) return;

        String nombreTrim = nombre != null ? nombre.trim() : "";
        String urlNorm = normalizarUrl(urlGithub);

        for (Proyecto p : existentes) {
            if (proyectoIdExcluir != null && p.getId() != null && p.getId().equals(proyectoIdExcluir)) {
                continue;
            }
            if (p.getNombre() != null && p.getNombre().trim().equalsIgnoreCase(nombreTrim)) {
                throw new ExcepcionNegocio("Ya tiene un proyecto registrado con este nombre.");
            }
            if (p.getUrlGithub() != null && normalizarUrl(p.getUrlGithub()).equalsIgnoreCase(urlNorm)) {
                throw new ExcepcionNegocio("Ya tiene un proyecto registrado con este enlace de repositorio.");
            }
        }
    }

    private String normalizarUrl(String url) {
        if (url == null) return "";
        String norm = url.trim().toLowerCase();
        while (norm.endsWith("/")) {
            norm = norm.substring(0, norm.length() - 1);
        }
        if (norm.endsWith(".git")) {
            norm = norm.substring(0, norm.length() - 4);
        }
        return norm;
    }

    @Override
    public void eliminarProyecto(Long proyectoId, Long usuarioId) {
        if (proyectoId == null || usuarioId == null) {
            throw new ExcepcionNegocio("Identificador de proyecto o usuario no válido.");
        }
        Proyecto proyecto = obtenerProyectoValido(proyectoId, usuarioId, "eliminar");
        validarPropietario(proyecto, usuarioId, "eliminar");

        proyectoDAO.eliminar(proyecto.getId());
        proyectoDAO.resecuenciarIdsEstudiante(usuarioId);
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private void validarDatosActualizacion(Long proyectoId, NuevoProyectoDTO dto, Long usuarioId) {
        if (proyectoId == null || dto == null || usuarioId == null) {
            throw new ExcepcionNegocio("Los datos para actualizar el proyecto son incompletos.");
        }
        if (dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new ExcepcionNegocio("El nombre del proyecto es obligatorio.");
        }
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private Proyecto obtenerProyectoValido(Long proyectoId, Long usuarioId, String accion) {
        Proyecto proyecto = proyectoDAO.buscarPorIdYUsuarioId(proyectoId, usuarioId);
        if (proyecto != null) {
            return proyecto;
        }
        Proyecto ajeno = proyectoDAO.buscarPorId(proyectoId);
        if (ajeno != null) {
            return ajeno;
        }
        throw new ExcepcionNegocio("El proyecto a " + accion + " no existe.");
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private void validarPropietario(Proyecto proyecto, Long usuarioId, String accion) {
        if (proyecto.getUsuario() == null || !usuarioId.equals(proyecto.getUsuario().getId())) {
            throw new ExcepcionNegocio("No tiene permisos para " + accion + " este proyecto. Solo puede gestionar sus propios proyectos.");
        }
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
