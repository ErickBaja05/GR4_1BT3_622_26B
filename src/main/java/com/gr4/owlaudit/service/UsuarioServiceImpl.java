package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.UsuarioDAO;
import com.gr4.owlaudit.dao.UsuarioDAOImpl;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Usuario;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioServiceImpl() {
        this(new UsuarioDAOImpl());
    }

    public UsuarioServiceImpl(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UsuarioDTO autenticar(String correo, String contrasena) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new ExcepcionNegocio("El correo electrónico es obligatorio.");
        }
        if (contrasena == null || contrasena.trim().isEmpty()) {
            throw new ExcepcionNegocio("La contraseña es obligatoria.");
        }

        Usuario usuario = usuarioDAO.buscarPorCredenciales(correo.trim(), contrasena);
        if (usuario == null) {
            throw new ExcepcionNegocio("Credenciales inválidas: correo o contraseña incorrectos.");
        }

        if (usuario.getRol() == null) {
            throw new ExcepcionNegocio("El usuario no cuenta con un rol asignado en el sistema.");
        }

        // Retorna el DTO excluyendo estrictamente la contraseña
        return new UsuarioDTO(usuario.getId(), usuario.getNombre(), usuario.getCorreo(), usuario.getRol());
    }
}
