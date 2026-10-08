package com.gr4.owlaudit.dto;

public class AuditoriaDTO {
    private Long proyectoId;
    private String url;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(String url) {
        this.url = url;
    }

    public AuditoriaDTO(Long proyectoId, String url) {
        this.proyectoId = proyectoId;
        this.url = url;
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "AuditoriaDTO{" +
                "proyectoId=" + proyectoId +
                ", url='" + url + '\'' +
                '}';
    }
}

