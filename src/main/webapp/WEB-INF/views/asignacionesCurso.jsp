<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="com.gr4.owlaudit.model.Asignacion" %>
<%@ page import="com.gr4.owlaudit.model.Curso" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String ctx = request.getContextPath();
    Curso curso = (Curso) request.getAttribute("curso");
    @SuppressWarnings("unchecked")
    List<Asignacion> asignaciones = (List<Asignacion>) request.getAttribute("asignaciones");
    @SuppressWarnings("unchecked")
    Map<Long, Integer> intentosConsumidosMap = (Map<Long, Integer>) request.getAttribute("intentosConsumidosMap");
    String mensajeError = (String) request.getAttribute("mensajeError");
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
%>
<!DOCTYPE html>
<html lang="es">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Asignaciones del Curso</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
            <div>
                <div style="display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.25rem;">
                    <a href="<%= ctx %>/curso" class="btn btn-secondary btn-sm" style="text-decoration: none;">&larr; Volver a Mis Cursos</a>
                    <% if (curso != null && curso.getCodigo() != null) { %>
                        <span class="badge badge-primary"><%= HtmlUtil.escape(curso.getCodigo()) %></span>
                    <% } %>
                </div>
                <h1 class="card-title">
                    Asignaciones: <%= (curso != null && curso.getNombre() != null) ? HtmlUtil.escape(curso.getNombre()) : "Curso Académico" %>
                </h1>
                <p class="card-subtitle">
                    Revise las fechas límites, controle sus intentos permitidos y entregue sus proyectos para evaluación de calidad de código.
                </p>
            </div>
        </div>

        <% if (mensajeError != null && !mensajeError.isBlank()) { %>
            <div class="alert alert-error" style="margin-top: 1rem;">
                <%= HtmlUtil.escape(mensajeError) %>
            </div>
        <% } %>

        <% if (mensajeExito != null && !mensajeExito.isBlank()) { %>
            <div class="alert alert-success alert-dismissible" style="margin-top: 1rem;">
                <span><strong>¡Éxito!</strong> <%= HtmlUtil.escape(mensajeExito) %></span>
            </div>
        <% } %>

        <% if (asignaciones == null || asignaciones.isEmpty()) { %>
            <div class="card" style="margin-top: 1.5rem; text-align: center; padding: 3rem 1rem;">
                <p style="color: var(--text-muted); font-size: 1.1rem; margin-bottom: 1rem;">
                    📖 Este curso aún no tiene asignaciones académicas programadas.
                </p>
                <a href="<%= ctx %>/curso" class="btn btn-primary">Volver al catálogo de cursos</a>
            </div>
        <% } else { %>
            <div class="menu-grid" style="margin-top: 1.5rem;">
                <%
                    for (Asignacion asig : asignaciones) {
                        boolean estaVencida = asig.estaVencida();
                        int permitidos = (asig.getIntentosPermitidos() != null && asig.getIntentosPermitidos() > 0)
                                ? asig.getIntentosPermitidos() : 1;
                        int consumidos = (intentosConsumidosMap != null && asig.getId() != null)
                                ? intentosConsumidosMap.getOrDefault(asig.getId(), 0) : 0;
                        int disponibles = Math.max(0, permitidos - consumidos);
                        boolean intentosAgotados = asig.haAlcanzadoLimiteIntentos(consumidos);
                        boolean puedeEntregar = !estaVencida && !intentosAgotados;
                        String fechaLimiteStr = (asig.getFechaLimite() != null)
                                ? asig.getFechaLimite().format(dtf) : "Sin fecha límite";
                %>
                    <div class="card menu-card" style="display: flex; flex-direction: column; justify-content: space-between;">
                        <div>
                            <div class="menu-meta" style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.75rem;">
                                <span class="badge <%= estaVencida ? "badge-no-vigente" : "badge-vigente" %>">
                                    <%= estaVencida ? "🔒 Vencida" : "🟢 Abierta" %>
                                </span>
                                <span class="badge badge-docente" title="Umbral de Aprobación">
                                    Umbral: <%= asig.getUmbralAprobacion() != null ? asig.getUmbralAprobacion() : 70.0 %>%
                                </span>
                            </div>

                            <h3 style="margin-top: 0; font-size: 1.15rem;">
                                <%= HtmlUtil.escape(asig.getNombre()) %>
                            </h3>

                            <p style="color: var(--text-muted); font-size: 0.9rem; margin-bottom: 1rem;">
                                <%= (asig.getDescripcion() != null && !asig.getDescripcion().isBlank())
                                        ? HtmlUtil.escape(asig.getDescripcion())
                                        : "Sin descripción disponible." %>
                            </p>

                            <div style="background-color: var(--bg-body); padding: 0.75rem; border-radius: var(--radius-sm); margin-bottom: 1rem; font-size: 0.85rem;">
                                <div style="margin-bottom: 0.35rem;">
                                    <strong>📅 Fecha límite:</strong> 
                                    <span style="<%= estaVencida ? "color: var(--accent-red); font-weight: 600;" : "" %>">
                                        <%= fechaLimiteStr %>
                                    </span>
                                </div>
                                <div>
                                    <strong>🎯 Intentos disponibles:</strong>
                                    <span style="<%= intentosAgotados ? "color: var(--accent-red); font-weight: 600;" : "color: var(--accent-green-dark); font-weight: 600;" %>">
                                        <%= disponibles %> de <%= permitidos %>
                                    </span>
                                    <span style="color: var(--text-muted); font-size: 0.8rem;">
                                        (<%= consumidos %> consumido<%= consumidos == 1 ? "" : "s" %>)
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div>
                            <% if (puedeEntregar) { %>
                                <a href="<%= ctx %>/asignaciones?idAsignacion=<%= asig.getId() %>" 
                                   class="btn btn-primary btn-block" id="btnEntregar_<%= asig.getId() %>">
                                    🚀 Entregar Asignación
                                </a>
                            <% } else { %>
                                <button type="button" class="btn btn-secondary btn-block" disabled 
                                        style="opacity: 0.6; cursor: not-allowed;"
                                        title="<%= estaVencida ? "La fecha límite ha expirado" : "Ha alcanzado el límite máximo de intentos" %>">
                                    <%= estaVencida ? "⛔ Entrega Cerrada (Vencida)" : "⛔ Intentos Agotados" %>
                                </button>
                            <% } %>

                            <a href="<%= ctx %>/evolucion?idAsignacion=<%= asig.getId() %>" 
                               class="btn btn-secondary btn-block" style="margin-top: 0.5rem;" id="btnEvolucion_<%= asig.getId() %>">
                                📈 Consultar Evolución
                            </a>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>
    </main>

</body>

</html>

