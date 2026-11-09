<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");

    @SuppressWarnings("unchecked")
    List<Regla> reglasVigentes = (List<Regla>) request.getAttribute("reglasVigentes");
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("reglas");
    }
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("listaReglas");
    }

    NuevaReglaDTO valores = (mensajeError != null && dto != null) ? dto : new NuevaReglaDTO();
    String motorSel = valores.getTipoMotor();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Registrar Regla de Calidad</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">

        <!-- Notificaciones y Feedback Descartables -->
        <% if (mensajeExito != null && !mensajeExito.trim().isEmpty()) { %>
            <div id="alertSuccess" class="alert-dismissible alert-success" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span>✅</span>
                    <div><%= HtmlUtil.escape(mensajeExito) %></div>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertSuccess')">&times;</button>
            </div>
        <% } %>

        <% if (mensajeError != null && !mensajeError.trim().isEmpty()) { %>
            <div id="alertError" class="alert-dismissible alert-error" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span>⚠️</span>
                    <div><%= HtmlUtil.escape(mensajeError) %></div>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertError')">&times;</button>
            </div>
        <% } %>

        <% 
            Object idCursoReg = request.getAttribute("idCurso");
            if (idCursoReg == null && dto != null) idCursoReg = dto.getIdCurso();
            if (idCursoReg == null && request.getParameter("idCurso") != null && !request.getParameter("idCurso").isBlank()) {
                idCursoReg = request.getParameter("idCurso");
            }
        %>
        <!-- Formulario de Registro de Regla -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem; margin-bottom: 1rem;">
                <div>
                    <h1 class="card-title" style="margin-bottom: 0.25rem;">Configurar Regla de Evaluación Técnica</h1>
                    <p class="card-subtitle" style="margin-bottom: 0;">Cree políticas de calidad de código para ser evaluadas automáticamente en los repositorios.</p>
                </div>
                <div style="display: flex; gap: 0.5rem; align-items: center; flex-wrap: wrap;">
                    <% if (idCursoReg != null) { %>
                        <a href="${pageContext.request.contextPath}/asignaciones?idCurso=<%= idCursoReg %>" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                            <span>📝</span> Volver a Asignaciones
                        </a>
                    <% } %>
                    <a href="${pageContext.request.contextPath}/regla<%= idCursoReg != null ? "?idCurso=" + idCursoReg : "" %>" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                        <span>📋</span> Volver al Catálogo de Reglas
                    </a>
                </div>
            </div>

            <form action="${pageContext.request.contextPath}/regla" method="POST">
                <input type="hidden" name="accion" value="registrar">
                <% if (idCursoReg != null) { %>
                    <input type="hidden" name="idCurso" value="<%= idCursoReg %>">
                <% } %>
                <div class="form-group">
                    <label for="tipoMotor" class="form-label">Tipo de Motor de Evaluación</label>
                    <select id="tipoMotor" name="tipoMotor" class="form-control" required>
                        <option value="PRESENCIA_ARCHIVO" <%="PRESENCIA_ARCHIVO".equals(motorSel) ? "selected" : ""%>>Presencia de Archivo</option>
                        <option value="RESTRICCION_ARCHIVOS" <%="RESTRICCION_ARCHIVOS".equals(motorSel) ? "selected" : ""%>>Restricción de Archivos</option>
                        <option value="ESTRUCTURA_CARPETAS" <%="ESTRUCTURA_CARPETAS".equals(motorSel) ? "selected" : ""%>>Estructura de Carpetas</option>
                        <option value="CONVENCION_COMMITS" <%="CONVENCION_COMMITS".equals(motorSel) ? "selected" : ""%>>Convención de Commits</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="nombreRepresentativo" class="form-label">Nombre Representativo</label>
                    <input type="text" id="nombreRepresentativo" name="nombreRepresentativo" class="form-control"
                           placeholder="Ej. Verificar archivo README.md" maxlength="100"
                           value="<%= HtmlUtil.escape(valores.getNombreRepresentativo()) %>" required>
                </div>

                <div class="form-group">
                    <label for="descripcionDetallada" class="form-label">Descripción Detallada</label>
                    <textarea id="descripcionDetallada" name="descripcionDetallada" class="form-control" rows="3"
                              placeholder="Explique el propósito de la regla..." maxlength="200"
                              required><%= HtmlUtil.escape(valores.getDescripcionDetallada()) %></textarea>
                </div>

                <div class="form-group">
                    <label for="parametroExacto" class="form-label">Parámetro Exacto (Ruta / Expresión / Archivo)</label>
                    <input type="text" id="parametroExacto" name="parametroExacto" class="form-control"
                           placeholder="Ej. README.md, src/main/java o *.env" maxlength="100"
                           value="<%= HtmlUtil.escape(valores.getParametroExacto()) %>" required>
                </div>

                <div style="display: flex; gap: 0.75rem; margin-top: 1rem;">
                    <button type="submit" class="btn btn-primary">Registrar Regla</button>
                    <% if (idCursoReg != null) { %>
                        <a href="${pageContext.request.contextPath}/regla?idCurso=<%= idCursoReg %>" class="btn btn-secondary">Cancelar</a>
                    <% } else { %>
                        <a href="${pageContext.request.contextPath}/curso" class="btn btn-secondary">Cancelar</a>
                    <% } %>
                </div>
            </form>
        </div>

        <% if (mensajeExito != null && dto != null) { %>
            <div class="card" style="border-left: 4px solid var(--accent-gold);">
                <h3 style="margin-bottom: 0.5rem;">📐 Regla registrada</h3>
                <p><strong>Motor:</strong> <%= HtmlUtil.escape(dto.getTipoMotor()) %></p>
                <p><strong>Nombre:</strong> <%= HtmlUtil.escape(dto.getNombreRepresentativo()) %></p>
                <p><strong>Parámetro:</strong> <%= HtmlUtil.escape(dto.getParametroExacto()) %></p>
            </div>
        <% } %>

        <!-- Catálogo de Reglas Técnicas de solo consulta con Badges de Estado -->
        <% if (reglasVigentes != null && !reglasVigentes.isEmpty()) { %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="card-title" style="font-size: 1.25rem;">Catálogo de Reglas Técnicas</h2>
                <p class="card-subtitle">Políticas de evaluación técnica configuradas para las auditorías de repositorios.</p>

                <div class="table-responsive">
                    <table class="table" style="width: 100%; border-collapse: collapse; margin-top: 1rem;">
                        <thead>
                            <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                                <th style="padding: 0.65rem 0.5rem; text-align: center; width: 55px;">ID</th>
                                <th style="padding: 0.65rem 0.5rem; min-width: 160px;">Nombre Representativo</th>
                                <th style="padding: 0.65rem 0.5rem;">Motor</th>
                                <th style="padding: 0.65rem 0.5rem;">Parámetro Exacto</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center; min-width: 110px;">Estado Actual</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Regla r : reglasVigentes) {
                                boolean esVigente = r.esVigente();
                            %>
                                <tr style="border-bottom: 1px solid var(--border-color);">
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;"><strong>#<%= r.getId() %></strong></td>
                                    <td style="padding: 0.65rem 0.5rem;">
                                        <div style="font-weight: 600; color: var(--bg-primary);"><%= HtmlUtil.escape(r.getNombreRepresentativo()) %></div>
                                        <% if (r.getDescripcionDetallada() != null && !r.getDescripcionDetallada().isBlank()) { %>
                                            <div style="font-size: 0.8rem; color: var(--text-muted); line-height: 1.3;"><%= HtmlUtil.escape(r.getDescripcionDetallada()) %></div>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.65rem 0.5rem;"><%= (r.getTipoMotor() != null) ? r.getTipoMotor().name() : "—" %></td>
                                    <td style="padding: 0.65rem 0.5rem;"><code><%= HtmlUtil.escape(r.getParametroExacto()) %></code></td>
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;">
                                        <% if (esVigente) { %>
                                            <span class="badge-vigente" title="Regla habilitada para auditorías">
                                                <span class="status-dot"></span> VIGENTE
                                            </span>
                                        <% } else { %>
                                            <span class="badge-no-vigente" title="Regla inactiva">
                                                <span class="status-dot"></span> NO_VIGENTE
                                            </span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </main>

    <script>
        function cerrarAlerta(elementId) {
            var alerta = document.getElementById(elementId);
            if (alerta) { alerta.style.display = 'none'; }
        }
    </script>
</body>
</html>