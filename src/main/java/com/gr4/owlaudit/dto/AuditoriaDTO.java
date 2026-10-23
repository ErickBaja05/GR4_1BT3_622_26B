package com.gr4.owlaudit.dto;

public class AuditoriaDTO {
    private Long proyectoId;
    private String url;
    private Long usuarioId;

    public AuditoriaDTO() {
    }

    public AuditoriaDTO(String url) {
        this.url = url;
    }

    public AuditoriaDTO(Long proyectoId, String url) {
        this.proyectoId = proyectoId;
        this.url = url;
    }

    public AuditoriaDTO(Long proyectoId, String url, Long usuarioId) {
        this.proyectoId = proyectoId;
        this.url = url;
        this.usuarioId = usuarioId;
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    @Override
    public String toString() {
        return "AuditoriaDTO{" +
                "proyectoId=" + proyectoId +
                ", url='" + url + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}

