package com.gr4.owlaudit.dto;

public class NuevaReglaDTO {
    private String tipoMotor;
    private String nombreRepresentativo;
    private String descripcionDetallada;
    private String parametroExacto;
    private String nivelSeveridad;
    private int ponderacion;

    public NuevaReglaDTO() {
    }

    public NuevaReglaDTO(String tipoMotor, String nombreRepresentativo, String descripcionDetallada,
                         String parametroExacto, String nivelSeveridad, int ponderacion) {
        this.tipoMotor = tipoMotor;
        this.nombreRepresentativo = nombreRepresentativo;
        this.descripcionDetallada = descripcionDetallada;
        this.parametroExacto = parametroExacto;
        this.nivelSeveridad = nivelSeveridad;
        this.ponderacion = ponderacion;
    }

    public String getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(String tipoMotor) {
        this.tipoMotor = tipoMotor;
    }

    public String getNombreRepresentativo() {
        return nombreRepresentativo;
    }

    public void setNombreRepresentativo(String nombreRepresentativo) {
        this.nombreRepresentativo = nombreRepresentativo;
    }

    public String getDescripcionDetallada() {
        return descripcionDetallada;
    }

    public void setDescripcionDetallada(String descripcionDetallada) {
        this.descripcionDetallada = descripcionDetallada;
    }

    public String getParametroExacto() {
        return parametroExacto;
    }

    public void setParametroExacto(String parametroExacto) {
        this.parametroExacto = parametroExacto;
    }

    public String getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(String nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public int getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(int ponderacion) {
        this.ponderacion = ponderacion;
    }

    @Override
    public String toString() {
        return "NuevaReglaDTO{" +
                "tipoMotor='" + tipoMotor + '\'' +
                ", nombreRepresentativo='" + nombreRepresentativo + '\'' +
                ", descripcionDetallada='" + descripcionDetallada + '\'' +
                ", parametroExacto='" + parametroExacto + '\'' +
                ", nivelSeveridad='" + nivelSeveridad + '\'' +
                ", ponderacion=" + ponderacion +
                '}';
    }
}
