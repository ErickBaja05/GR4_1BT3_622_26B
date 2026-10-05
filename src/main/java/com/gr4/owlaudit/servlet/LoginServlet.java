package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.service.UsuarioService;
import com.gr4.owlaudit.service.UsuarioServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Autenticación de usuarios (precondición de todos los casos de uso).
 *  - POST /login  : valida credenciales y crea la sesión.
 *  - GET  /logout : invalida la sesión y regresa al login.
 */
@WebServlet(urlPatterns = {"/login", "/logout"})
public class LoginServlet extends HttpServlet {

    /** Atributo de sesión donde se almacena el UsuarioDTO autenticado. */
    public static final String ATRIBUTO_USUARIO = "usuarioSesion";

    private final UsuarioService usuarioService = new UsuarioServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout".equals(request.getServletPath())) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/index.jsp?logout=1");
            return;
        }
        // GET /login: mostrar el formulario (o el menú si ya hay sesión)
        response.sendRedirect(request.getContextPath() + "/index.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if ("/logout".equals(request.getServletPath())) {
            doGet(request, response);
            return;
        }

        request.setCharacterEncoding("UTF-8");
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        try {
            UsuarioDTO usuario = usuarioService.autenticar(correo, contrasena);

            // Evita fijación de sesión: se descarta la sesión anónima previa
            HttpSession anterior = request.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(ATRIBUTO_USUARIO, usuario);

            System.out.println("[LoginServlet] Usuario autenticado: " + usuario.getCorreo() + " (" + usuario.getRol() + ")");
            response.sendRedirect(request.getContextPath() + "/vistas");
            return;

        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[LoginServlet] Error técnico al autenticar: " + e.getMessage());
            request.setAttribute("mensajeError",
                    "No fue posible iniciar sesión en este momento. Verifique la conexión con la base de datos e intente nuevamente.");
        }

        request.setAttribute("correo", correo);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
