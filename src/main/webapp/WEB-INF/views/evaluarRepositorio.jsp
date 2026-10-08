<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.AuditoriaDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.model.Proyecto" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    AuditoriaDTO auditoriaDTO = (AuditoriaDTO) request.getAttribute("auditoriaDTO");
    ResumenDTO resumen = (ResumenDTO) request.getAttribute("resumenDTO");
    ResultadoDTO resultado = (ResultadoDTO) request.getAttribute("resultadoDTO");
    @SuppressWarnings("unchecked")
    List<Proyecto> proyectos = (List<Proyecto>) request.getAttribute("proyectos");
    String urlActual = auditoriaDTO != null && auditoriaDTO.getUrl() != null ? auditoriaDTO.getUrl() : "";
    Long proyectoIdActual = auditoriaDTO != null ? auditoriaDTO.getProyectoId() : null;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Auditar Repositorio</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">Auditoría Evaluativa de Repositorio</h1>
            <p class="card-subtitle">Ejecute la inspección automatizada de reglas sobre la estructura de GitHub.</p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success"><%= HtmlUtil.escape(mensajeExito) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <!-- Paso 1: Selección de proyecto y previa -->
            <form action="${pageContext.request.contextPath}/auditoria" method="GET">
                <% if (proyectos != null && !proyectos.isEmpty()) { %>
                    <div class="form-group">
                        <label for="proyectoId" class="form-label">Proyecto Registrado a Auditar</label>
                        <select id="proyectoId" name="proyectoId" class="form-control" onchange="actualizarUrlDesdeSelect(this)" required>
                            <option value="">-- Seleccione un proyecto registrado --</option>
                            <% for (Proyecto p : proyectos) { 
                                   boolean esSel = proyectoIdActual != null && proyectoIdActual.equals(p.getId());
                                   if (!esSel && urlActual != null && urlActual.equals(p.getUrlGithub())) {
                                       esSel = true;
                                   }
                            %>
                                <option value="<%= p.getId() %>" data-url="<%= HtmlUtil.escape(p.getUrlGithub()) %>" <%= esSel ? "selected" : "" %>>
                                    <%= HtmlUtil.escape(p.getNombre()) %> &middot; (<%= HtmlUtil.escape(p.getUrlGithub()) %>)
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="url" class="form-label">URL del Repositorio de GitHub</label>
                        <input type="url" id="url" name="url" class="form-control" 
                               placeholder="https://github.com/usuario/repositorio" 
                               value="<%= HtmlUtil.escape(urlActual) %>" required readonly>
                    </div>

                    <div style="display: flex; gap: 1rem;">
                        <button type="submit" class="btn btn-primary">Solicitar Auditoría</button>
                        <a href="${pageContext.request.contextPath}/proyecto" class="btn btn-secondary">Registrar Nuevo Proyecto</a>
                    </div>
                <% } else { %>
                    <div class="alert alert-info" style="margin-bottom: 1.25rem;">
                        No existen proyectos académicos registrados en el sistema. Debe registrar un proyecto antes de ejecutar una auditoría.
                    </div>
                    <div>
                        <a href="${pageContext.request.contextPath}/proyecto" class="btn btn-primary">Registrar Primer Proyecto</a>
                    </div>
                <% } %>
            </form>
        </div>

        <!-- Paso 2: previa de auditoría y confirmación -->
        <% if (resumen != null && resultado == null) { %>
            <div class="card" style="border-left: 4px solid var(--brand-blue);">
                <h3>📋 Previa de Auditoría</h3>
                <p style="margin: 0.75rem 0;"><strong>Repositorio:</strong> <%= HtmlUtil.escape(urlActual) %></p>
                <p style="margin-bottom: 1.25rem;"><strong>Puntaje Máximo Alcanzable:</strong> <%= resumen.getPuntajeMaximo() %> pts</p>
                <p class="card-subtitle">¿Desea continuar con la auditoría? Se consultará la estructura del repositorio en GitHub y se registrará el resultado en el historial.</p>

                <form action="${pageContext.request.contextPath}/auditoria" method="POST" style="display: flex; gap: 1rem;">
                    <input type="hidden" name="proyectoId" value="<%= proyectoIdActual != null ? proyectoIdActual : "" %>">
                    <input type="hidden" name="urlGithub" value="<%= HtmlUtil.escape(urlActual) %>">
                    <button type="submit" class="btn btn-primary">Confirmar Auditoría</button>
                    <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-secondary">Cancelar</a>
                </form>
            </div>
        <% } %>

        <!-- Paso 3: resultado final con desglose y retroalimentación -->
        <% if (resultado != null) { %>
            <div class="card" style="background: #F0FDF4; border: 1px solid #BBF7D0;">
                <h2 style="color: #166534; margin-bottom: 1rem;">🎯 Resultado de la Evaluación</h2>
                <p style="font-size: 1.1rem; margin-bottom: 0.5rem;">
                    <strong>Repositorio:</strong> <%= HtmlUtil.escape(urlActual) %>
                </p>
                <p style="font-size: 1.1rem; margin-bottom: 0.5rem;">
                    <strong>Puntaje Obtenido:</strong> <%= resultado.getPuntajeObtenido() %> / <%= resultado.getPuntajeMaximo() %> pts
                </p>
                <p style="font-size: 1.1rem; color: #15803D;">
                    <strong>Cumplimiento de Calidad:</strong> <%= resultado.getPorcentaje() %>%
                </p>

                <% if (resultado.getDetalles() != null && !resultado.getDetalles().isEmpty()) { %>
                    <h3 style="margin-top: 1.5rem; margin-bottom: 0.75rem; color: var(--bg-primary);">📋 Desglose de Reglas Evaluadas y Hallazgos</h3>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Regla Técnica</th>
                                    <th>Estado</th>
                                    <th>Puntos</th>
                                    <th>Severidad</th>
                                    <th>Evidencia</th>
                                    <th>Recomendación</th>
                                    <th>Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (ResultadoDTO.DetalleResultadoDTO d : resultado.getDetalles()) { %>
                                    <tr>
                                        <td><strong><%= HtmlUtil.escape(d.getNombreRegla()) %></strong></td>
                                        <td>
                                            <% if (d.isCumple()) { %>
                                                <span class="badge badge-corregido">✓ Cumple</span>
                                            <% } else { %>
                                                <span class="badge badge-persistente">✗ Incumple</span>
                                            <% } %>
                                        </td>
                                        <td><%= d.getPuntosObtenidos() %> / <%= d.getPonderacion() %> pts</td>
                                        <td>
                                            <% if (d.getNivelSeveridad() != null) { %>
                                                <span class="badge badge-<%= d.getNivelSeveridad().name().toLowerCase() %>"><%= d.getNivelSeveridad().name() %></span>
                                            <% } else { %>
                                                —
                                            <% } %>
                                        </td>
                                        <td><%= HtmlUtil.escape(d.getEvidencia() != null ? d.getEvidencia() : "—") %></td>
                                        <td><%= HtmlUtil.escape(d.getRecomendacion() != null ? d.getRecomendacion() : "—") %></td>
                                        <td>
                                            <% if (!d.isCumple() && d.getHallazgoId() != null) { %>
                                                <a class="btn btn-secondary" style="padding: 0.35rem 0.75rem; font-size: 0.82rem; white-space: nowrap;"
                                                   href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar&hallazgoId=<%= d.getHallazgoId() %>&reglaNombre=<%= java.net.URLEncoder.encode(d.getNombreRegla(), "UTF-8") %>&severidad=<%= d.getNivelSeveridad() != null ? d.getNivelSeveridad().name() : "ALTA" %>&evidencia=<%= java.net.URLEncoder.encode(d.getEvidencia() != null ? d.getEvidencia() : "", "UTF-8") %>">
                                                    Solicitar Feedback
                                                </a>
                                            <% } else { %>
                                                <span style="color: var(--text-muted); font-size: 0.85rem;">—</span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } %>

                <div style="margin-top: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-secondary">Nueva Auditoría</a>
                </div>
            </div>
        <% } %>
    </main>

    <script>
        function actualizarUrlDesdeSelect(selectElement) {
            const selectedOption = selectElement.options[selectElement.selectedIndex];
            const urlInput = document.getElementById('url');
            if (selectedOption && selectedOption.dataset.url) {
                urlInput.value = selectedOption.dataset.url;
            } else {
                urlInput.value = '';
            }
        }
        document.addEventListener('DOMContentLoaded', function() {
            const selectElement = document.getElementById('proyectoId');
            if (selectElement && !document.getElementById('url').value) {
                actualizarUrlDesdeSelect(selectElement);
            }
        });
    </script>

</body>
</html>