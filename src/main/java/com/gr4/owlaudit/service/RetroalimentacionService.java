package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.SolicitudDTO;

public interface RetroalimentacionService {
    void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto);
    void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto);
}
