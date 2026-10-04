package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
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

        // Instanciación y persistencia
        Proyecto proyecto = new Proyecto(dto);
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
}
