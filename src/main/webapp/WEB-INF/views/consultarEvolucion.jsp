<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenHistorialDTO" %>
<%@ page import="com.gr4.owlaudit.dto.AuditoriaResumenDTO" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionFinalDTO" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionFinalDTO.ComparacionRegla" %>
<%@ page import="com.gr4.owlaudit.model.Proyecto" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    EvolucionDTO evolucionDTO = (EvolucionDTO) request.getAttribute("evolucionDTO");
    ResumenHistorialDTO resumenHistorial = (ResumenHistorialDTO) request.getAttribute("resumenHistorialDTO");
    EvolucionFinalDTO evolucionFinal = (EvolucionFinalDTO) request.getAttribute("evolucionFinalDTO");
    @SuppressWarnings("unchecked")
    List<Proyecto> listaProyectos = (List<Proyecto>) request.getAttribute("listaProyectos");

    Long proyectoIdActual = evolucionDTO != null && evolucionDTO.getProyectoId() != null ? evolucionDTO.getProyectoId() : null;
    Long baseIdActual = evolucionDTO != null ? evolucionDTO.getBaseId() : null;
    Long comparadaIdActual = evolucionDTO != null ? evolucionDTO.getComparadaId() : null;
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
        <!-- Encabezado de la Funcionalidad -->
        <div class="card">
            <h1 class="card-title">📈 Consultar Evolución de Calidad</h1>
            <p class="card-subtitle">
                Compare los resultados históricos entre dos auditorías de un mismo proyecto para analizar variaciones de puntaje y el estado de sus hallazgos técnicos.
            </p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success"><%= HtmlUtil.escape(mensajeExito) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <!-- Paso 1: Selección del Proyecto -->
            <form action="${pageContext.request.contextPath}/evolucion" method="GET" style="display: flex; gap: 1rem; align-items: flex-end; margin-bottom: 0.5rem;">
                <div class="form-group" style="flex: 1; margin-bottom: 0;">
                    <label for="proyectoId" class="form-label">Proyecto Académico</label>
                    <% if (listaProyectos != null && !listaProyectos.isEmpty()) { %>
                        <select id="proyectoId" name="proyectoId" class="form-control" onchange="this.form.submit()" required>
                            <option value="">-- Seleccione un Proyecto Registrado --</option>
                            <% for (Proyecto p : listaProyectos) { %>
                                <option value="<%= p.getId() %>" <%= (proyectoIdActual != null && proyectoIdActual.equals(p.getId())) ? "selected" : "" %>>
                                    #<%= p.getId() %> &middot; <%= HtmlUtil.escape(p.getNombre()) %> (<%= HtmlUtil.escape(p.getUrlGithub()) %>)
                                </option>
                            <% } %>
                        </select>
                    <% } else { %>
                        <input type="number" id="proyectoId" name="proyectoId" class="form-control" 
                               placeholder="Ingrese el identificador del proyecto"
                               value="<%= proyectoIdActual != null ? proyectoIdActual : "" %>" min="1" required>
                    <% } %>
                </div>
                <button type="submit" class="btn btn-secondary">Cargar Historial</button>
            </form>
        </div>

        <!-- Paso 2: Historial de Auditorías y Formulario de Selección para Comparar -->
        <% if (resumenHistorial != null && resumenHistorial.getAuditorias() != null && !resumenHistorial.getAuditorias().isEmpty()) { %>
            <div class="card">
                <h3 style="margin-bottom: 0.25rem;">📋 Historial de Auditorías Registradas</h3>
                <p class="card-subtitle" style="margin-bottom: 1rem;">
                    Proyecto ID: <strong><%= resumenHistorial.getProyectoId() != null ? resumenHistorial.getProyectoId() : proyectoIdActual %></strong> &middot; 
                    Total registradas: <%= resumenHistorial.getAuditorias().size() %>
                </p>

                <!-- Tabla informativa de auditorías -->
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID Auditoría</th>
                                <th>Fecha y Hora</th>
                                <th>Puntaje Obtenido</th>
                                <th>Cumplimiento</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (AuditoriaResumenDTO item : resumenHistorial.getAuditorias()) { %>
                                <tr>
                                    <td><strong>#<%= item.getId() %></strong></td>
                                    <td><%= HtmlUtil.escape(item.getFechaFormateada()) %></td>
                                    <td><%= item.getPuntajeObtenido() %> / <%= item.getPuntajeMaximo() %> pts</td>
                                    <td>
                                        <span class="badge <%= item.getPorcentaje() >= 80 ? "badge-corregido" : "badge-alta" %>">
                                            <%= item.getPorcentaje() %>%
                                        </span>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <!-- Formulario para Indicar Dos Auditorías a Comparar (secuencia4.puml) -->
                <form action="${pageContext.request.contextPath}/evolucion" method="POST">
                    <input type="hidden" name="proyectoId" value="<%= proyectoIdActual != null ? proyectoIdActual : resumenHistorial.getProyectoId() %>">

                    <h4 style="margin: 1.5rem 0 1rem; color: var(--bg-primary);">Seleccione las dos auditorías a comparar:</h4>

                    <div class="comparison-picker">
                        <!-- Auditoría Base (más antigua) -->
                        <div class="picker-box">
                            <h4>📅 Auditoría Base (Referencia)</h4>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="baseId" class="form-label">Seleccione Auditoría Base</label>
                                <select id="baseId" name="baseId" class="form-control" required>
                                    <option value="">-- Seleccionar --</option>
                                    <% for (AuditoriaResumenDTO item : resumenHistorial.getAuditorias()) { %>
                                        <option value="<%= item.getId() %>" 
                                            <%= (baseIdActual != null && baseIdActual.equals(item.getId())) ? "selected" : "" %>>
                                            #<%= item.getId() %> &middot; <%= item.getFechaFormateada() %> (<%= item.getPorcentaje() %>%)
                                        </option>
                                    <% } %>
                                </select>
                            </div>
                        </div>

                        <!-- Auditoría Comparada (más reciente) -->
                        <div class="picker-box">
                            <h4>🔍 Auditoría Comparada (Reciente)</h4>
                            <div class="form-group" style="margin-bottom: 0;">
                                <label for="comparadaId" class="form-label">Seleccione Auditoría Comparada</label>
                                <select id="comparadaId" name="comparadaId" class="form-control" required>
                                    <option value="">-- Seleccionar --</option>
                                    <% for (AuditoriaResumenDTO item : resumenHistorial.getAuditorias()) { %>
                                        <option value="<%= item.getId() %>" 
                                            <%= (comparadaIdActual != null && comparadaIdActual.equals(item.getId())) ? "selected" : "" %>>
                                            #<%= item.getId() %> &middot; <%= item.getFechaFormateada() %> (<%= item.getPorcentaje() %>%)
                                        </option>
                                    <% } %>
                                </select>
                            </div>
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary">Comparar Evolución de Calidad</button>
                </form>
            </div>
        <% } %>

        <!-- Paso 3: Resultados de la Evolución (secuencia4.puml & cu04-consultarEvolucionCalidad.md) -->
        <% if (evolucionFinal != null) { %>
            <div class="card" style="border-top: 4px solid var(--brand-blue);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                    <div>
                        <h2 style="color: var(--bg-primary); font-size: 1.4rem;">🎯 Reporte Comparativo de Calidad</h2>
                        <p class="card-subtitle" style="margin-bottom: 0;">
                            Auditoría Base <strong>#<%= evolucionFinal.getBaseId() %></strong> (<%= evolucionFinal.getFechaBaseFormateada() %>) vs 
                            Auditoría Comparada <strong>#<%= evolucionFinal.getComparadaId() %></strong> (<%= evolucionFinal.getFechaComparadaFormateada() %>)
                        </p>
                    </div>
                </div>

                <% if (evolucionFinal.getMensaje() != null) { %>
                    <div class="alert alert-info"><%= HtmlUtil.escape(evolucionFinal.getMensaje()) %></div>
                <% } %>

                <!-- Métricas Clave de Evolución -->
                <div class="stat-grid">
                    <div class="stat-card primary">
                        <div class="stat-value"><%= evolucionFinal.getPuntajeObtenidoComparada() %> pts</div>
                        <div class="stat-label">Puntaje Reciente</div>
                        <span class="stat-delta <%= evolucionFinal.getVariacionPuntaje() >= 0 ? "positive" : "negative" %>">
                            <%= evolucionFinal.getVariacionPuntaje() >= 0 ? "+" : "" %><%= evolucionFinal.getVariacionPuntaje() %> pts
                        </span>
                    </div>

                    <div class="stat-card <%= evolucionFinal.getVariacionPorcentaje() >= 0 ? "success" : "danger" %>">
                        <div class="stat-value"><%= evolucionFinal.getPorcentajeComparada() %>%</div>
                        <div class="stat-label">Cumplimiento</div>
                        <span class="stat-delta <%= evolucionFinal.getVariacionPorcentaje() >= 0 ? "positive" : "negative" %>">
                            <%= evolucionFinal.getVariacionPorcentaje() >= 0 ? "+" : "" %><%= evolucionFinal.getVariacionPorcentaje() %>%
                        </span>
                    </div>

                    <div class="stat-card success">
                        <div class="stat-value" style="color: #16A34A;"><%= evolucionFinal.getTotalCorregidos() %></div>
                        <div class="stat-label">Corregidos</div>
                        <span class="badge badge-corregido" style="margin-top: 0.5rem;">Subsanados</span>
                    </div>

                    <div class="stat-card danger">
                        <div class="stat-value" style="color: #DC2626;"><%= evolucionFinal.getTotalPersistentes() %></div>
                        <div class="stat-label">Persistentes</div>
                        <span class="badge badge-persistente" style="margin-top: 0.5rem;">Requieren Atención</span>
                    </div>

                    <div class="stat-card warning">
                        <div class="stat-value" style="color: #D97706;"><%= evolucionFinal.getTotalNuevos() %></div>
                        <div class="stat-label">Nuevos</div>
                        <span class="badge badge-nuevo" style="margin-top: 0.5rem;">Incumplimientos</span>
                    </div>
                </div>

                <!-- Detalle por Reglas Técnicas Evaluadas -->
                <h3 style="margin: 1.5rem 0 0.5rem; font-size: 1.1rem;">Detalle de Reglas y Clasificación de Hallazgos</h3>
                <% if (evolucionFinal.getComparaciones() != null && !evolucionFinal.getComparaciones().isEmpty()) { %>
                    <div class="table-responsive">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>Regla Técnica</th>
                                    <th>Severidad</th>
                                    <th>Estado de Evolución</th>
                                    <th>Evidencia</th>
                                    <th>Recomendación</th>
                                    <th style="text-align: center;">Acción</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% for (ComparacionRegla r : evolucionFinal.getComparaciones()) { %>
                                    <tr>
                                        <td><strong><%= HtmlUtil.escape(r.getNombreRepresentativo()) %></strong></td>
                                        <td>
                                            <% String sev = r.getNivelSeveridad() != null ? r.getNivelSeveridad().name() : "BAJA"; %>
                                            <span class="badge badge-<%= sev.toLowerCase() %>">
                                                <%= sev %>
                                            </span>
                                        </td>
                                        <td>
                                            <% if ("Corregido".equalsIgnoreCase(r.getEstado())) { %>
                                                <span class="badge badge-corregido">✨ Corregido</span>
                                            <% } else if ("Persistente".equalsIgnoreCase(r.getEstado())) { %>
                                                <span class="badge badge-persistente">⚠️ Persistente</span>
                                            <% } else if ("Nuevo".equalsIgnoreCase(r.getEstado())) { %>
                                                <span class="badge badge-nuevo">⚡ Nuevo</span>
                                            <% } else { %>
                                                <span class="badge badge-muted"><%= HtmlUtil.escape(r.getEstado()) %></span>
                                            <% } %>
                                        </td>
                                        <td><%= HtmlUtil.escape(r.getEvidencia() != null && !r.getEvidencia().isBlank() ? r.getEvidencia() : "—") %></td>
                                        <td><%= HtmlUtil.escape(r.getRecomendacion() != null && !r.getRecomendacion().isBlank() ? r.getRecomendacion() : "—") %></td>
                                        <td style="text-align: center;">
                                            <% if (r.getHallazgoId() != null && ("Nuevo".equalsIgnoreCase(r.getEstado()) || "Persistente".equalsIgnoreCase(r.getEstado()))) { %>
                                                <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar&hallazgoId=<%= r.getHallazgoId() %>" 
                                                   class="btn btn-sm btn-primary">
                                                    Solicitar Feedback
                                                </a>
                                            <% } else { %>
                                                <span style="color: var(--text-muted);">—</span>
                                            <% } %>
                                        </td>
                                    </tr>
                                <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } else { %>
                    <p style="color: var(--text-muted); margin-top: 0.5rem;">No existen hallazgos técnicos en las auditorías comparadas.</p>
                <% } %>

                <div style="margin-top: 1rem;">
                    <a href="${pageContext.request.contextPath}/evolucion?proyectoId=<%= proyectoIdActual != null ? proyectoIdActual : "" %>" class="btn btn-secondary">Nueva Consulta</a>
                </div>
            </div>
        <% } %>
    </main>

</body>
</html>
