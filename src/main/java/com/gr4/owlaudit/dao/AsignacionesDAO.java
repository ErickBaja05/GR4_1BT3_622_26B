package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;

import java.util.List;

public interface AsignacionesDAO {
    Asignacion crearAsignacion(Asignacion asignacion);

    List<Asignacion> listarAsignaciones();

    public List<Asignacion> consultarAsignacionesDisponibles();

    public List<Regla> consultarReglasDeAsignacion(Long idAsignacion);

    public double consultarUmbralAprobacion(Long idAsignacion);
}
