package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;

import java.util.List;

public interface AsignacionesDAO {
    Asignacion crearAsignacion(Asignacion asignacion);

    List<Asignacion> listarAsignaciones();

    List<Asignacion> consultarAsignacionesDisponibles();

    List<Asignacion> consultarAsignacionesDeCurso(Long idCurso);

    List<Regla> consultarReglasDeAsignacion(Long idAsignacion);

    double consultarUmbralAprobacion(Long idAsignacion);

    Asignacion buscarPorId(Long id);

    void actualizar(Asignacion asignacion, List<Long> nuevasReglasIds);

    void eliminar(Long id);

    void resecuenciarIdsAsignaciones();
}
