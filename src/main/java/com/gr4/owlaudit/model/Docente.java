package com.gr4.owlaudit.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("DOCENTE")
public class Docente extends Usuario {

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
}
