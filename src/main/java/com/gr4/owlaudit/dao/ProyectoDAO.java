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
}
