package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;

public interface AuditoriaService {
    ResumenDTO solicitarPreviaDeAuditoria(AuditoriaDTO dto);
    ResultadoDTO confirmarEjecucionDeAuditoria(AuditoriaDTO dto);
}
