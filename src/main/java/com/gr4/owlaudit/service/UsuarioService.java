package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.UsuarioDTO;

public interface UsuarioService {
    UsuarioDTO autenticar(String correo, String contrasena);
}
