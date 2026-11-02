package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.AsignacionesServiceImpl;
import com.gr4.owlaudit.service.EvolucionService;
import com.gr4.owlaudit.service.EvolucionServiceImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * CU04 - Consultar evolución de la calidad (roles ESTUDIANTE y DOCENTE).
 */
@WebServlet("/evolucion")
public class EvolucionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/views/consultarEvolucion.jsp";

    private final EvolucionService evolucionService;
    private final ProyectoDAO proyectoDAO;
    private final AsignacionesService asignacionesService;

    public EvolucionServlet() {
        this(new EvolucionServiceImpl(), new ProyectoDAOImpl(), new AsignacionesServiceImpl());
    }

    public EvolucionServlet(EvolucionService evolucionService) {
        this(evolucionService, new ProyectoDAOImpl(), new AsignacionesServiceImpl());
    }

    public EvolucionServlet(EvolucionService evolucionService, ProyectoDAO proyectoDAO) {
        this(evolucionService, proyectoDAO, new AsignacionesServiceImpl());
    }

    public EvolucionServlet(EvolucionService evolucionService, ProyectoDAO proyectoDAO, AsignacionesService asignacionesService) {
        this.evolucionService = evolucionService;
        this.proyectoDAO = proyectoDAO;
        this.asignacionesService = asignacionesService;
    }

    // 1. Carga del Historial (doGet) según secuencia4.puml
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        List<Proyecto> listaProyectos = null;
        try {
            listaProyectos = (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE)
                    ? proyectoDAO.listarPorUsuarioId(usuario.getId())
                    : proyectoDAO.listarTodos();
            request.setAttribute("listaProyectos", listaProyectos);
        } catch (Exception e) {
            System.err.println("[EvolucionServlet] Error al listar proyectos: " + e.getMessage());
        }

        try {
            if (asignacionesService != null) {
                request.setAttribute("listaAsignaciones", asignacionesService.listarAsignaciones());
            }
        } catch (Exception e) {
            System.err.println("[EvolucionServlet] Error al listar asignaciones: " + e.getMessage());
        }

        String tipoAuditoria = request.getParameter("tipoAuditoria");
        if (tipoAuditoria == null || tipoAuditoria.isBlank()) {
            tipoAuditoria = "GENERAL";
        }
        request.setAttribute("tipoAuditoria", tipoAuditoria);

        String asignacionIdParam = request.getParameter("asignacionId");
        Long asignacionId = null;
        if (asignacionIdParam != null && !asignacionIdParam.isBlank()) {
            try {
                asignacionId = Long.parseLong(asignacionIdParam.trim());
                request.setAttribute("asignacionId", asignacionId);
            } catch (NumberFormatException ignored) {}
        }

        String proyectoIdParam = request.getParameter("proyectoId");
        if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
            try {
                Long proyectoId = Long.parseLong(proyectoIdParam.trim());
                if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE) {
                    final Long finalPid = proyectoId;
                    Proyecto miProyecto = null;
                    if (listaProyectos != null) {
                        for (Proyecto p : listaProyectos) {
                            if (finalPid.equals(p.getId()) || (p.getIdEstudiante() != null && finalPid.equals(p.getIdEstudiante()))) {
                                miProyecto = p;
                                break;
                            }
                        }
                    }
                    if (miProyecto == null) {
                        request.setAttribute("mensajeError", "No tiene permisos para consultar el proyecto seleccionado.");
                        request.getRequestDispatcher(VISTA).forward(request, response);
                        return;
                    }
                    proyectoId = miProyecto.getId();
                }
                EvolucionDTO dto = new EvolucionDTO(proyectoId);
                dto.setTipoAuditoria(tipoAuditoria);
                dto.setAsignacionId(asignacionId);
                request.setAttribute("evolucionDTO", dto);

                if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) && asignacionId == null) {
                    request.setAttribute("mensajeInfo", "Seleccione una asignación para cargar el historial correspondiente.");
                } else {
                    ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);
                    request.setAttribute("resumenHistorialDTO", resumen);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("mensajeError", "El identificador del proyecto debe ser un número entero válido.");
            } catch (ExcepcionNegocio e) {
                request.setAttribute("mensajeError", e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("[EvolucionServlet] Error técnico al consultar historial: " + e.getMessage());
                request.setAttribute("mensajeError", "Ocurrió un error inesperado al consultar el historial de auditorías.");
            }
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }

    // 2. Comparación de Auditorías (doPost) según secuencia4.puml
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDTO usuario = (UsuarioDTO) request.getSession().getAttribute(LoginServlet.ATRIBUTO_USUARIO);
        List<Proyecto> listaProyectos = null;
        try {
            listaProyectos = (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE)
                    ? proyectoDAO.listarPorUsuarioId(usuario.getId())
                    : proyectoDAO.listarTodos();
            request.setAttribute("listaProyectos", listaProyectos);
        } catch (Exception e) {
            System.err.println("[EvolucionServlet] Error al listar proyectos: " + e.getMessage());
        }

        try {
            if (asignacionesService != null) {
                request.setAttribute("listaAsignaciones", asignacionesService.listarAsignaciones());
            }
        } catch (Exception e) {
            System.err.println("[EvolucionServlet] Error al listar asignaciones: " + e.getMessage());
        }

        request.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        if ("cancelar".equalsIgnoreCase(accion)) {
            request.setAttribute("mensajeInfo", "Consulta cancelada.");
            doGet(request, response);
            return;
        }

        String tipoAuditoria = request.getParameter("tipoAuditoria");
        if (tipoAuditoria == null || tipoAuditoria.isBlank()) {
            tipoAuditoria = "GENERAL";
        }
        request.setAttribute("tipoAuditoria", tipoAuditoria);

        String asignacionIdParam = request.getParameter("asignacionId");
        Long asignacionId = null;
        if (asignacionIdParam != null && !asignacionIdParam.isBlank()) {
            try {
                asignacionId = Long.parseLong(asignacionIdParam.trim());
                request.setAttribute("asignacionId", asignacionId);
            } catch (NumberFormatException ignored) {}
        }

        String proyectoIdParam = request.getParameter("proyectoId");
        String baseIdParam = request.getParameter("baseId");
        String comparadaIdParam = request.getParameter("comparadaId");

        Long proyectoId = null;
        Long baseId = null;
        Long comparadaId = null;

        try {
            if (proyectoIdParam != null && !proyectoIdParam.isBlank()) {
                proyectoId = Long.parseLong(proyectoIdParam.trim());
            }
            if (baseIdParam != null && !baseIdParam.isBlank()) {
                baseId = Long.parseLong(baseIdParam.trim());
            }
            if (comparadaIdParam != null && !comparadaIdParam.isBlank()) {
                comparadaId = Long.parseLong(comparadaIdParam.trim());
            }

            if (proyectoId != null && usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE) {
                final Long finalPid = proyectoId;
                Proyecto miProyecto = null;
                if (listaProyectos != null) {
                    for (Proyecto p : listaProyectos) {
                        if (finalPid.equals(p.getId()) || (p.getIdEstudiante() != null && finalPid.equals(p.getIdEstudiante()))) {
                            miProyecto = p;
                            break;
                        }
                    }
                }
                if (miProyecto == null) {
                    request.setAttribute("mensajeError", "No tiene permisos para consultar el proyecto seleccionado.");
                    request.getRequestDispatcher(VISTA).forward(request, response);
                    return;
                }
                proyectoId = miProyecto.getId();
            }

            EvolucionDTO dto = new EvolucionDTO(proyectoId, baseId, comparadaId);
            dto.setTipoAuditoria(tipoAuditoria);
            dto.setAsignacionId(asignacionId);
            request.setAttribute("evolucionDTO", dto);

            EvolucionFinalDTO evolucionFinal = evolucionService.indicarDosAuditoriasAComparar(dto);
            request.setAttribute("evolucionFinalDTO", evolucionFinal);

            // Mantener el historial cargado para la vista
            if (proyectoId != null) {
                try {
                    ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);
                    request.setAttribute("resumenHistorialDTO", resumen);
                } catch (Exception ignored) {}
            }

        } catch (NumberFormatException e) {
            request.setAttribute("mensajeError", "Los identificadores ingresados no son válidos.");
        } catch (ExcepcionNegocio e) {
            request.setAttribute("mensajeError", e.getMessage());
            // Si hubo error de comparación pero el proyecto es válido, reintentar cargar el historial
            if (proyectoId != null) {
                try {
                    EvolucionDTO dto = new EvolucionDTO(proyectoId);
                    dto.setTipoAuditoria(tipoAuditoria);
                    dto.setAsignacionId(asignacionId);
                    ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);
                    request.setAttribute("resumenHistorialDTO", resumen);
                } catch (Exception ignored) {
                }
            }
        } catch (RuntimeException e) {
            System.err.println("[EvolucionServlet] Error técnico al comparar auditorías: " + e.getMessage());
            request.setAttribute("mensajeError", "Ocurrió un error inesperado al procesar la comparación.");
        }

        request.getRequestDispatcher(VISTA).forward(request, response);
    }
}
