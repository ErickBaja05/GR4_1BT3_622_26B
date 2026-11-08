package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.NivelSeveridadEnum;

/**
 * DTO used to associate a rule with an assignment, including its severity and weighting.
 */
public class ReglaAsignacionDTO {
    private Long reglaId;
    private NivelSeveridadEnum severidad;
    private Integer ponderacion;

    public ReglaAsignacionDTO() {}

    public ReglaAsignacionDTO(Long reglaId, NivelSeveridadEnum severidad, Integer ponderacion) {
        this.reglaId = reglaId;
        this.severidad = severidad;
        this.ponderacion = ponderacion;
    }

    public Long getReglaId() { return reglaId; }
    public void setReglaId(Long reglaId) { this.reglaId = reglaId; }

    public NivelSeveridadEnum getSeveridad() { return severidad; }
    public void setSeveridad(NivelSeveridadEnum severidad) { this.severidad = severidad; }

    public Integer getPonderacion() { return ponderacion; }
    public void setPonderacion(Integer ponderacion) { this.ponderacion = ponderacion; }
}

