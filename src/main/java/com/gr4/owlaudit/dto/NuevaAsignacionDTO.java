package com.gr4.owlaudit.dto;

import java.util.List;

public class NuevaAsignacionDTO {

    private String nombre;
    private String umbral;
    private List<Long> reglasIds;

    public NuevaAsignacionDTO() {
    }

    public NuevaAsignacionDTO(String nombre, String umbral, List<Long> reglasIds) {
        this.nombre = nombre;
        this.umbral = umbral;
        this.reglasIds = reglasIds;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUmbral() {
        return umbral;
    }

    public void setUmbral(String umbral) {
        this.umbral = umbral;
    }

    public List<Long> getReglasIds() {
        return reglasIds;
    }

    public void setReglasIds(List<Long> reglasIds) {
        this.reglasIds = reglasIds;
    }
}
