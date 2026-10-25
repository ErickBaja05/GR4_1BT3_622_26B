package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Asignacion;
import java.util.List;

public interface AsignacionesDAO {
    Asignacion crearAsignacion(Asignacion asignacion);
    List<Asignacion> listarAsignaciones();
}
