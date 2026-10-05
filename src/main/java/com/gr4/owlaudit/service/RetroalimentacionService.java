package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.SolicitudDTO;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.service.RetroalimentacionService
 * Interfaz de la capa de negocio para la gestión de retroalimentaciones de hallazgos.
 */
public interface RetroalimentacionService {

    /**
     * Trazabilidad Diagrama de Secuencia: servicio.solicitarRetroalimentacionDeHallazgo(solicitudDTO)
     */
    void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto);

    /**
     * Trazabilidad Diagrama de Clases: + registrarOrientacionTecnicaEnLaSolicitud(dto : SolicitudDTO) : void
     */
    void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto);
}
