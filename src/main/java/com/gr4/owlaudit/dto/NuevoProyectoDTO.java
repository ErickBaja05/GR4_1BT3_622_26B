package com.gr4.owlaudit.dto;

public class NuevoProyectoDTO {
    private String nombre;
    private String urlGithub;

    public NuevoProyectoDTO() {
    }

    public NuevoProyectoDTO(String nombre, String urlGithub) {
        this.nombre = nombre;
        this.urlGithub = urlGithub;
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
}
