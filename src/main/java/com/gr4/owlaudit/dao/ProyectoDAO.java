package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Proyecto;
import java.util.List;

public interface ProyectoDAO {
    void guardarProyectoAcademico(Proyecto proyecto);
    List<Proyecto> listarTodos();
    Proyecto buscarPorId(Long id);
}

