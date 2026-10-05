package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;

public interface EvolucionService {
    ResumenHistorialDTO solicitarEvolucionDeCalidad(EvolucionDTO dto);
    EvolucionFinalDTO indicarDosAuditoriasAComparar(EvolucionDTO dto);
}
