package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dao.SolicitudDAOImpl;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.service.RetroalimentacionServiceImpl
 * Implementación de las reglas de negocio para el CU05 - Solicitar Retroalimentación de Hallazgo.
 */
public class RetroalimentacionServiceImpl implements RetroalimentacionService {

    private final SolicitudDAO solicitudDAO;

    public RetroalimentacionServiceImpl() {
        this.solicitudDAO = new SolicitudDAOImpl();
    }

    public RetroalimentacionServiceImpl(SolicitudDAO solicitudDAO) {
        this.solicitudDAO = solicitudDAO;
    }

    /**
     * Trazabilidad Diagrama de Secuencia [02]: servicio.solicitarRetroalimentacionDeHallazgo(solicitudDTO)
     * Ejecuta el flujo principal del CU05 validando precondiciones, reglas de negocio y persistencia.
     */
    @Override
    public void solicitarRetroalimentacionDeHallazgo(SolicitudDTO dto) {
        // Validar entradas básicas del DTO según Datos Estandarizados del CU05
        if (dto == null || dto.getHallazgoId() == null || dto.getHallazgoId() <= 0) {
            throw new ExcepcionNegocio("El identificador del hallazgo debe ser un número entero positivo mayor a cero.");
        }

        // Regla de Negocio (Escenario Alternativo 2): Validación de la justificación técnica
        String justificacion = dto.getJustificacion();
        if (justificacion == null || justificacion.trim().length() < 10 || justificacion.trim().length() > 1000) {
            throw new ExcepcionNegocio("Debe proporcionar una justificación técnica válida");
        }

        // Trazabilidad Diagrama de Secuencia [03]: Verificar estado de revisión del hallazgo
        verificarEstadoDeRevisionDelHallazgo(dto.getHallazgoId());

        // Trazabilidad Diagrama de Secuencia [04]: Crear solicitud de retroalimentación
        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion(dto);
        solicitudDAO.crearSolicitudDeRetroalimentacion(solicitud);
    }

    /**
     * Trazabilidad Diagrama de Clases: + registrarOrientacionTecnicaEnLaSolicitud(dto : SolicitudDTO) : void
     */
    @Override
    public void registrarOrientacionTecnicaEnLaSolicitud(SolicitudDTO dto) {
        if (dto == null || dto.getSolicitudId() == null || dto.getSolicitudId() <= 0) {
            throw new ExcepcionNegocio("El identificador de la solicitud es inválido.");
        }

        validarOrientacionRedactada(dto.getOrientacion());

        SolicitudRetroalimentacion solicitud = solicitudDAO.buscarPorId(dto.getSolicitudId());
        if (solicitud == null) {
            throw new ExcepcionNegocio("La solicitud especificada no existe en el sistema.");
        }

        solicitud.setOrientacionTecnica(dto.getOrientacion().trim());
        solicitud.marcarSolicitudComoAtendida();
        solicitudDAO.actualizarSolicitud(solicitud);
    }

    /**
     * Trazabilidad Diagrama de Clases / Secuencia [03]:
     * - verificarEstadoDeRevisionDelHallazgo(hallazgoId : Long) : void
     * Consulta el DAO para determinar si ya existe una solicitud registrada previamente para el hallazgo.
     */
    private void verificarEstadoDeRevisionDelHallazgo(Long hallazgoId) {
        // Trazabilidad Diagrama de Secuencia: dao.buscarPorHallazgoId(solicitudDTO.getHallazgoId())
        SolicitudRetroalimentacion solicitudExistente = solicitudDAO.buscarPorHallazgoId(hallazgoId);

        // Trazabilidad Diagrama de Secuencia [06 / Alt]: ¿Ya tiene solicitud? -> throw ExcepcionNegocio
        if (solicitudExistente != null) {
            throw new ExcepcionNegocio("El hallazgo ya se encuentra en proceso de revisión");
        }
    }

    /**
     * Trazabilidad Diagrama de Clases: - validarOrientacionRedactada(orientacion : String) : void
     */
    private void validarOrientacionRedactada(String orientacion) {
        if (orientacion == null || orientacion.trim().isEmpty() || orientacion.trim().length() < 10) {
            throw new ExcepcionNegocio("La orientación técnica debe ser válida y contener al menos 10 caracteres.");
        }
    }
}
