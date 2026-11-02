package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Proyecto;
import java.util.List;

public interface ProyectoDAO {
    void guardarProyectoAcademico(Proyecto proyecto);

    List<Proyecto> listarTodos();

    Proyecto buscarPorId(Long id);

    List<Proyecto> listarPorUsuarioId(Long usuarioId);

    void actualizar(Proyecto proyecto);

    void eliminar(Long id);

    void resecuenciarIdsEstudiante(Long usuarioId);

    default Proyecto buscarPorIdYUsuarioId(Long proyectoId, Long usuarioId) {
        if (proyectoId == null || usuarioId == null) return null;
        Proyecto p = buscarPorId(proyectoId);
        if (p != null && p.getUsuario() != null && usuarioId.equals(p.getUsuario().getId())) {
            return p;
        }
        List<Proyecto> usuarioProyectos = listarPorUsuarioId(usuarioId);
        if (usuarioProyectos != null) {
            for (Proyecto up : usuarioProyectos) {
                if (proyectoId.equals(up.getIdEstudiante()) || proyectoId.equals(up.getId())) {
                    return up;
                }
            }
        }
        return null;
    }
}
