package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import java.util.List;

public interface AsignacionesService {
    Asignacion crearAsignacion(NuevaAsignacionDTO dto);
    List<Asignacion> listarAsignaciones();
}
