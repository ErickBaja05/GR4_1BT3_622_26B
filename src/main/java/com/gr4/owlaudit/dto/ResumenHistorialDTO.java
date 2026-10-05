package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

public class ResumenHistorialDTO {

    private Long proyectoId;
    private List<AuditoriaResumenDTO> auditorias;

    public ResumenHistorialDTO() {
        this.auditorias = new ArrayList<>();
    }

    public ResumenHistorialDTO(Long proyectoId, List<AuditoriaResumenDTO> auditorias) {
        this.proyectoId = proyectoId;
        this.auditorias = auditorias != null ? auditorias : new ArrayList<>();
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public List<AuditoriaResumenDTO> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriaResumenDTO> auditorias) {
        this.auditorias = auditorias;
    }
}
