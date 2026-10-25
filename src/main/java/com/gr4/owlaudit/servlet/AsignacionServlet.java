package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.AsignacionesServiceImpl;
import com.gr4.owlaudit.service.AuditoriaService;
import com.gr4.owlaudit.service.AuditoriaServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * HU-02 · Crear asignaciones para auditorías específicas.
 * HU-03 · Ejecutar auditoría de proyecto bajo una asignación específica.
 * Boundary: Interfaz de Asignaciones / Control de Asignaciones.
 */
@WebServlet("/asignaciones")
public class AsignacionServlet extends HttpServlet {

    private static final String VISTA_CREAR = "/WEB-INF/views/crearAsignacion.jsp";
    private static final String VISTA_DETALLE_ESTUDIANTE = "/WEB-INF/views/DetalleAsignacionJSP.jsp";

    private final AsignacionesService asignacionesService;
    private final ReglaDAO reglaDAO;
    private final AuditoriaService auditoriaService;

    public AsignacionServlet() {
        this(new AsignacionesServiceImpl(), new ReglaDAOImpl(), new AuditoriaServiceImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO) {
        this(asignacionesService, reglaDAO, new AuditoriaServiceImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO,
            AuditoriaService auditoriaService) {
        this.asignacionesService = asignacionesService;
        this.reglaDAO = reglaDAO;
        this.auditoriaService = auditoriaService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idAsignacionParam = request.getParameter("idAsignacion");
        String vistaParam = request.getParameter("vista");

        if (esFlujoEstudianteGet(idAsignacionParam, vistaParam)) {
            procesarGetEstudiante(request, response, idAsignacionParam);
            return;
        }

        cargarDatosVista(request);
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String idAsignacionParam = request.getParameter("idAsignacion");
        if (idAsignacionParam != null) {
            procesarPostAuditoriaEstudiante(request, response, idAsignacionParam);
            return;
        }

        String nombre = request.getParameter("nombre");
        String umbralAprobacionStr = request.getParameter("umbralAprobacion");
        String[] reglasIdsParam = request.getParameterValues("reglasIds");

        Double umbralAprobacion;
        try {
            if (umbralAprobacionStr == null || umbralAprobacionStr.trim().isEmpty()) {
                throw new NumberFormatException("El umbral no puede ser vacío");
            }
            umbralAprobacion = Double.parseDouble(umbralAprobacionStr.trim());
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "El umbral de aprobación debe ser un número válido.");
            cargarDatosVista(request);
            request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
            return;
        }

        List<Long> reglasIds = parsearReglasIds(reglasIdsParam);
        NuevaAsignacionDTO dto = new NuevaAsignacionDTO(nombre, umbralAprobacion, reglasIds);

        try {
            final boolean fueAsignacionExitosa = asignacionesService != null
                    && asignacionesService.crearAsignacion(dto) != null;

            if (fueAsignacionExitosa) {
                request.setAttribute("mensajeExito", "Asignación creada correctamente.");
            } else {
                request.setAttribute("mensajeError", "No se pudo crear la asignación.");
            }
        } catch (IllegalArgumentException e) {
            request.setAttribute("mensajeError", e.getMessage());
        }

        cargarDatosVista(request);
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    private boolean esFlujoEstudianteGet(String idAsignacionParam, String vistaParam) {
        return idAsignacionParam != null || "estudiante".equalsIgnoreCase(vistaParam);
    }

    private void procesarGetEstudiante(HttpServletRequest request, HttpServletResponse response,
            String idAsignacionParam)
            throws ServletException, IOException {
        cargarAsignacionesDisponibles(request);

        if (idAsignacionParam != null && !idAsignacionParam.trim().isEmpty()) {
            try {
                Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
                ResumenAsignacionDTO resumen = asignacionesService.generarResumenAsignacion(idAsignacion);
                request.setAttribute("resumenAsignacion", resumen);
            } catch (NumberFormatException e) {
                request.setAttribute("mensajeError", "El identificador de la asignación no es válido.");
            } catch (ExcepcionNegocio | IllegalArgumentException e) {
                request.setAttribute("mensajeError", e.getMessage());
            }
        } else if (idAsignacionParam != null) {
            request.setAttribute("mensajeError", "El identificador de la asignación no puede estar vacío.");
        }

        request.getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE).forward(request, response);
    }

    private void procesarPostAuditoriaEstudiante(HttpServletRequest request, HttpServletResponse response,
            String idAsignacionParam)
            throws ServletException, IOException {
        cargarAsignacionesDisponibles(request);

        try {
            Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
            String idProyectoParam = request.getParameter("idProyecto");
            Long idProyecto = (idProyectoParam != null && !idProyectoParam.trim().isEmpty())
                    ? Long.parseLong(idProyectoParam.trim())
                    : null;

            AuditoriaDTO auditoriaDTO = construirAuditoriaDTO(request, idAsignacion, idProyecto);
            ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(auditoriaDTO);
            request.setAttribute("resultadoAuditoria", resultado);

            if (asignacionesService != null) {
                request.setAttribute("resumenAsignacion", asignacionesService.generarResumenAsignacion(idAsignacion));
            }
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "Los identificadores de asignación o proyecto no son válidos.");
        } catch (ExcepcionNegocio | IllegalArgumentException e) {
            request.setAttribute("mensajeError", e.getMessage());
        }

        request.getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE).forward(request, response);
    }

    private AuditoriaDTO construirAuditoriaDTO(HttpServletRequest request, Long idAsignacion, Long idProyecto) {
        AuditoriaDTO dto = new AuditoriaDTO();
        dto.setIdAsignacion(idAsignacion);
        dto.setProyectoId(idProyecto);

        HttpSession session = request.getSession(false);
        if (session != null) {
            Object usuarioObj = session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);
            if (usuarioObj instanceof UsuarioDTO) {
                dto.setUsuarioId(((UsuarioDTO) usuarioObj).getId());
            }
        }
        return dto;
    }

    private void cargarAsignacionesDisponibles(HttpServletRequest request) {
        try {
            if (asignacionesService != null) {
                List<?> lista = asignacionesService.listarAsignaciones();
                request.setAttribute("asignacionesDisponibles", lista);
                request.setAttribute("asignaciones", lista);
            }
        } catch (Exception e) {
            System.err.println("[AsignacionServlet] Error al cargar asignaciones disponibles: " + e.getMessage());
        }
    }

    private void cargarDatosVista(HttpServletRequest request) {
        try {
            request.setAttribute("reglasVigentes", reglaDAO.consultarReglasDeEvaluacionVigentes());
        } catch (Exception e) {
            System.err.println("[AsignacionServlet] Error al consultar reglas vigentes: " + e.getMessage());
        }

        try {
            if (asignacionesService != null) {
                request.setAttribute("asignaciones", asignacionesService.listarAsignaciones());
            }
        } catch (Exception e) {
            System.err.println("[AsignacionServlet] Error al listar asignaciones: " + e.getMessage());
        }
    }

    private List<Long> parsearReglasIds(String[] reglasIdsParam) {
        List<Long> reglasIds = new ArrayList<>();
        if (reglasIdsParam != null) {
            for (String idStr : reglasIdsParam) {
                try {
                    reglasIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return reglasIds;
    }
}
