<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Locale" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenHistorialDTO" %>
<%@ page import="com.gr4.owlaudit.dto.EvolucionFinalDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%!
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static String fecha(ResumenHistorialDTO.AuditoriaHistorial a) {
        return a != null && a.getFechaHora() != null ? a.getFechaHora().format(FORMATO_FECHA) : "-";
    }

    private static String porcentaje(double valor) {
        return String.format(Locale.US, "%.1f", valor);
    }

    private static String claseDelta(double valor) {
        return valor > 0 ? "delta-positiva" : (valor < 0 ? "delta-negativa" : "delta-neutra");
    }

    private static String signo(double valor) {
        return valor > 0 ? "+" : "";
    }
%>
<%
    String mensajeError = (String) request.getAttribute("mensajeError");
    EvolucionDTO evolucionDTO = (EvolucionDTO) request.getAttribute("evolucionDTO");
    ResumenHistorialDTO historial = (ResumenHistorialDTO) request.getAttribute("resumenHistorialDTO");
    EvolucionFinalDTO evolucion = (EvolucionFinalDTO) request.getAttribute("evolucionFinalDTO");

    Long proyectoId = evolucionDTO != null ? evolucionDTO.getProyectoId() : null;
    boolean hayHistorial = historial != null && historial.getAuditorias() != null && !historial.getAuditorias().isEmpty();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Evolución de Calidad</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/incremento2.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">

        <!-- Paso 1: indicar el proyecto -->
        <div class="card">
            <div class="card-header">
                <span class="card-icon">📈</span>
                <div>
                    <h1 class="card-title">Evolución de Calidad</h1>
                    <p class="card-subtitle">Compare dos auditorías del historial y revise cómo cambió el puntaje y los hallazgos del proyecto.</p>
                </div>
            </div>

            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/evolucion" method="GET" class="form-inline">
                <div class="form-group">
                    <label for="proyectoId" class="form-label">Identificador del Proyecto</label>
                    <input type="number" id="proyectoId" name="proyectoId" class="form-control"
                           min="1" step="1" placeholder="Ej. 1"
                           value="<%= proyectoId != null ? proyectoId : "" %>" required>
                </div>
                <button type="submit" class="btn btn-primary">Consultar Historial</button>
            </form>
        </div>

        <!-- Paso 2: historial y selección de dos auditorías -->
        <% if (hayHistorial) { %>
            <div class="card">
                <h3 style="margin-bottom: 0.4rem;">🗂️ Auditorías Registradas</h3>
                <p class="card-subtitle">Seleccione exactamente dos auditorías. La más antigua se tomará como base.</p>

                <form action="${pageContext.request.contextPath}/evolucion" method="POST" id="formComparar">
                    <input type="hidden" name="proyectoId" value="<%= proyectoId != null ? proyectoId : "" %>">

                    <div class="table-wrapper">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th></th>
                                    <th>Auditoría</th>
                                    <th>Fecha y Hora</th>
                                    <th>Puntaje</th>
                                    <th>Cumplimiento</th>
                                </tr>
                            </thead>
                            <tbody>
                            <% for (ResumenHistorialDTO.AuditoriaHistorial a : historial.getAuditorias()) { %>
                                <tr>
                                    <td><input type="checkbox" name="auditoriaId" value="<%= a.getAuditoriaId() %>" aria-label="Seleccionar auditoría <%= a.getAuditoriaId() %>"></td>
                                    <td><strong>#<%= a.getAuditoriaId() %></strong></td>
                                    <td><%= fecha(a) %></td>
                                    <td><%= a.getPuntajeObtenido() %> / <%= a.getPuntajeMaximo() %> pts</td>
                                    <td>
                                        <div class="progress">
                                            <div class="progress-bar"><span style="width: <%= porcentaje(Math.max(0, Math.min(100, a.getPorcentaje()))) %>%;"></span></div>
                                            <span><%= porcentaje(a.getPorcentaje()) %>%</span>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary" id="btnComparar" disabled>Comparar Auditorías</button>
                        <a href="${pageContext.request.contextPath}/evolucion" class="btn btn-secondary">Cancelar</a>
                    </div>
                </form>
            </div>
        <% } else if (proyectoId != null && evolucion == null) { %>
            <!-- Mientras el historial no esté disponible se pueden indicar las auditorías por su identificador -->
            <div class="card">
                <h3 style="margin-bottom: 0.4rem;">🗂️ Indicar Auditorías a Comparar</h3>
                <p class="card-subtitle">Ingrese los identificadores de las dos auditorías del proyecto #<%= proyectoId %>.</p>

                <form action="${pageContext.request.contextPath}/evolucion" method="POST">
                    <input type="hidden" name="proyectoId" value="<%= proyectoId %>">
                    <div class="form-row">
                        <div class="form-group">
                            <label for="auditoriaA" class="form-label">Auditoría A</label>
                            <input type="number" id="auditoriaA" name="auditoriaId" class="form-control" min="1" step="1" placeholder="Ej. 3" required>
                        </div>
                        <div class="form-group">
                            <label for="auditoriaB" class="form-label">Auditoría B</label>
                            <input type="number" id="auditoriaB" name="auditoriaId" class="form-control" min="1" step="1" placeholder="Ej. 7" required>
                        </div>
                    </div>
                    <div class="form-actions">
                        <button type="submit" class="btn btn-primary">Comparar Auditorías</button>
                        <a href="${pageContext.request.contextPath}/evolucion" class="btn btn-secondary">Cancelar</a>
                    </div>
                </form>
            </div>
        <% } %>

        <!-- Paso 3: evolución de calidad -->
        <% if (evolucion != null) {
               ResumenHistorialDTO.AuditoriaHistorial base = evolucion.getBase();
               ResumenHistorialDTO.AuditoriaHistorial comparada = evolucion.getComparada();
        %>
            <div class="card card-accent">
                <h2 style="margin-bottom: 1.25rem;">🎯 Evolución de Calidad</h2>

                <div class="metric-grid">
                    <div class="metric">
                        <div class="metric-label">Base</div>
                        <div class="metric-value"><%= base != null ? porcentaje(base.getPorcentaje()) : "-" %>%</div>
                        <div class="metric-detail">
                            <%= base != null ? base.getPuntajeObtenido() + " / " + base.getPuntajeMaximo() + " pts" : "" %> · <%= fecha(base) %>
                        </div>
                    </div>
                    <div class="metric">
                        <div class="metric-label">Comparada</div>
                        <div class="metric-value"><%= comparada != null ? porcentaje(comparada.getPorcentaje()) : "-" %>%</div>
                        <div class="metric-detail">
                            <%= comparada != null ? comparada.getPuntajeObtenido() + " / " + comparada.getPuntajeMaximo() + " pts" : "" %> · <%= fecha(comparada) %>
                        </div>
                    </div>
                    <div class="metric">
                        <div class="metric-label">Variación de Puntaje</div>
                        <div class="metric-value <%= claseDelta(evolucion.getVariacionPuntaje()) %>">
                            <%= signo(evolucion.getVariacionPuntaje()) %><%= evolucion.getVariacionPuntaje() %> pts
                        </div>
                    </div>
                    <div class="metric">
                        <div class="metric-label">Variación de Porcentaje</div>
                        <div class="metric-value <%= claseDelta(evolucion.getVariacionPorcentaje()) %>">
                            <%= signo(evolucion.getVariacionPorcentaje()) %><%= porcentaje(evolucion.getVariacionPorcentaje()) %>%
                        </div>
                    </div>
                </div>

                <div class="metric-grid">
                    <div class="metric metric-nuevo">
                        <div class="metric-label">Nuevos</div>
                        <div class="metric-value"><%= evolucion.getTotalNuevos() %></div>
                    </div>
                    <div class="metric metric-persistente">
                        <div class="metric-label">Persistentes</div>
                        <div class="metric-value"><%= evolucion.getTotalPersistentes() %></div>
                    </div>
                    <div class="metric metric-corregido">
                        <div class="metric-label">Corregidos</div>
                        <div class="metric-value"><%= evolucion.getTotalCorregidos() %></div>
                    </div>
                </div>

                <% if (evolucion.getComparaciones() == null || evolucion.getComparaciones().isEmpty()) { %>
                    <div class="empty-state">
                        <div class="empty-icon">✅</div>
                        <p>No existen hallazgos en las auditorías comparadas</p>
                    </div>
                <% } else { %>
                    <div class="table-wrapper">
                        <table class="table">
                            <thead>
                                <tr>
                                    <th>Regla</th>
                                    <th>Severidad</th>
                                    <th>Estado</th>
                                </tr>
                            </thead>
                            <tbody>
                            <% for (EvolucionFinalDTO.ComparacionRegla c : evolucion.getComparaciones()) {
                                   String severidad = c.getNivelSeveridad() == null ? "" : c.getNivelSeveridad().toLowerCase();
                                   String estado = c.getEstado() == null ? "" : c.getEstado().toLowerCase();
                            %>
                                <tr>
                                    <td><%= HtmlUtil.escape(c.getNombreRepresentativo()) %></td>
                                    <td><span class="badge badge-<%= HtmlUtil.escape(severidad) %>"><%= HtmlUtil.escape(c.getNivelSeveridad()) %></span></td>
                                    <td><span class="badge badge-<%= HtmlUtil.escape(estado) %>"><%= HtmlUtil.escape(c.getEstado()) %></span></td>
                                </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </div>
                <% } %>

                <div class="form-actions">
                    <a href="${pageContext.request.contextPath}/evolucion<%= proyectoId != null ? "?proyectoId=" + proyectoId : "" %>" class="btn btn-secondary">Nueva Comparación</a>
                </div>
            </div>
        <% } %>
    </main>

    <script>
        // Habilita "Comparar" solo cuando hay exactamente dos auditorías seleccionadas
        (function () {
            var form = document.getElementById('formComparar');
            if (!form) return;
            var boton = document.getElementById('btnComparar');
            var casillas = form.querySelectorAll('input[name="auditoriaId"]');

            function actualizar() {
                var marcadas = form.querySelectorAll('input[name="auditoriaId"]:checked').length;
                boton.disabled = marcadas !== 2;
                casillas.forEach(function (c) {
                    c.disabled = !c.checked && marcadas >= 2;
                    c.closest('tr').classList.toggle('seleccionada', c.checked);
                });
            }

            casillas.forEach(function (c) { c.addEventListener('change', actualizar); });
        })();
    </script>

</body>
</html>
