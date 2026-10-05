<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenHistorialDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenHistorialDTO.AuditoriaItemDTO" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionFinalDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ComparacionReglaDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    String mensajeError = (String) request.getAttribute("mensajeError");
    String mensajeInfo = (String) request.getAttribute("mensajeInfo");
    EvolucionDTO dto = (EvolucionDTO) request.getAttribute("evolucionDTO");
    ResumenHistorialDTO resumen = (ResumenHistorialDTO) request.getAttribute("resumenHistorialDTO");
    EvolucionFinalDTO evolucion = (EvolucionFinalDTO) request.getAttribute("evolucionFinalDTO");

    Long proyectoIdActual = null;
    Long baseIdActual = null;
    Long comparadaIdActual = null;

    if (dto != null) {
        proyectoIdActual = dto.getProyectoId();
        baseIdActual = dto.getBaseId();
        comparadaIdActual = dto.getComparadaId();
    } else if (resumen != null) {
        proyectoIdActual = resumen.getProyectoId();
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Consultar Evolución de Calidad</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <!-- Encabezado de la funcionalidad -->
        <div class="card">
            <h1 class="card-title">📈 Consultar Evolución de Calidad</h1>
            <p class="card-subtitle">Compare dos auditorías registradas de un proyecto para analizar la variación de puntaje y el progreso de los hallazgos técnicos.</p>

            <% if (mensajeInfo != null) { %>
                <div class="alert alert-info"><%= HtmlUtil.escape(mensajeInfo) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <!-- Paso 1: Selección del proyecto -->
            <form action="${pageContext.request.contextPath}/evolucion" method="GET" style="display: flex; gap: 1rem; align-items: flex-end;">
                <div class="form-group" style="flex: 1; margin-bottom: 0;">
                    <label for="proyectoId" class="form-label">Identificador del Proyecto</label>
                    <input type="number" id="proyectoId" name="proyectoId" class="form-control" 
                           placeholder="Ej. 1" min="1"
                           value="<%= proyectoIdActual != null ? proyectoIdActual : "" %>" required>
                </div>
                <button type="submit" class="btn btn-primary">Consultar Historial</button>
            </form>
        </div>

        <!-- Paso 2: Historial de auditorías registradas del proyecto -->
        <% if (resumen != null && resumen.getHistorialAuditorias() != null && !resumen.getHistorialAuditorias().isEmpty()) { 
               List<AuditoriaItemDTO> historial = resumen.getHistorialAuditorias();
        %>
            <div class="card">
                <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">📋 Historial de Auditorías del Proyecto #<%= resumen.getProyectoId() %></h2>
                <p class="card-subtitle" style="margin-bottom: 1rem;">Seleccione dos auditorías a comparar. El sistema tomará automáticamente la más antigua como base y la más reciente como comparada.</p>

                <form action="${pageContext.request.contextPath}/evolucion" method="POST">
                    <input type="hidden" name="proyectoId" value="<%= resumen.getProyectoId() %>">

                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th style="width: 70px; text-align: center;">Base</th>
                                    <th style="width: 70px; text-align: center;">Comp.</th>
                                    <th>ID</th>
                                    <th>Fecha y Hora</th>
                                    <th>Puntaje Obtenido</th>
                                    <th>Puntaje Máximo</th>
                                    <th>Porcentaje</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (AuditoriaItemDTO item : historial) { 
                                       boolean esBase = baseIdActual != null && baseIdActual.equals(item.getAuditoriaId());
                                       boolean esComp = comparadaIdActual != null && comparadaIdActual.equals(item.getAuditoriaId());
                                %>
                                    <tr>
                                        <td style="text-align: center;">
                                            <input type="radio" name="baseId" value="<%= item.getAuditoriaId() %>" 
                                                   <%= esBase ? "checked" : "" %> required>
                                        </td>
                                        <td style="text-align: center;">
                                            <input type="radio" name="comparadaId" value="<%= item.getAuditoriaId() %>" 
                                                   <%= esComp ? "checked" : "" %> required>
                                        </td>
                                        <td><strong>#<%= item.getAuditoriaId() %></strong></td>
                                        <td><%= item.getFechaFormateada() %></td>
                                        <td><%= item.getPuntajeObtenido() %> pts</td>
                                        <td><%= item.getPuntajeMaximo() %> pts</td>
                                        <td><span class="badge badge-muted"><%= item.getPorcentaje() %> %</span></td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>

                    <div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
                        <button type="submit" class="btn btn-primary">Comparar Auditorías</button>
                        <button type="submit" name="accion" value="cancelar" class="btn btn-secondary" formnovalidate>Cancelar</button>
                    </div>
                </form>
            </div>
        <% } %>

        <!-- Paso 3: Evolución de Calidad Resultante -->
        <% if (evolucion != null) { 
               int varPts = evolucion.getVariacionPuntaje();
               double varPct = evolucion.getVariacionPorcentaje();
               String clasePts = varPts > 0 ? "stat-positive" : (varPts < 0 ? "stat-negative" : "stat-neutral");
               String clasePct = varPct > 0.0 ? "stat-positive" : (varPct < 0.0 ? "stat-negative" : "stat-neutral");
               String prefijoPts = varPts > 0 ? "+" : "";
               String prefijoPct = varPct > 0.0 ? "+" : "";
        %>
            <div class="card" style="border-left: 4px solid var(--brand-blue);">
                <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--bg-primary); margin-bottom: 0.5rem;">
                    🎯 Resultado de la Evolución de Calidad
                </h2>
                <p class="card-subtitle" style="margin-bottom: 1.25rem;">
                    Comparación entre Auditoría Base <strong>#<%= evolucion.getBaseId() %></strong> (<%= evolucion.getFechaBaseFormateada() %>)
                    y Auditoría Comparada <strong>#<%= evolucion.getComparadaId() %></strong> (<%= evolucion.getFechaComparadaFormateada() %>).
                </p>

                <!-- Métricas destacadas -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-label">Variación Puntaje</div>
                        <div class="stat-value <%= clasePts %>"><%= prefijoPts %><%= varPts %> pts</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Variación Calidad</div>
                        <div class="stat-value <%= clasePct %>"><%= prefijoPct %><%= varPct %> %</div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Nuevos</div>
                        <div class="stat-value" style="color: #2563EB;"><%= evolucion.getTotalNuevos() %></div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Persistentes</div>
                        <div class="stat-value" style="color: #DC2626;"><%= evolucion.getTotalPersistentes() %></div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-label">Corregidos</div>
                        <div class="stat-value" style="color: #16A34A;"><%= evolucion.getTotalCorregidos() %></div>
                    </div>
                </div>

                <!-- Mensaje para escenario alternativo 4 si no hay hallazgos -->
                <% if (evolucion.getMensaje() != null) { %>
                    <div class="alert alert-info" style="margin-top: 1rem;">
                        <%= HtmlUtil.escape(evolucion.getMensaje()) %>
                    </div>
                <% } %>

                <!-- Lista de comparaciones por regla -->
                <% if (evolucion.getComparaciones() != null && !evolucion.getComparaciones().isEmpty()) { %>
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin: 1.5rem 0 0.5rem;">Detalle de Hallazgos por Regla</h3>
                    <div class="table-container">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Regla Técnica</th>
                                    <th>Severidad</th>
                                    <th>Estado de Evolución</th>
                                    <th>Evidencia / Recomendación</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (ComparacionReglaDTO comp : evolucion.getComparaciones()) { 
                                       String badgeEstado = "badge-muted";
                                       if ("Nuevo".equalsIgnoreCase(comp.getEstado())) badgeEstado = "badge-nuevo";
                                       else if ("Persistente".equalsIgnoreCase(comp.getEstado())) badgeEstado = "badge-persistente";
                                       else if ("Corregido".equalsIgnoreCase(comp.getEstado())) badgeEstado = "badge-corregido";

                                       String badgeSeveridad = "badge-muted";
                                       if (comp.getNivelSeveridad() != null) {
                                           badgeSeveridad = "badge-" + comp.getNivelSeveridad().name().toLowerCase();
                                       }
                                %>
                                    <tr>
                                        <td><strong><%= HtmlUtil.escape(comp.getNombreRepresentativo()) %></strong></td>
                                        <td>
                                            <span class="badge <%= badgeSeveridad %>">
                                                <%= comp.getNivelSeveridad() != null ? comp.getNivelSeveridad() : "-" %>
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge <%= badgeEstado %>">
                                                <%= HtmlUtil.escape(comp.getEstado()) %>
                                            </span>
                                        </td>
                                        <td>
                                            <% if (comp.getEvidencia() != null && !comp.getEvidencia().isBlank()) { %>
                                                <div style="font-size: 0.85rem; color: var(--text-main); margin-bottom: 0.25rem;">
                                                    <strong>Evidencia:</strong> <%= HtmlUtil.escape(comp.getEvidencia()) %>
                                                </div>
                                            <% } %>
                                            <% if (comp.getRecomendacion() != null && !comp.getRecomendacion().isBlank()) { %>
                                                <div style="font-size: 0.825rem; color: var(--text-muted);">
                                                    <strong>Recomendación:</strong> <%= HtmlUtil.escape(comp.getRecomendacion()) %>
                                                </div>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>

</body>
</html>
