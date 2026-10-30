package com.gr4.owlaudit.servlet;

import java.io.IOException;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.service.ReglaService;
import com.gr4.owlaudit.service.ReglaServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * CU01 - Registrar regla de evaluación técnica (rol DOCENTE, validado por AutenticacionFilter).
 * HU-01 - Activar / Desactivar vigencia de reglas.
 */
@WebServlet("/regla")
    public class ReglaServlet extends HttpServlet {
    private static final Set<String> VISTAS_REGISTRO = Set.of("registro", "registrar", "nueva");
    private static final String VISTA = "/WEB-INF/views/registrarRegla.jsp";
    private static final String VISTA_CATALOGO = "/WEB-INF/views/catalogoReglas.jsp";
    private static final String ACCION_CAMBIAR_VIGENCIA = "cambiarVigencia";
    private static final String ACCION_ELIMINAR = "eliminar";
    private static final String ACCION_ACTUALIZAR = "actualizar";
    private static final String ATTR_MENSAJE_EXITO = "mensajeExito";
    private static final String ATTR_MENSAJE_ERROR = "mensajeError";

    private final ReglaService reglaService;
    private final ReglaDAO reglaDAO;

    /** Constructor que usa Tomcat. */
    public ReglaServlet() {
        this(new ReglaServiceImpl(), new ReglaDAOImpl());
    }

    /** Constructor para inyectar mocks en las pruebas. */
    ReglaServlet(ReglaService reglaService, ReglaDAO reglaDAO) {
        this.reglaService = reglaService;
        this.reglaDAO = reglaDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
            request.setAttribute("reglas", reglaDAO.consultarTodasLasReglas());
        } catch (Exception e) {
            System.err.println("[ReglaServlet] Error al consultar reglas: " + e.getMessage());
        }

        String vista = request.getParameter("vista");
        String accion = request.getParameter("accion");
        if ("editar".equalsIgnoreCase(accion) || "editar".equalsIgnoreCase(vista)) {
            String idStr = request.getParameter("id");
            if (idStr != null && !idStr.isBlank()) {
                try {
                    Long id = Long.parseLong(idStr);
                    request.setAttribute("reglaAEditar", reglaService.obtenerReglaPorId(id));
                } catch (Exception ignored) {}
            }
        }

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
        String accion = request.getParameter("accion");

        if (ACCION_CAMBIAR_VIGENCIA.equals(accion)) {
            procesarCambioVigencia(request, response);
            return;
        }

        if (ACCION_ELIMINAR.equals(accion)) {
            procesarEliminacion(request, response);
            return;
        }

        if (ACCION_ACTUALIZAR.equals(accion)) {
            procesarActualizacion(request, response);
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
        request.setAttribute("reglaDTO", dto);

        // 3. Delegación a la capa de servicio (validación de sintaxis, unicidad y persistencia)
        try {
            reglaService.registrarReglaEvaluacionTecnica(dto);
            request.setAttribute(ATTR_MENSAJE_EXITO, "Regla '" + dto.getNombreRepresentativo() + "' registrada con éxito y activa para las auditorías.");
            recargarListasReglas(request);
            request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
            return;
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[ReglaServlet] Error técnico: " + e.getMessage());
            request.setAttribute(ATTR_MENSAJE_ERROR, "Ocurrió un error inesperado al registrar la regla. Intente nuevamente.");
        }

        recargarListasReglas(request);
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    private void procesarEliminacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("idRegla");
        if (idStr == null || idStr.isBlank()) {
            idStr = request.getParameter("id");
        }
        try {
            Long id = Long.parseLong(idStr);
            reglaService.eliminarRegla(id);
            request.setAttribute(ATTR_MENSAJE_EXITO, "Regla eliminada exitosamente.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        } catch (Exception e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, "Error al eliminar la regla: " + e.getMessage());
        }
        recargarListasReglas(request);
        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    private void procesarActualizacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("idRegla");
        if (idStr == null || idStr.isBlank()) {
            idStr = request.getParameter("id");
        }
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

        try {
            Long id = Long.parseLong(idStr);
            NuevaReglaDTO dto = new NuevaReglaDTO(id, tipoMotor, nombreRepresentativo, descripcionDetallada, parametroExacto, nivelSeveridad, ponderacion);
            reglaService.actualizarRegla(id, dto);
            request.setAttribute(ATTR_MENSAJE_EXITO, "Regla '" + dto.getNombreRepresentativo() + "' actualizada con éxito.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        } catch (Exception e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, "Error al actualizar la regla: " + e.getMessage());
        }
        recargarListasReglas(request);
        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    private void recargarListasReglas(HttpServletRequest request) {
        try {
            request.setAttribute("reglas", reglaDAO.consultarTodasLasReglas());
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
        } catch (Exception ignored) {}
    }

    /**
     * HU-01: Procesa la solicitud de cambio de vigencia de una regla técnica.
     */
    private void procesarCambioVigencia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombreRegla = request.getParameter("nombreRegla");
        EstadoVigencia nuevoEstado = EstadoVigencia.valueOf(request.getParameter("nuevoEstado"));

        boolean actualizado = reglaService.cambiarEstadoVigenciaDeUnaRegla(nombreRegla, nuevoEstado);

        if (!actualizado) {
            request.setAttribute(ATTR_MENSAJE_ERROR, "La regla ya se encuentra en estado " + obtenerDescripcionEstado(nuevoEstado) + ".");
        } else {
            request.setAttribute(ATTR_MENSAJE_EXITO, nuevoEstado.construirMensajeExito());
        }

        try {
            request.setAttribute("reglas", reglaDAO.consultarTodasLasReglas());
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
        } catch (Exception ignored) {}

        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query.
     * Reemplaza la variable temporal estadoTexto de procesarCambioVigencia
     * por un método de consulta reutilizable.
     */
    private String obtenerDescripcionEstado(EstadoVigencia estado) {
        return (estado == EstadoVigencia.NO_VIGENTE) ? "no vigente" : "vigente";
    }
}