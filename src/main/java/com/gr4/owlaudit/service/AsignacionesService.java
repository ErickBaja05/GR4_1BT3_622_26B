package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;

public interface AsignacionesService {
    Asignacion crearAsignacion(NuevaAsignacionDTO dto);
}
