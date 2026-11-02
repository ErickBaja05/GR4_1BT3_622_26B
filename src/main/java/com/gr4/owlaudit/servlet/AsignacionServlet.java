package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.RolEnum;
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
 * HU-02 · Crear y gestionar asignaciones para auditorías específicas (CRUD Completo Docente).
 * HU-03 · Ejecutar auditoría de proyecto bajo una asignación específica (Estudiante).
 */
@WebServlet({"/asignaciones", "/asignacion"})
public class AsignacionServlet extends HttpServlet {

    private static final String VISTA_CREAR = "/WEB-INF/views/crearAsignacion.jsp";
    private static final String VISTA_DETALLE_ESTUDIANTE = "/WEB-INF/views/DetalleAsignacionJSP.jsp";

    private final AsignacionesService asignacionesService;
    private final ReglaDAO reglaDAO;
    private final AuditoriaService auditoriaService;
    private final ProyectoDAO proyectoDAO;
    private final AuditoriaDAO auditoriaDAO;

    public AsignacionServlet() {
        this(new AsignacionesServiceImpl(), new ReglaDAOImpl(), new AuditoriaServiceImpl(), new ProyectoDAOImpl(), new AuditoriaDAOImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO) {
        this(asignacionesService, reglaDAO, new AuditoriaServiceImpl(), new ProyectoDAOImpl(), new AuditoriaDAOImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO,
            AuditoriaService auditoriaService) {
        this(asignacionesService, reglaDAO, auditoriaService, new ProyectoDAOImpl(), new AuditoriaDAOImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO,
            AuditoriaService auditoriaService, ProyectoDAO proyectoDAO) {
        this(asignacionesService, reglaDAO, auditoriaService, proyectoDAO, new AuditoriaDAOImpl());
    }

    public AsignacionServlet(AsignacionesService asignacionesService, ReglaDAO reglaDAO,
            AuditoriaService auditoriaService, ProyectoDAO proyectoDAO, AuditoriaDAO auditoriaDAO) {
        this.asignacionesService = asignacionesService;
        this.reglaDAO = reglaDAO;
        this.auditoriaService = auditoriaService;
        this.proyectoDAO = proyectoDAO;
        this.auditoriaDAO = auditoriaDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idAsignacionParam = request.getParameter("idAsignacion");
        String vistaParam = request.getParameter("vista");

        if (esFlujoEstudianteGet(request, idAsignacionParam, vistaParam)) {
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

        String accion = request.getParameter("accion");
        if ("actualizar".equalsIgnoreCase(accion)) {
            procesarActualizarAsignacion(request, response);
            return;
        }
        if ("eliminar".equalsIgnoreCase(accion)) {
            procesarEliminarAsignacion(request, response);
            return;
        }

        String idAsignacionParam = request.getParameter("idAsignacion");
        if (idAsignacionParam != null && request.getParameter("idProyecto") != null) {
            procesarPostAuditoriaEstudiante(request, response, idAsignacionParam);
            return;
        }

        try {
            NuevaAsignacionDTO dto = obtenerNuevaAsignacionDTO(request);

            if (asignacionesService != null && asignacionesService.crearAsignacion(dto) != null) {
                request.setAttribute("mensajeExito", "Asignación creada correctamente.");
            } else {
                request.setAttribute("mensajeError", "No se pudo crear la asignación.");
            }
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "El umbral de aprobación debe ser un número válido.");
        } catch (IllegalArgumentException | ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        }

        cargarDatosVista(request);
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    private void procesarActualizarAsignacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idAsignacionStr = request.getParameter("idAsignacion");
        try {
            Long idAsignacion = Long.parseLong(idAsignacionStr.trim());
            NuevaAsignacionDTO dto = obtenerNuevaAsignacionDTO(request);
            asignacionesService.actualizarAsignacion(idAsignacion, dto);
            request.setAttribute("mensajeExito", "Asignación '" + dto.getNombre() + "' actualizada exitosamente. Las auditorías realizadas han sido revalorizadas automáticamente.");
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "El identificador o umbral de la asignación no es válido.");
        } catch (IllegalArgumentException | ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (Exception e) {
            request.setAttribute("mensajeError", "Error al actualizar la asignación: " + e.getMessage());
        }
        cargarDatosVista(request);
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    private void procesarEliminarAsignacion(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idAsignacionStr = request.getParameter("idAsignacion");
        try {
            Long idAsignacion = Long.parseLong(idAsignacionStr.trim());
            asignacionesService.eliminarAsignacion(idAsignacion);
            request.setAttribute("mensajeExito", "Asignación eliminada exitosamente en cascada y numeración secuencial actualizada.");
        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "Identificador de asignación no válido.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
        } catch (Exception e) {
            request.setAttribute("mensajeError", "Error al eliminar la asignación: " + e.getMessage());
        }
        cargarDatosVista(request);
        request.getRequestDispatcher(VISTA_CREAR).forward(request, response);
    }

    // --- QUERY METHODS (Replace Temp with Query) ---

    private NuevaAsignacionDTO obtenerNuevaAsignacionDTO(HttpServletRequest request) {
        String nombre = request.getParameter("nombre");
        Double umbralAprobacion = obtenerUmbralAprobacion(request);
        List<Long> reglasIds = parsearReglasIds(request.getParameterValues("reglasIds"));

        return new NuevaAsignacionDTO(nombre, umbralAprobacion, reglasIds);
    }

    private Double obtenerUmbralAprobacion(HttpServletRequest request) {
        String umbralAprobacionStr = request.getParameter("umbralAprobacion");
        if (umbralAprobacionStr == null || umbralAprobacionStr.trim().isEmpty()) {
            throw new NumberFormatException("El umbral no puede ser vacío");
        }
        return Double.parseDouble(umbralAprobacionStr.trim());
    }

    private Long obtenerUsuarioIdEnSesion(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object usuarioObj = session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);
            if (usuarioObj instanceof UsuarioDTO usuario) {
                return usuario.getId();
            }
        }
        return null;
    }

    // --- MÉTODOS DE SOPORTE ---

    private boolean esFlujoEstudianteGet(HttpServletRequest request, String idAsignacionParam, String vistaParam) {
        if (idAsignacionParam != null && !idAsignacionParam.trim().isEmpty()) {
            return true;
        }
        if ("estudiante".equalsIgnoreCase(vistaParam)) {
            return true;
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) instanceof UsuarioDTO usuario) {
            return usuario.getRol() == RolEnum.ESTUDIANTE;
        }
        return false;
    }

    private void procesarGetEstudiante(HttpServletRequest request, HttpServletResponse response,
            String idAsignacionParam)
            throws ServletException, IOException {
        cargarAsignacionesDisponibles(request);

        Long usuarioId = obtenerUsuarioIdEnSesion(request);

        if (idAsignacionParam != null && !idAsignacionParam.trim().isEmpty()) {
            try {
                Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
                ResumenAsignacionDTO resumen = (usuarioId != null)
                        ? asignacionesService.generarResumenAsignacion(idAsignacion, usuarioId)
                        : asignacionesService.generarResumenAsignacion(idAsignacion);
                request.setAttribute("resumenAsignacion", resumen);

                if (auditoriaDAO != null) {
                    List<Auditoria> historial = auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(idAsignacion, usuarioId);
                    request.setAttribute("historialAuditoriasAsignacion", historial);
                }

                String detalleAuditoriaIdParam = request.getParameter("detalleAuditoriaId");
                if (detalleAuditoriaIdParam != null && !detalleAuditoriaIdParam.trim().isEmpty() && auditoriaService != null) {
                    Long detalleId = Long.parseLong(detalleAuditoriaIdParam.trim());
                    ResultadoDTO resultado = auditoriaService.obtenerDetalleAuditoria(detalleId);
                    request.setAttribute("resultadoAuditoria", resultado);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("mensajeError", "El identificador de la asignación o auditoría no es válido.");
            } catch (ExcepcionNegocio | IllegalArgumentException e) {
                request.setAttribute("mensajeError", e.getMessage());
            }
        }

        request.getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE).forward(request, response);
    }

    private void procesarPostAuditoriaEstudiante(HttpServletRequest request, HttpServletResponse response,
            String idAsignacionParam)
            throws ServletException, IOException {
        cargarAsignacionesDisponibles(request);
        Long usuarioId = obtenerUsuarioIdEnSesion(request);

        try {
            Long idAsignacion = Long.parseLong(idAsignacionParam.trim());
            if (asignacionesService != null) {
                request.setAttribute("resumenAsignacion", asignacionesService.generarResumenAsignacion(idAsignacion, usuarioId));
            }

            String idProyectoParam = request.getParameter("idProyecto");
            Long idProyecto = (idProyectoParam != null && !idProyectoParam.trim().isEmpty())
                    ? Long.parseLong(idProyectoParam.trim())
                    : null;

            // Validación estricta de pertenencia para estudiante
            HttpSession session = request.getSession(false);
            UsuarioDTO usuarioSesion = (session != null) ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) : null;
            if (usuarioSesion != null && usuarioSesion.getRol() == RolEnum.ESTUDIANTE && idProyecto != null) {
                List<Proyecto> misProyectos = proyectoDAO.listarPorUsuarioId(usuarioSesion.getId());
                boolean pertenece = misProyectos != null && misProyectos.stream()
                        .anyMatch(p -> p.getId().equals(idProyecto) || (p.getIdEstudiante() != null && p.getIdEstudiante().equals(idProyecto)));
                if (!pertenece) {
                    request.setAttribute("mensajeError", "No tiene permisos para auditar un proyecto que no le pertenece.");
                    request.getRequestDispatcher(VISTA_DETALLE_ESTUDIANTE).forward(request, response);
                    return;
                }
            }

            AuditoriaDTO auditoriaDTO = construirAuditoriaDTO(request, idAsignacion, idProyecto);
            ResultadoDTO resultado = auditoriaService.ejecutarAuditoriaDeAsignacion(auditoriaDTO);
            request.setAttribute("resultadoAuditoria", resultado);

            if (auditoriaDAO != null) {
                List<Auditoria> historial = auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(idAsignacion, usuarioId);
                request.setAttribute("historialAuditoriasAsignacion", historial);
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
        dto.setUsuarioId(obtenerUsuarioIdEnSesion(request));
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
