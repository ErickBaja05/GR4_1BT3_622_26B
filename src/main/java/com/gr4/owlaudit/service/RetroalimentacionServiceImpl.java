package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dao.SolicitudDAOImpl;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.model.EstadoRetroEnum;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;

public class RetroalimentacionServiceImpl implements RetroalimentacionService {

    private static final String MENSAJE_ORIENTACION_INVALIDA = "Orientación en blanco o inválida";
    private static final String MENSAJE_HALLAZGO_EN_REVISION = "Hallazgo en revisión";

    private final SolicitudDAO solicitudDAO;

    public RetroalimentacionServiceImpl() {
        this(new SolicitudDAOImpl());
    }

    public RetroalimentacionServiceImpl(SolicitudDAO solicitudDAO) {
        this.solicitudDAO = solicitudDAO;
    }

    @Override
    public void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto) {
        if (dto == null || dto.getHallazgoId() == null || dto.getHallazgoId() <= 0) {
            throw new ExcepcionNegocio("El identificador del hallazgo no es válido.");
        }

        verificarEstadoDeRevisionDelHallazgo(dto.getHallazgoId());

        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion(dto);
        if (solicitud.getHallazgo() == null) {
            com.gr4.owlaudit.model.Hallazgo h = new com.gr4.owlaudit.model.Hallazgo();
            h.setId(dto.getHallazgoId());
            solicitud.setHallazgo(h);
        }
        solicitudDAO.crearSolicitudDeRetroalimentacion(solicitud);
    }

    @Override
    public void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }

        validarOrientacionRedactada(dto.getOrientacion());

        if (dto.getSolicitudId() == null || dto.getSolicitudId() <= 0) {
            throw new ExcepcionNegocio("Identificador de solicitud no válido.");
        }

        SolicitudRetroalimentacion solicitud = solicitudDAO.buscarPorId(dto.getSolicitudId());
        if (solicitud == null) {
            throw new ExcepcionNegocio("La solicitud de retroalimentación no existe.");
        }

        if (solicitud.getEstado() != EstadoRetroEnum.PENDIENTE) {
            throw new ExcepcionNegocio("La solicitud ya ha sido atendida.");
        }

        solicitud.setOrientacionTecnica(dto.getOrientacion().trim());
        solicitud.marcarSolicitudComoAtendida();
        solicitudDAO.actualizarSolicitud(solicitud);
    }

    // ========================================================
    // MÉTODOS PRIVADOS DEL DIAGRAMA DE CLASES
    // ========================================================

    private void verificarEstadoDeRevisionDelHallazgo(Long hallazgoId) {
        SolicitudRetroalimentacion existente = solicitudDAO.buscarPorHallazgoId(hallazgoId);
        if (existente != null) {
            throw new ExcepcionNegocio(MENSAJE_HALLAZGO_EN_REVISION);
        }
    }

    private void validarOrientacionRedactada(String orientacion) {
        if (orientacion == null) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }
        String texto = orientacion.trim();
        if (texto.isEmpty() || texto.length() < 10 || texto.length() > 1000) {
            throw new ExcepcionNegocio(MENSAJE_ORIENTACION_INVALIDA);
        }
    }
}
