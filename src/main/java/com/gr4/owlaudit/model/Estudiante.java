package com.gr4.owlaudit.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("ESTUDIANTE")
public class Estudiante extends Usuario {

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<ListadoEstudiantes> matriculas = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<Proyecto> proyectos = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<Auditoria> auditorias = new ArrayList<>();

    @OneToMany(mappedBy = "estudiante", fetch = FetchType.LAZY)
    private List<SolicitudRetroalimentacion> solicitudesRetroalimentacion = new ArrayList<>();

    public Estudiante() {
        super();
        setRol(RolEnum.ESTUDIANTE);
    }

    public Estudiante(String nombre, String correo, String contrasena) {
        super(nombre, correo, contrasena, RolEnum.ESTUDIANTE);
    }

    public Estudiante(Long id, String nombre, String correo, String contrasena) {
        super(id, nombre, correo, contrasena, RolEnum.ESTUDIANTE);
    }

    public static Estudiante desdeUsuario(Usuario usuario) {
        if (usuario == null) {
            return null;
        }
        if (usuario instanceof Estudiante) {
            return (Estudiante) usuario;
        }
        return new Estudiante(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getContrasena());
    }

    public List<ListadoEstudiantes> getMatriculas() {
        return matriculas;
    }

    public void setMatriculas(List<ListadoEstudiantes> matriculas) {
        this.matriculas = matriculas != null ? new ArrayList<>(matriculas) : new ArrayList<>();
    }

    public List<Proyecto> getProyectos() {
        return proyectos;
    }

    public void setProyectos(List<Proyecto> proyectos) {
        this.proyectos = proyectos != null ? new ArrayList<>(proyectos) : new ArrayList<>();
    }

    public List<Auditoria> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<Auditoria> auditorias) {
        this.auditorias = auditorias != null ? new ArrayList<>(auditorias) : new ArrayList<>();
    }

    public List<SolicitudRetroalimentacion> getSolicitudesRetroalimentacion() {
        return solicitudesRetroalimentacion;
    }

    public void setSolicitudesRetroalimentacion(List<SolicitudRetroalimentacion> solicitudesRetroalimentacion) {
        this.solicitudesRetroalimentacion = solicitudesRetroalimentacion != null ? new ArrayList<>(solicitudesRetroalimentacion) : new ArrayList<>();
    }
}
