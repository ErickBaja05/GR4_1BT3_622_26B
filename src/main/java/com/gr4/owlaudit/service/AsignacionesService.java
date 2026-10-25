package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;

import java.util.List;

public interface AsignacionesService {
    Asignacion crearAsignacion(NuevaAsignacionDTO dto);

    List<Asignacion> listarAsignaciones();

    public List<Asignacion> consultarAsignacionesDisponibles();

    public List<Regla> consultarReglasDeAsignacion(Long idAsignacion);

    public double consultarUmbralAprobacion(Long idAsignacion);

    public ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion);
}
