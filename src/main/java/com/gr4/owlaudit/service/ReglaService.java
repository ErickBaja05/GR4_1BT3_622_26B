package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;

public interface ReglaService {
    void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto);

    public boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado);

    default void actualizarRegla(Long id, NuevaReglaDTO dto) {}

    default void eliminarRegla(Long id) {}

    default com.gr4.owlaudit.model.Regla obtenerReglaPorId(Long id) {
        return null;
    }
}
