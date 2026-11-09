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
    ResumenAsignacionDTO resumen = (ResumenAsignacionDTO) request.getAttribute("resumenAsignacion");
    ResultadoDTO resultado = (ResultadoDTO) request.getAttribute("resultadoAuditoria");
    @SuppressWarnings("unchecked")
    List<Auditoria> historialAuditorias = (List<Auditoria>) request.getAttribute("historialAuditoriasAsignacion");
    com.gr4.owlaudit.dto.DeudaTecnicaDTO deudaTecnica = (com.gr4.owlaudit.dto.DeudaTecnicaDTO) request.getAttribute("deudaTecnica");
    String mensajeError = (String) request.getAttribute("mensajeError");
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Entregar Asignación</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <div style="margin-bottom: 0.5rem;">
                        <a href="<%= ctx %>/curso" class="btn btn-secondary btn-sm" style="text-decoration: none;">&larr; Volver a Mis Cursos</a>
                    </div>
                    <h1 class="card-title">
                        Entrega de Asignación: <%= (resumen != null && resumen.getNombreAsignacion() != null) ? HtmlUtil.escape(resumen.getNombreAsignacion()) : "Asignación Académica" %>
                    </h1>
                    <p class="card-subtitle">
                        Revise las reglas técnicas requeridas, proporcione la URL de su repositorio GitHub y confirme el envío para obtener su calificación y dictamen de calidad.
                    </p>
                </div>
            </div>

            <% if (mensajeError != null && !mensajeError.isBlank()) { %>
                <div class="alert alert-error" style="margin-top: 1rem;">
                    <%= HtmlUtil.escape(mensajeError) %>
                </div>
            <% } %>
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

                <form method="post" action="<%= ctx %>/asignaciones" id="formEntregaAsignacion">
                    <input type="hidden" name="idAsignacion" value="<%= resumen.getIdAsignacion() %>">

                    <div style="margin-bottom: 1.25rem;">
                        <label for="inputUrlGithub" class="form-label" style="font-weight: 600;">
                            <strong>URL del Repositorio GitHub de la Entrega *:</strong>
                        </label>
                        <input type="url" name="urlGithub" id="inputUrlGithub" class="form-control" 
                               placeholder="https://github.com/usuario/mi-repositorio-academico" 
                               style="margin-top: 0.5rem;" required>
                        <small class="text-muted" style="display: block; margin-top: 0.35rem;">
                            Ingrese la URL pública del repositorio de GitHub que contiene el código a evaluar.
                        </small>
                    </div>

                    <% if (resumen.getProyectosEstudiante() != null && !resumen.getProyectosEstudiante().isEmpty()) { %>
                        <div style="margin-bottom: 1.25rem;">
                            <label for="selectProyecto"><strong>O seleccione un proyecto previamente registrado:</strong></label>
                            <select name="idProyecto" id="selectProyecto" class="form-control" style="margin-top: 0.5rem;" 
                                    onchange="if(this.value && this.options[this.selectedIndex].getAttribute('data-url')) { document.getElementById('inputUrlGithub').value = this.options[this.selectedIndex].getAttribute('data-url'); }">
                                <option value="">-- Seleccionar proyecto para auto-completar URL --</option>
                                <% for (Proyecto proy : resumen.getProyectosEstudiante()) { %>
                                    <option value="<%= proy.getId() %>" data-url="<%= HtmlUtil.escape(proy.getUrlGithub()) %>">
                                        <%= HtmlUtil.escape(proy.getNombre()) %> (<%= HtmlUtil.escape(proy.getUrlGithub()) %>)
                                    </option>
                                <% } %>
                            </select>
                        </div>
                    <% } %>

                    <div style="margin-bottom: 1.25rem;">
                        <label style="display: flex; align-items: center; gap: 0.5rem; cursor: pointer; font-size: 0.95rem;">
                            <input type="checkbox" id="checkConfirmacion" name="confirmacionEnvio" required>
                            <span>Confirmo que esta entrega corresponde al trabajo académico a evaluar bajo las reglas de calidad configuradas.</span>
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary" id="btnEjecutarAuditoriaAsignacion">
                        🚀 Enviar Entrega y Ejecutar Auditoría
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
                        Aún no registra auditorías ejecutadas en esta asignación. Ingrese su repositorio arriba y presione "Enviar Entrega y Ejecutar Auditoría".
                    </p>
                <% } %>
            </div>
        <% } %>

        <% 
            if (resultado != null) { 
                String idLabel = resultado.getCodigoVisible() != null ? resultado.getCodigoVisible() : (resultado.getIdVisible() != null ? "Auditoría #" + resultado.getIdVisible() : "");
        %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="section-title" style="margin-top: 0;">Resultado de la Evaluación <%= !idLabel.isEmpty() ? "(" + HtmlUtil.escape(idLabel) + ")" : "" %></h2>
                <%@ include file="/WEB-INF/views/fragments/resultadoAuditoria.jspf" %>
            </div>
        <% } %>
    </main>

</body>

</html>
