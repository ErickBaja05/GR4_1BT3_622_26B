package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import java.util.List;

public interface SolicitudDAO {
    SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId);
    void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud);
    SolicitudRetroalimentacion buscarPorId(Long solicitudId);
    void actualizarSolicitud(SolicitudRetroalimentacion solicitud);
    List<SolicitudRetroalimentacion> consultarSolicitudesPendientes();
    List<SolicitudRetroalimentacion> buscarPendientes();
    Hallazgo buscarHallazgoPorId(Long hallazgoId);
    List<SolicitudRetroalimentacion> consultarTodasLasSolicitudes();
}
