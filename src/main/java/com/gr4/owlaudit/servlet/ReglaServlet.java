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

/**
 * CU01 - Registrar regla de evaluación técnica (rol DOCENTE, validado por AutenticacionFilter).
 * HU-01 - Activar / Desactivar vigencia de reglas.
 */
@WebServlet("/regla")
public class ReglaServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/registrarRegla.jsp";
    private static final String VISTA_CATALOGO = "/WEB-INF/views/catalogoReglas.jsp";
    private static final String ACCION_CAMBIAR_VIGENCIA = "cambiarVigencia";
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
        } catch (Exception e) {
            System.err.println("[ReglaServlet] Error al consultar reglas vigentes: " + e.getMessage());
        }
        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");

        if (ACCION_CAMBIAR_VIGENCIA.equals(request.getParameter("accion"))) {
            procesarCambioVigencia(request, response);
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
        } catch (ExcepcionNegocio e) {
            request.setAttribute(ATTR_MENSAJE_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("[ReglaServlet] Error técnico: " + e.getMessage());
            request.setAttribute(ATTR_MENSAJE_ERROR, "Ocurrió un error inesperado al registrar la regla. Intente nuevamente.");
        }

        try {
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
        } catch (Exception ignored) {}

        request.getRequestDispatcher(VISTA).forward(request, response);
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
            String estadoTexto = (nuevoEstado == EstadoVigencia.NO_VIGENTE) ? "no vigente" : "vigente";
            request.setAttribute(ATTR_MENSAJE_ERROR, "La regla ya se encuentra en estado " + estadoTexto + ".");
        } else {
            request.setAttribute(ATTR_MENSAJE_EXITO, construirMensajeExito(nuevoEstado));
        }

        request.getRequestDispatcher(VISTA_CATALOGO).forward(request, response);
    }

    private String construirMensajeExito(EstadoVigencia nuevoEstado) {
        return (nuevoEstado == EstadoVigencia.NO_VIGENTE)
                ? "La regla fue desactivada para nuevas evaluaciones."
                : "La regla fue reactivada y vuelve a estar disponible para su aplicación.";
    }
}