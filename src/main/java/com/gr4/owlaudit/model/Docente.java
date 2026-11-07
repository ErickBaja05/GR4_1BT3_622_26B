package com.gr4.owlaudit.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("DOCENTE")
public class Docente extends Usuario {

    @OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
    private List<Curso> cursos = new ArrayList<>();

    @OneToMany(mappedBy = "docente", fetch = FetchType.LAZY)
    private List<SolicitudRetroalimentacion> solicitudesAtendidas = new ArrayList<>();

    public Docente() {
        super();
        setRol(RolEnum.DOCENTE);
    }

    public Docente(String nombre, String correo, String contrasena) {
        super(nombre, correo, contrasena, RolEnum.DOCENTE);
    }

    public Docente(Long id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, RolEnum.DOCENTE);
    }

    public static Docente desdeUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        if (usuario instanceof Docente) {
            return (Docente) usuario;
        }
        return new Docente(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getContrasena());
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos != null ? new ArrayList<>(cursos) : new ArrayList<>();
    }

    public List<SolicitudRetroalimentacion> getSolicitudesAtendidas() {
        return solicitudesAtendidas;
    }

    public void setSolicitudesAtendidas(List<SolicitudRetroalimentacion> solicitudesAtendidas) {
        this.solicitudesAtendidas = solicitudesAtendidas != null ? new ArrayList<>(solicitudesAtendidas) : new ArrayList<>();
    }
}
