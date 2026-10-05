package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.SolicitudRetroalimentacion;

public interface SolicitudDAO {
    SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId);
    void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud);
    SolicitudRetroalimentacion buscarPorId(Long solicitudId);
    void actualizarSolicitud(SolicitudRetroalimentacion solicitud);
}
