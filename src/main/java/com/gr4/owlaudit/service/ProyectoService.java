package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.model.Proyecto;
import java.util.List;

public interface ProyectoService {
    void solicitarRegistroDeNuevoProyecto(NuevoProyectoDTO dto);

    void actualizarProyecto(Long proyectoId, NuevoProyectoDTO dto, Long usuarioId);

    void eliminarProyecto(Long proyectoId, Long usuarioId);

    Proyecto buscarPorId(Long proyectoId);

    List<Proyecto> listarProyectosDeEstudiante(Long usuarioId);
}
