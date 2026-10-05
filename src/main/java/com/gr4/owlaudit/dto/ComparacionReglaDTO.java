package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.NivelSeveridadEnum;

public class ComparacionReglaDTO {

    private String nombreRepresentativo;
    private NivelSeveridadEnum nivelSeveridad;
    private String estado; // "Nuevo", "Persistente", "Corregido"
    private String evidencia;
    private String recomendacion;

    public ComparacionReglaDTO() {
    }

    public ComparacionReglaDTO(String nombreRepresentativo, NivelSeveridadEnum nivelSeveridad,
                               String estado, String evidencia, String recomendacion) {
        this.nombreRepresentativo = nombreRepresentativo;
        this.nivelSeveridad = nivelSeveridad;
        this.estado = estado;
        this.evidencia = evidencia;
        this.recomendacion = recomendacion;
    }

    public String getNombreRepresentativo() {
        return nombreRepresentativo;
    }

    public void setNombreRepresentativo(String nombreRepresentativo) {
        this.nombreRepresentativo = nombreRepresentativo;
    }

    public NivelSeveridadEnum getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    @Override
    public String toString() {
        return "ComparacionReglaDTO{" +
                "nombreRepresentativo='" + nombreRepresentativo + '\'' +
                ", nivelSeveridad=" + nivelSeveridad +
                ", estado='" + estado + '\'' +
                '}';
    }
}
