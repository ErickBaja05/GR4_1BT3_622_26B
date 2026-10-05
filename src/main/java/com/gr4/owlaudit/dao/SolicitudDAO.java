package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.SolicitudRetroalimentacion;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.dao.SolicitudDAO <<DAO>>
 * Interfaz para las operaciones de persistencia de SolicitudRetroalimentacion.
 */
public interface SolicitudDAO {

    /**
     * Trazabilidad Diagrama de Secuencia: Mensaje dao.buscarPorHallazgoId(solicitudDTO.getHallazgoId())
     */
    SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId);

    /**
     * Trazabilidad Diagrama de Secuencia: Mensaje dao.crearSolicitudDeRetroalimentacion(solicitud)
     */
    void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud);

    /**
     * Busca una solicitud por su identificador primario.
     */
    SolicitudRetroalimentacion buscarPorId(Long solicitudId);

    /**
     * Actualiza el estado y orientación técnica de una solicitud existente.
     */
    void actualizarSolicitud(SolicitudRetroalimentacion solicitud);
}
