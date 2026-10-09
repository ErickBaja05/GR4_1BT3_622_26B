package com.gr4.owlaudit.service;

import java.util.List;
import java.util.Map;

import com.gr4.owlaudit.dto.SolicitudDTO;

public interface RetroalimentacionService {
    void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto);
    void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto);
    List<Map<String, Object>> obtenerHallazgosDisponibles(Long proyectoId);
}
