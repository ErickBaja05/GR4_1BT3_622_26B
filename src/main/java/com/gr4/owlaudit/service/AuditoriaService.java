package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.AuditoriaResumenDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.model.Auditoria;

import java.util.List;

public interface AuditoriaService {
    ResumenDTO solicitarPreviaDeAuditoria(AuditoriaDTO dto);

    ResultadoDTO confirmarEjecucionDeAuditoria(AuditoriaDTO dto);

    ResultadoDTO ejecutarAuditoriaDeAsignacion(AuditoriaDTO auditoriaDTO);

    void revalorizarAuditoriasDeAsignacion(Long asignacionId);

    ResultadoDTO obtenerDetalleAuditoria(Long auditoriaId);

    ResultadoDTO convertirAuditoriaAResultadoDTO(Auditoria auditoria);

    List<AuditoriaResumenDTO> consultarEntregasDeAsignacionParaDocente(Long idAsignacion, Long docenteId);
}
