package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.ModificacionReglaDTO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.Regla;
import java.util.List;

public interface ReglaService {
    void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto);

    void modificarDescripcionYVigencia(ModificacionReglaDTO modificacion);

    List<Regla> listarReglasPorCurso(Long idCurso);

    List<Regla> listarReglasVigentesPorCurso(Long idCurso);
}
