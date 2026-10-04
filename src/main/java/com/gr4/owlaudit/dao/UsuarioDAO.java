package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Usuario;

public interface UsuarioDAO {
    Usuario buscarPorCorreo(String correo);
    Usuario buscarPorCredenciales(String correo, String contrasena);
    void guardar(Usuario usuario);
}
