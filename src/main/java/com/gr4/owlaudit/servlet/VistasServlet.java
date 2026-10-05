package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.dto.UsuarioDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Panel principal posterior al login.
 *  - GET /vistas                 : muestra las opciones habilitadas para el rol del usuario.
 *  - GET /vistas?opcion={clave}  : redirige al Servlet de la funcionalidad elegida (si el rol lo permite).
 *
 * La sesión ya fue verificada por AutenticacionFilter.
 */
@WebServlet("/vistas")
public class VistasServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        String clave = request.getParameter("opcion");

        if (clave != null && !clave.isBlank()) {
            OpcionMenu opcion = OpcionMenu.porClave(clave);

            if (opcion == null || !opcion.permiteRol(usuario.getRol())) {
                request.setAttribute("mensajeError", "No tiene permisos para acceder a la funcionalidad solicitada.");
            } else if (!opcion.isDisponible()) {
                request.setAttribute("mensajeError", "La funcionalidad '" + opcion.getTitulo() + "' estará disponible en el Incremento 2.");
            } else {
                response.sendRedirect(request.getContextPath() + opcion.getRuta());
                return;
            }
        }

        if ("acceso".equals(request.getParameter("error"))) {
            request.setAttribute("mensajeError", "No tiene permisos para acceder a la funcionalidad solicitada.");
        }

        request.setAttribute("opciones", OpcionMenu.opcionesPara(usuario.getRol()));
        request.getRequestDispatcher("/WEB-INF/views/menu.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
