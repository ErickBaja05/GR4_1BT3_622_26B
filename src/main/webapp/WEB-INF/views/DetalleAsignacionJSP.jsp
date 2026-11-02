<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenAsignacionDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO.DetalleResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.model.Asignacion" %>
<%@ page import="com.gr4.owlaudit.model.Auditoria" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.model.Proyecto" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String ctx = request.getContextPath();
    @SuppressWarnings("unchecked")
    List<Asignacion> asignacionesDisponibles = (List<Asignacion>) request.getAttribute("asignacionesDisponibles");
    if (asignacionesDisponibles == null) {
        asignacionesDisponibles = (List<Asignacion>) request.getAttribute("asignaciones");
    }
    ResumenAsignacionDTO resumen = (ResumenAsignacionDTO) request.getAttribute("resumenAsignacion");
    ResultadoDTO resultado = (ResultadoDTO) request.getAttribute("resultadoAuditoria");
    @SuppressWarnings("unchecked")
    List<Auditoria> historialAuditorias = (List<Auditoria>) request.getAttribute("historialAuditoriasAsignacion");
    String mensajeError = (String) request.getAttribute("mensajeError");
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Evaluar Asignación</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">Auditoría por Asignación Académica</h1>
            <p class="card-subtitle">
                Seleccione una asignación para revisar sus reglas técnicas, umbral de aprobación y evaluar uno de sus proyectos registrados.
            </p>

            <% if (mensajeError != null) { %>
                <div class="alert alert-error" style="margin-top: 1rem;">
                    <%= HtmlUtil.escape(mensajeError) %>
                </div>
            <% } %>

            <form method="get" action="<%= ctx %>/asignaciones" style="margin-top: 1rem;">
                <label for="selectAsignacion"><strong>Asignaciones Disponibles:</strong></label>
                <div style="display: flex; gap: 0.5rem; margin-top: 0.5rem;">
                    <select name="idAsignacion" id="selectAsignacion" class="form-control" required>
                        <option value="">-- Seleccione una asignación --</option>
                        <%
                            if (asignacionesDisponibles != null) {
                                for (Asignacion asig : asignacionesDisponibles) {
                                    boolean selected = (resumen != null && resumen.getIdAsignacion() != null
                                            && resumen.getIdAsignacion().equals(asig.getId()));
                        %>
                            <option value="<%= asig.getId() %>" <%= selected ? "selected" : "" %>>
                                <%= HtmlUtil.escape(asig.getNombre()) %> (Umbral: <%= asig.getUmbralAprobacion() %>%)
                            </option>
                        <%
                                }
                            }
                        %>
                    </select>
                    <button type="submit" class="btn btn-secondary">Ver Detalle</button>
                </div>
            </form>
        </div>

        <% if (resumen != null) { %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="section-title" style="margin-top: 0;">
                    Detalle de Asignación: <%= HtmlUtil.escape(resumen.getNombreAsignacion()) %>
                </h2>
                <p style="margin-bottom: 1rem;">
                    <strong>Porcentaje de Umbral de Aprobación:</strong>
                    <span class="badge badge-docente">
                        <%= resumen.getUmbralAprobacion() %>%
                    </span>
                </p>

                <h3>Reglas Asociadas a la Asignación</h3>
                <div class="table-responsive">
                    <table class="table data-table" style="width: 100%;">
                        <thead>
                            <tr>
                                <th>Regla Técnica</th>
                                <th>Severidad</th>
                                <th>Ponderación</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (resumen.getReglasAsociadas() != null && !resumen.getReglasAsociadas().isEmpty()) {
                                    for (Regla regla : resumen.getReglasAsociadas()) {
                            %>
                                <tr>
                                    <td><%= HtmlUtil.escape(regla.getNombreRepresentativo()) %></td>
                                    <td><%= regla.getNivelSeveridad() != null ? regla.getNivelSeveridad() : "-" %></td>
                                    <td><strong><%= regla.getPonderacion() %> pts</strong></td>
                                </tr>
                            <%
                                    }
                                } else {
                            %>
                                <tr>
                                    <td colspan="3">No hay reglas asociadas a esta asignación.</td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <form method="post" action="<%= ctx %>/asignaciones">
                    <input type="hidden" name="idAsignacion" value="<%= resumen.getIdAsignacion() %>">
                    <div style="margin-bottom: 1rem;">
                        <label for="selectProyecto"><strong>Seleccione su Proyecto Registrado:</strong></label>
                        <select name="idProyecto" id="selectProyecto" class="form-control" style="margin-top: 0.5rem;">
                            <option value="">-- Seleccione un proyecto --</option>
                            <%
                                if (resumen.getProyectosEstudiante() != null) {
                                    for (Proyecto proy : resumen.getProyectosEstudiante()) {
                            %>
                                <option value="<%= proy.getId() %>">
                                    <%= HtmlUtil.escape(proy.getNombre()) %> (<%= HtmlUtil.escape(proy.getUrlGithub()) %>)
                                </option>
                            <%
                                    }
                                }
                            %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary" id="btnEjecutarAuditoriaAsignacion">
                        Ejecutar Auditoría de Asignación
                    </button>
                </form>

                <hr style="margin: 2rem 0; border: none; border-top: 1px solid var(--border-color, #e2e8f0);">

                <h3 style="margin-top: 0;">📋 Historial de Auditorías en esta Asignación</h3>
                <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">
                    Consulte sus evaluaciones previas en esta asignación o presione <strong>🔍 Ver Detalle</strong> para inspeccionar los hallazgos y evidencias.
                </p>

                <% if (historialAuditorias != null && !historialAuditorias.isEmpty()) { %>
                    <div class="table-responsive">
                        <table class="table data-table" style="width: 100%;">
                            <thead>
                                <tr>
                                    <th>ID Auditoría</th>
                                    <th>Proyecto</th>
                                    <th>Fecha y Hora</th>
                                    <th>Puntaje</th>
                                    <th>Cumplimiento</th>
                                    <th>Dictamen</th>
                                    <th style="text-align: center;">Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                <%
                                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                                    for (Auditoria aud : historialAuditorias) {
                                        boolean passed = "APROBADA".equalsIgnoreCase(aud.getDictamen());
                                        String proyNom = aud.getProyecto() != null ? aud.getProyecto().getNombre() : "-";
                                        String fechaStr = aud.getFechaHora() != null ? aud.getFechaHora().format(dtf) : "-";
                                %>
                                    <tr>
                                        <td><strong><%= aud.getCodigoVisible() %></strong></td>
                                        <td><%= HtmlUtil.escape(proyNom) %></td>
                                        <td><%= fechaStr %></td>
                                        <td><%= aud.getPuntajeObtenido() %> / <%= aud.getPuntajeMaximo() %> pts</td>
                                        <td><strong><%= aud.getPorcentaje() %>%</strong></td>
                                        <td>
                                            <span class="badge <%= passed ? "badge-estudiante" : "alert-error" %>">
                                                <%= HtmlUtil.escape(aud.getDictamen() != null ? aud.getDictamen() : "-") %>
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <a href="<%= ctx %>/asignaciones?idAsignacion=<%= resumen.getIdAsignacion() %>&detalleAuditoriaId=<%= aud.getId() %>" 
                                               class="btn btn-secondary btn-sm" id="btnDetalleAuditoria_<%= aud.getId() %>">
                                                🔍 Ver Detalle
                                            </a>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } else { %>
                    <p style="color: var(--text-muted); font-style: italic;">
                        Aún no registra auditorías ejecutadas en esta asignación. Seleccione su proyecto arriba y presione "Ejecutar Auditoría de Asignación".
                    </p>
                <% } %>
            </div>
        <% } %>

        <%
            if (resultado != null) {
                boolean aprobada = "APROBADA".equalsIgnoreCase(resultado.getDictamen());
                String idLabel = resultado.getCodigoVisible() != null ? resultado.getCodigoVisible() : (resultado.getIdVisible() != null ? "Auditoría #" + resultado.getIdVisible() : "");
        %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="section-title" style="margin-top: 0;">Resultado de la Evaluación <%= !idLabel.isEmpty() ? "(" + idLabel + ")" : "" %></h2>
                <div style="display: flex; gap: 1.5rem; align-items: center; margin-bottom: 1rem; flex-wrap: wrap;">
                    <div>
                        <strong>Dictamen Final:</strong>
                        <span class="badge <%= aprobada ? "badge-estudiante" : "alert-error" %>" style="font-size: 1rem; padding: 0.4rem 0.8rem;">
                            <%= HtmlUtil.escape(resultado.getDictamen()) %>
                        </span>
                    </div>
                    <div>
                        <strong>Puntaje Obtenido:</strong>
                        <%= resultado.getPuntajeObtenido() %> / <%= resultado.getPuntajeMaximo() %> pts (<%= resultado.getPorcentaje() %>%)
                    </div>
                    <div>
                        <strong>Umbral Requerido:</strong>
                        <%= resultado.getUmbralAprobacion() %>%
                    </div>
                </div>

                <h3>Desglose de Hallazgos y Cumplimiento Regla por Regla</h3>
                <div class="table-responsive">
                    <table class="table data-table" style="width: 100%;">
                        <thead>
                            <tr>
                                <th>Regla</th>
                                <th>Estado</th>
                                <th>Puntaje / Ponderación</th>
                                <th>Severidad</th>
                                <th>Evidencia / Observación</th>
                                <th>Recomendación</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (resultado.getDetalles() != null) {
                                    for (DetalleResultadoDTO det : resultado.getDetalles()) {
                            %>
                                <tr>
                                    <td><%= HtmlUtil.escape(det.getNombreRegla()) %></td>
                                    <td><%= det.isCumple() ? "✅ Cumple" : "❌ Incumple" %></td>
                                    <td><%= det.getPuntosObtenidos() %> / <%= det.getPonderacion() %> pts</td>
                                    <td><%= det.getNivelSeveridad() != null ? det.getNivelSeveridad() : "-" %></td>
                                    <td><%= det.getEvidencia() != null ? HtmlUtil.escape(det.getEvidencia()) : "Sin hallazgos" %></td>
                                    <td><%= det.getRecomendacion() != null ? HtmlUtil.escape(det.getRecomendacion()) : "-" %></td>
                                </tr>
                            <%
                                    }
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </main>

</body>

</html>
