package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dto.UsuarioDTO;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Protege las funcionalidades del sistema:
 *  1. Exige una sesión autenticada (si no, redirige al login).
 *  2. Verifica que el rol del usuario tenga permiso sobre la funcionalidad (según OpcionMenu).
 */
@WebFilter(urlPatterns = {"/vistas", "/proyecto", "/regla", "/auditoria"})
public class AutenticacionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Evita que el navegador muestre páginas protegidas desde caché tras cerrar sesión
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = session == null ? null : (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/index.jsp?sesion=requerida");
            return;
        }

        OpcionMenu opcion = OpcionMenu.porRuta(request.getServletPath());
        if (opcion != null && (!opcion.permiteRol(usuario.getRol()) || !opcion.isDisponible())) {
            response.sendRedirect(request.getContextPath() + "/vistas?error=acceso");
            return;
        }

        chain.doFilter(req, res);
    }
}
