package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;

public interface ReglaService {
    void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto);

    boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado);

    void actualizarRegla(Long id, NuevaReglaDTO dto);

    void eliminarRegla(Long id);

    Regla obtenerReglaPorId(Long id);
}
