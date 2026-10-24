package com.gr4.owlaudit.dto;

public class NuevoProyectoDTO {
    private String nombre;
    private String urlGithub;
    private Long usuarioId;
    private Long idEstudiante;

    public NuevoProyectoDTO() {
    }

    public NuevoProyectoDTO(String nombre, String urlGithub) {
        this.nombre = nombre;
        this.urlGithub = urlGithub;
    }

    public NuevoProyectoDTO(String nombre, String urlGithub, Long usuarioId) {
        this.nombre = nombre;
        this.urlGithub = urlGithub;
        this.usuarioId = usuarioId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlGithub() {
        return urlGithub;
    }

    public void setUrlGithub(String urlGithub) {
        this.urlGithub = urlGithub;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Long idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    @Override
    public String toString() {
        return "NuevoProyectoDTO{" +
                "nombre='" + nombre + '\'' +
                ", urlGithub='" + urlGithub + '\'' +
                ", usuarioId=" + usuarioId +
                '}';
    }
}
