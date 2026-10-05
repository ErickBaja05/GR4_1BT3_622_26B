package com.gr4.owlaudit.dto;

/**
 * CU05 / CU06 - Datos de una solicitud de retroalimentación sobre un hallazgo.
 *  - CU05 (Estudiante): hallazgoId + justificacion.
 *  - CU06 (Docente)   : solicitudId + orientacion.
 */
public class SolicitudDTO {
    private Long hallazgoId;
    private Long solicitudId;
    private String justificacion;
    private String orientacion;

    public SolicitudDTO() {
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

    @Override
    public String toString() {
        return "SolicitudDTO{" +
                "hallazgoId=" + hallazgoId +
                ", solicitudId=" + solicitudId +
                ", justificacion='" + justificacion + '\'' +
                ", orientacion='" + orientacion + '\'' +
                '}';
    }
}
