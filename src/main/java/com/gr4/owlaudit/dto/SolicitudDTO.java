package com.gr4.owlaudit.dto;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.dto.SolicitudDTO
 * Objeto de Transferencia de Datos (DTO) para transportar la información de la solicitud
 * de retroalimentación de hallazgos entre capas.
 */
public class SolicitudDTO {

    private Long hallazgoId;
    private Long solicitudId;
    private String justificacion;
    private String orientacion;

    public SolicitudDTO() {
    }

    public SolicitudDTO(Long hallazgoId, String justificacion) {
        this.hallazgoId = hallazgoId;
        this.justificacion = justificacion;
    }

    public SolicitudDTO(Long hallazgoId, Long solicitudId, String justificacion, String orientacion) {
        this.hallazgoId = hallazgoId;
        this.solicitudId = solicitudId;
        this.justificacion = justificacion;
        this.orientacion = orientacion;
    }

    public Long getHallazgoId() {
        return hallazgoId;
    }

    public void setHallazgoId(Long hallazgoId) {
        this.hallazgoId = hallazgoId;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(String orientacion) {
        this.orientacion = orientacion;
    }
}
