package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.Set;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.ModificacionReglaDTO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.ReglaService;
import com.gr4.owlaudit.service.ReglaServiceImpl;
import com.gr4.owlaudit.service.ValidadorAccesoCurso;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * CU01 - Registrar regla de evaluación técnica (rol DOCENTE, validado por AutenticacionFilter).
 * HU-01 - Crear reglas para el curso y aislamiento por curso.
 */
@WebServlet("/regla")
public class ReglaServlet extends HttpServlet {
    private static final Set<String> VISTAS_REGISTRO = Set.of("registro", "registrar", "nueva");
    private static final String VISTA = "/WEB-INF/views/registrarRegla.jsp";
    private static final String VISTA_CATALOGO = "/WEB-INF/views/catalogoReglas.jsp";
    private static final String ACCION_MODIFICAR_REGLA = "modificarRegla";
    private static final String MENSAJE_MODIFICACION_EXITOSA = "Descripción actualizada exitosamente";
    private static final String MENSAJE_VIGENCIA_INVALIDA = "El estado de vigencia seleccionado no es válido.";
    private static final String ATTR_MENSAJE_EXITO = "mensajeExito";
    private static final String ATTR_MENSAJE_ERROR = "mensajeError";

    private final ReglaService reglaService;
    private final ReglaDAO reglaDAO;
    private final CursoDAO cursoDAO;
    private final ValidadorAccesoCurso validadorAccesoCurso;

    /** Constructor que usa Tomcat. */
    public ReglaServlet() {
        this(new ReglaServiceImpl(), new ReglaDAOImpl(), new CursoDAOImpl());
    }

