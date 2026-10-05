package com.gr4.owlaudit.dto;

/**
 * CU04 - Datos de entrada para consultar la evolución de calidad de un proyecto.
 *  - proyectoId  : proyecto del que se consulta el historial (doGet).
 *  - baseId      : auditoría tomada como base de la comparación (doPost).
 *  - comparadaId : auditoría comparada contra la base (doPost).
 */
public class EvolucionDTO {
    private Long proyectoId;
    private Long baseId;
    private Long comparadaId;

    public EvolucionDTO() {
    }

    public EvolucionDTO(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public EvolucionDTO(Long proyectoId, Long baseId, Long comparadaId) {
        this.proyectoId = proyectoId;
        this.baseId = baseId;
        this.comparadaId = comparadaId;
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public Long getComparadaId() {
        return comparadaId;
    }

    public void setComparadaId(Long comparadaId) {
        this.comparadaId = comparadaId;
    }

    @Override
    public String toString() {
        return "EvolucionDTO{" +
                "proyectoId=" + proyectoId +
                ", baseId=" + baseId +
                ", comparadaId=" + comparadaId +
                '}';
    }
}
