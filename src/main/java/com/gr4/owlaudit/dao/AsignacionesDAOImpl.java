package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Asignacion;
import java.util.List;

public class AsignacionesDAOImpl implements AsignacionesDAO {

    @Override
    public Asignacion crearAsignacion(Asignacion asignacion) {
        // Fase RED: Retorno nulo deliberado para que falle la prueba
        return null;
    }

    @Override
    public List<Asignacion> listarAsignaciones() {
        return null;
    }
}