    /** Constructor para inyectar mocks en las pruebas. */
    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO) {
        this(reglaService, reglaDAO, null, null);
    }

    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO, CursoDAO cursoDAO) {
        this(reglaService, reglaDAO, cursoDAO, (cursoDAO != null) ? new ValidadorAccesoCurso(cursoDAO) : null);
    }

    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO, CursoDAO cursoDAO, ValidadorAccesoCurso validadorAccesoCurso) {
        this.reglaService = reglaService;
        this.reglaDAO = reglaDAO;
        this.cursoDAO = cursoDAO;
        this.validadorAccesoCurso = validadorAccesoCurso;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null) ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) : null;
        if (usuario != null && (validadorAccesoCurso != null ? validadorAccesoCurso.esEstudiante(usuario) : usuario.getRol() == RolEnum.ESTUDIANTE)) {
            response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
            return;
        }

        Long idCurso = parsearId(request.getParameter("idCurso"));
        if (idCurso == null) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinCurso");
            return;
        }

        if (!validarTitularidadDocente(request, response, idCurso)) {
            return;
        }

        cargarReglasEnRequest(request, idCurso);

        String vista = request.getParameter("vista");
        String accion = request.getParameter("accion");
        // Substitute Algorithm: sustitución de la cadena de || por consulta en VISTAS_REGISTRO
        boolean esVistaRegistro = (vista != null && VISTAS_REGISTRO.contains(vista.toLowerCase()))
                || (accion != null && VISTAS_REGISTRO.contains(accion.toLowerCase()));

        if (esVistaRegistro) {
            request.getRequestDispatcher(VISTA).forward(request, response);
            return;
        }

        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null) ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) : null;
        if (usuario != null && (validadorAccesoCurso != null ? validadorAccesoCurso.esEstudiante(usuario) : usuario.getRol() == RolEnum.ESTUDIANTE)) {
            response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
            return;
        }

        Long idCurso = parsearId(request.getParameter("idCurso"));
        if (idCurso == null) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinCurso");
            return;
        }

        if (!validarTitularidadDocente(request, response, idCurso)) {
            return;
        }

        if (ACCION_MODIFICAR_REGLA.equals(request.getParameter("accion"))) {
            procesarModificacionRegla(request, response, idCurso, usuario);
            return;
        }

        // 1. Lectura de parámetros desde el formulario (exclusivamente hasta parámetro exacto)
        String tipoMotor = request.getParameter("tipoMotor");
        String nombreRepresentativo = request.getParameter("nombreRepresentativo");
        String descripcionDetallada = request.getParameter("descripcionDetallada");
        String parametroExacto = request.getParameter("parametroExacto");

        // 2. Instanciación del DTO según el nuevo flujo (sin severidad ni ponderación)
        NuevaReglaDTO dto = new NuevaReglaDTO(
            tipoMotor, 
            nombreRepresentativo, 
            descripcionDetallada, 
            parametroExacto
        );
        dto.setIdCurso(idCurso);
        request.setAttribute("reglaDTO", dto);

        // 3. Delegación a la capa de servicio (validación de sintaxis, unicidad y persistencia)
        try {
            reglaService.registrarReglaEvaluacionTecnica(dto);
            request.setAttribute(ATTR_MENSAJE_EXITO, "Regla creada exitosamente");
            cargarReglasEnRequest(request, idCurso);
            request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
            return;
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[ReglaServlet] Error técnico: " + e.getMessage());
            request.setAttribute(ATTR_MENSAJE_ERROR, "Ocurrió un error inesperado al registrar la regla. Intente nuevamente.");
        }

        cargarReglasEnRequest(request, idCurso);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    /**
     * Valida que el docente autenticado en sesión sea el titular del curso mediante ValidadorAccesoCurso.
     * Retorna true si es válido o si no hay sesión/cursoDAO en pruebas unitarias aisladas.
     */
    private boolean validarTitularidadDocente(HttpServletRequest request, HttpServletResponse response, Long idCurso) 
            throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null) ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) : null;
        if (usuario == null) {
            return true;
        }

        if (validadorAccesoCurso != null) {
            if (!validadorAccesoCurso.esDocente(usuario) || !validadorAccesoCurso.puedeAccederACatalogoReglas(usuario, idCurso)) {
                response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                return false;
            }
        } else if (cursoDAO != null) {
            if (usuario.getRol() != RolEnum.DOCENTE) {
                response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                return false;
            }
            Curso curso = cursoDAO.buscarPorId(idCurso);
            if (curso == null || curso.getDocente() == null || !usuario.getId().equals(curso.getDocente().getId())) {
                response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                return false;
            }
        }
        return true;
    }

    /**
     * HU-02: actualiza únicamente la descripción y la vigencia de una regla del curso.
     * Nombre y motor no se leen de la petición, por lo que no pueden alterarse.
     */
    private void procesarModificacionRegla(HttpServletRequest request, HttpServletResponse response, Long idCurso,
            UsuarioDTO usuario) throws ServletException, IOException {
        try {
            reglaService.modificarDescripcionYVigencia(new ModificacionReglaDTO(
                    parsearId(request.getParameter("idRegla")),
                    idCurso,
                    (usuario != null) ? usuario.getId() : null,
                    request.getParameter("descripcionDetallada"),
                    EstadoVigencia.valueOf(String.valueOf(request.getParameter("vigencia")).trim())));
            request.setAttribute(ATTR_MENSAJE_EXITO, MENSAJE_MODIFICACION_EXITOSA);
        } catch (IllegalArgumentException e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, MENSAJE_VIGENCIA_INVALIDA);
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        }

        cargarReglasEnRequest(request, idCurso);
        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    private Long parsearId(String id) {
        if (id == null || id.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(id.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void cargarReglasEnRequest(HttpServletRequest request, Long idCurso) {
        if (idCurso == null) {
            return;
        }
        try {
            request.setAttribute("idCurso", idCurso);
            request.setAttribute("reglas", reglaService.listarReglasPorCurso(idCurso));
            request.setAttribute("reglasVigentes", reglaService.listarReglasVigentesPorCurso(idCurso));
        } catch (Exception e) {
            System.err.println("[ReglaServlet] Error al consultar reglas por curso: " + e.getMessage());
        }
    }
}