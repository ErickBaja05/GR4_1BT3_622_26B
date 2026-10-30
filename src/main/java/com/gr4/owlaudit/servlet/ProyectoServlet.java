package com.gr4.owlaudit.servlet;

import java.io.IOException;

import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.ProyectoService;
import com.gr4.owlaudit.service.ProyectoServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * CU02 - Registrar proyecto académico (rol ESTUDIANTE, validado por AutenticacionFilter).
 */
@WebServlet("/proyecto")
public class ProyectoServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/registrarProyecto.jsp";

    private final ProyectoService proyectoService;
    private final ProyectoDAO proyectoDAO;

    public ProyectoServlet() {
        this(new ProyectoServiceImpl(), new ProyectoDAOImpl());
    }

    ProyectoServlet(ProyectoService proyectoService, ProyectoDAO proyectoDAO) {
        this.proyectoService = proyectoService;
        this.proyectoDAO = proyectoDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
            recargarProyectos(request, usuario);
        } catch (Exception e) {
            System.err.println("[ProyectoServlet] Error al listar proyectos: " + e.getMessage());
        }
        // Redirige a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configura la codificación de caracteres para acentos y caracteres especiales
        request.setCharacterEncoding("UTF-8");
        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        if ("actualizar".equals(accion)) {
            procesarActualizacion(request, response);
            return;
        }

        // 1. Obtención de parámetros del formulario HTML y usuario de sesión
        String nombre = request.getParameter("nombre");
        String urlGithub = request.getParameter("urlGithub");
        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);

        // 2. Instanciación del DTO según el Diagrama de Clases
        NuevoProyectoDTO dto = new NuevoProyectoDTO(nombre, urlGithub, usuario != null ? usuario.getId() : null);
        request.setAttribute("proyectoDTO", dto);

        // 3. Delegación a la capa de servicio (validación del enlace + persistencia)
        try {
            proyectoService.solicitarRegistroDeNuevoProyecto(dto);
            request.setAttribute("mensajeExito", "Proyecto '" + dto.getNombre() + "' registrado correctamente.");

            recargarProyectos(request, usuario);
            @SuppressWarnings("unchecked")
            List<Proyecto> proyectos = (List<Proyecto>) request.getAttribute("proyectos");
            if (proyectos != null && !proyectos.isEmpty() && urlGithub != null) {
                Proyecto proyectoRegistrado = proyectos.stream()
                        .filter(p -> urlGithub.trim().equalsIgnoreCase(p.getUrlGithub() != null ? p.getUrlGithub().trim() : ""))
                        .reduce((first, second) -> second)
                        .orElse(null);
                request.setAttribute("proyectoRegistrado", proyectoRegistrado);
            }
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
            recargarProyectos(request, usuario);
        } catch (RuntimeException e) {
            System.err.println("[ProyectoServlet] Error técnico: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al registrar el proyecto. Intente nuevamente.");
            recargarProyectos(request, usuario);
        }

        // 4. Redirección a la vista protegida en WEB-INF/views/
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long proyectoId = extraerIdProyecto(request);
        try {
            proyectoService.eliminarProyecto(proyectoId, usuario.getId());
            request.setAttribute("mensajeExito", "Proyecto eliminado exitosamente y orden secuencial actualizado.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (Exception e) {
            System.err.println("[ProyectoServlet] Error al eliminar proyecto: " + e.getMessage());
            request.setAttribute("mensajeError", "Error al eliminar el proyecto: " + e.getMessage());
        }
        recargarProyectos(request, usuario);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private void procesarActualizacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Long proyectoId = extraerIdProyecto(request);
        String nombre = request.getParameter("nombre");
        String urlGithub = request.getParameter("urlGithub");

        try {
            NuevoProyectoDTO dto = new NuevoProyectoDTO(nombre, urlGithub, usuario.getId());
            proyectoService.actualizarProyecto(proyectoId, dto, usuario.getId());
            request.setAttribute("mensajeExito", "Proyecto '" + dto.getNombre() + "' actualizado exitosamente.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (Exception e) {
            System.err.println("[ProyectoServlet] Error al actualizar proyecto: " + e.getMessage());
            request.setAttribute("mensajeError", "Error al actualizar el proyecto: " + e.getMessage());
        }
        recargarProyectos(request, usuario);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods).
     * Reemplaza las variables temporales idStr dispersas en los métodos de acción
     * por un método de consulta reutilizable y robusto.
     */
    private Long extraerIdProyecto(HttpServletRequest request) {
        String idStr = request.getParameter("idProyecto");
        if (idStr == null || idStr.isBlank()) {
            idStr = request.getParameter("id");
        }
        if (idStr == null || idStr.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(idStr.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void recargarProyectos(HttpServletRequest request, UsuarioDTO usuario) {
        try {
            List<Proyecto> proyectos = (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE)
                    ? proyectoDAO.listarPorUsuarioId(usuario.getId())
                    : proyectoDAO.listarTodos();
            request.setAttribute("proyectos", proyectos);
        } catch (Exception ignored) {}
    }
}