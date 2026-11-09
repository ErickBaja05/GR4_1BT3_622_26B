package com.gr4.owlaudit.servlet;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaResumenDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.UsuarioDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.service.AsignacionesService;
import com.gr4.owlaudit.service.AsignacionesServiceImpl;
import com.gr4.owlaudit.service.AuditoriaService;
import com.gr4.owlaudit.service.AuditoriaServiceImpl;
import com.gr4.owlaudit.service.ValidadorAccesoCurso;

/**
 * EP-02 >> FT-04 >> HU-02: Inspeccionar listado de entregas y resultados de auditoría por el docente.
 */
@WebServlet({"/docente/asignacion/entregas", "/docente/asignacion/detalle"})
public class DetalleAsignacionDocenteServlet extends HttpServlet {

    private static final String VISTA_DETALLE_DOCENTE = "/WEB-INF/views/detalleAsignacionDocente.jsp";

    private final AsignacionesService asignacionesService;
    private final AuditoriaService auditoriaService;
    private final ValidadorAccesoCurso validadorAccesoCurso;

    public DetalleAsignacionDocenteServlet() {
        this(new AsignacionesServiceImpl(), new AuditoriaServiceImpl(), new ValidadorAccesoCurso(new CursoDAOImpl()));
    }

    public DetalleAsignacionDocenteServlet(AsignacionesService asignacionesService,
                                         AuditoriaService auditoriaService,
                                         ValidadorAccesoCurso validadorAccesoCurso) {
        this.asignacionesService = asignacionesService;
        this.auditoriaService = auditoriaService;
        this.validadorAccesoCurso = validadorAccesoCurso;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        UsuarioDTO usuario = (session != null)
                ? (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO)
                : null;

        if (usuario == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Restricción por rol (CA-3): Solo docentes pueden acceder a esta vista
        if (usuario.getRol() != RolEnum.DOCENTE) {
            response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
            return;
        }

        String idAsignacionParam = request.getParameter("idAsignacion");
        if (idAsignacionParam == null || idAsignacionParam.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
            return;
        }

        Long idAsignacion;
        try {
            idAsignacion = Long.parseLong(idAsignacionParam.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
            return;
        }

        Asignacion asignacion = asignacionesService.buscarPorId(idAsignacion);
        if (asignacion == null || asignacion.getCurso() == null) {
            response.sendRedirect(request.getContextPath() + "/curso?error=sinAsignacion");
            return;
        }

        // Restricción por titularidad del curso (CA-3)
        try {
            validadorAccesoCurso.validarTitularidadDocenteCurso(asignacion.getCurso(), usuario.getId());
        } catch (ExcepcionNegocio e) {
            response.sendRedirect(request.getContextPath() + "/curso?error=permisos");
            return;
        }

        // CA-1: Despliegue del listado de entregas
        List<AuditoriaResumenDTO> listaEntregas =
                auditoriaService.consultarEntregasDeAsignacionParaDocente(idAsignacion, usuario.getId());

        // CA-2: Inspección detallada de hallazgos y evidencias individuales
        String idAuditoriaParam = request.getParameter("idAuditoria");
        if (idAuditoriaParam != null && !idAuditoriaParam.isBlank()) {
            try {
                Long idAuditoria = Long.parseLong(idAuditoriaParam.trim());
                ResultadoDTO detalleAuditoria = auditoriaService.obtenerDetalleAuditoria(idAuditoria);
                request.setAttribute("detalleAuditoria", detalleAuditoria);
            } catch (NumberFormatException ignored) {}
        }

        request.setAttribute("asignacion", asignacion);
        request.setAttribute("curso", asignacion.getCurso());
        request.setAttribute("listaEntregas", listaEntregas);

        request.getRequestDispatcher(VISTA_DETALLE_DOCENTE).forward(request, response);
    }
}

