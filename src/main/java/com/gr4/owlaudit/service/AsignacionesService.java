package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;

import java.util.List;

public interface AsignacionesService {
    Asignacion crearAsignacion(NuevaAsignacionDTO dto);

    List<Asignacion> listarAsignaciones();

    List<Asignacion> consultarAsignacionesDisponibles();

    List<Regla> consultarReglasDeAsignacion(Long idAsignacion);

    double consultarUmbralAprobacion(Long idAsignacion);

    ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion);

    ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion, Long usuarioId);

    Asignacion buscarPorId(Long id);

    void actualizarAsignacion(Long idAsignacion, NuevaAsignacionDTO dto);

    void eliminarAsignacion(Long idAsignacion);
}
