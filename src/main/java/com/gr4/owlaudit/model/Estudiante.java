package com.gr4.owlaudit.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ESTUDIANTE")
public class Estudiante extends Usuario {

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
}
