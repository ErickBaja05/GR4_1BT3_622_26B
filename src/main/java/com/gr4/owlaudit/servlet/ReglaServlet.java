package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.Set;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.ReglaService;
import com.gr4.owlaudit.service.ReglaServiceImpl;

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
    private static final String ACCION_CAMBIAR_VIGENCIA = "cambiarVigencia";
    private static final String ATTR_MENSAJE_EXITO = "mensajeExito";
    private static final String ATTR_MENSAJE_ERROR = "mensajeError";

    private final ReglaService reglaService;
    private final ReglaDAO reglaDAO;
    private final CursoDAO cursoDAO;

    /** Constructor que usa Tomcat. */
    public ReglaServlet() {
        this(new ReglaServiceImpl(), new ReglaDAOImpl(), new CursoDAOImpl());
    }

    /** Constructor para inyectar mocks en las pruebas. */
    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO) {
        this(reglaService, reglaDAO, null);
    }

    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO, CursoDAO cursoDAO) {
        this.reglaService = reglaService;
        this.reglaDAO = reglaDAO;
        this.cursoDAO = cursoDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        Long idCurso = parsearIdCurso(request.getParameter("idCurso"));
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
        Long idCurso = parsearIdCurso(request.getParameter("idCurso"));
        if (idCurso == null) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinCurso");
            return;
        }

        if (!validarTitularidadDocente(request, response, idCurso)) {
            return;
        }

        if (ACCION_CAMBIAR_VIGENCIA.equals(request.getParameter("accion"))) {
            procesarCambioVigencia(request, response, idCurso);
            return;
        }

        // 1. Lectura de parámetros desde el formulario
        String tipoMotor = request.getParameter("tipoMotor");
        String nombreRepresentativo = request.getParameter("nombreRepresentativo");
        String descripcionDetallada = request.getParameter("descripcionDetallada");
        String parametroExacto = request.getParameter("parametroExacto");
        String nivelSeveridad = request.getParameter("nivelSeveridad");
        
        int ponderacion = 0;
        try {
            ponderacion = Integer.parseInt(request.getParameter("ponderacion"));
        } catch (NumberFormatException e) {
            ponderacion = 0;
        }

        // 2. Instanciación del DTO según clases.puml
        NuevaReglaDTO dto = new NuevaReglaDTO(
            tipoMotor, 
            nombreRepresentativo, 
            descripcionDetallada, 
            parametroExacto, 
            nivelSeveridad, 
            ponderacion
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
     * Valida que el docente autenticado en sesión sea el titular del curso.
     * Retorna true si es válido o si no hay sesión/cursoDAO en pruebas unitarias aisladas.
     */
    private boolean validarTitularidadDocente(HttpServletRequest request, HttpServletResponse response, Long idCurso) 
            throws IOException {
        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null) ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) : null;
        if (usuario != null && usuario.getRol() == RolEnum.DOCENTE && cursoDAO != null) {
            Curso curso = cursoDAO.buscarPorId(idCurso);
            if (curso == null || curso.getDocente() == null || !usuario.getId().equals(curso.getDocente().getId())) {
                response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
                return false;
            }
        }
        return true;
    }

    /**
     * HU-01: Procesa la solicitud de cambio de vigencia de una regla técnica dentro de un curso.
     */
    private void procesarCambioVigencia(HttpServletRequest request, HttpServletResponse response, Long idCurso)
            throws ServletException, IOException {
        String nombreRegla = request.getParameter("nombreRegla");
        EstadoVigencia nuevoEstado = EstadoVigencia.valueOf(request.getParameter("nuevoEstado"));

        boolean actualizado = reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, nuevoEstado);

        if (!actualizado) {
            request.setAttribute(ATTR_MENSAJE_ERROR, "La regla ya se encuentra en estado " + obtenerDescripcionEstado(nuevoEstado) + ".");
        } else {
            request.setAttribute(ATTR_MENSAJE_EXITO, nuevoEstado.construirMensajeExito());
        }

        cargarReglasEnRequest(request, idCurso);
        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    private Long parsearIdCurso(String idCursoStr) {
        if (idCursoStr == null || idCursoStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(idCursoStr.trim());
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

    /**
     * Refactorización Martin Fowler: Replace Temp with Query.
     */
    private String obtenerDescripcionEstado(EstadoVigencia estado) {
        return (estado == EstadoVigencia.NO_VIGENTE) ? "no vigente" : "vigente";
    }
}