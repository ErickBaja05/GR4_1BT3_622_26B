<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");
    @SuppressWarnings("unchecked")
    List<Regla> reglasVigentes = (List<Regla>) request.getAttribute("reglasVigentes");
    // Si hubo error se conservan los datos ingresados para que el docente los corrija
    NuevaReglaDTO valores = (mensajeError != null && dto != null) ? dto : new NuevaReglaDTO();
    String motorSel = valores.getTipoMotor();
    String severidadSel = valores.getNivelSeveridad();
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
        <div class="card">
            <h1 class="card-title">Configurar Regla de Evaluación Técnica</h1>
            <p class="card-subtitle">Cree políticas de calidad de código para ser evaluadas automáticamente en los repositorios.</p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success"><%= HtmlUtil.escape(mensajeExito) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/regla" method="POST">
                <div class="form-group">
                    <label for="tipoMotor" class="form-label">Tipo de Motor de Evaluación</label>
                    <select id="tipoMotor" name="tipoMotor" class="form-control" required>
                        <option value="PRESENCIA_ARCHIVO" <%= "PRESENCIA_ARCHIVO".equals(motorSel) ? "selected" : "" %>>Presencia de Archivo</option>
                        <option value="RESTRICCION_ARCHIVOS" <%= "RESTRICCION_ARCHIVOS".equals(motorSel) ? "selected" : "" %>>Restricción de Archivos</option>
                        <option value="ESTRUCTURA_CARPETAS" <%= "ESTRUCTURA_CARPETAS".equals(motorSel) ? "selected" : "" %>>Estructura de Carpetas</option>
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
                              placeholder="Explique el propósito de la regla..." maxlength="200" required><%= HtmlUtil.escape(valores.getDescripcionDetallada()) %></textarea>
                </div>

                <div class="form-group">
                    <label for="parametroExacto" class="form-label">Parámetro Exacto (Ruta / Expresión / Archivo)</label>
                    <input type="text" id="parametroExacto" name="parametroExacto" class="form-control" 
                           placeholder="Ej. README.md, src/main/java o *.env" maxlength="100"
                           value="<%= HtmlUtil.escape(valores.getParametroExacto()) %>" required>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label for="nivelSeveridad" class="form-label">Nivel de Severidad</label>
                        <select id="nivelSeveridad" name="nivelSeveridad" class="form-control" required>
                            <option value="ALTA" <%= "ALTA".equals(severidadSel) ? "selected" : "" %>>ALTA</option>
                            <option value="MEDIA" <%= "MEDIA".equals(severidadSel) ? "selected" : "" %>>MEDIA</option>
                            <option value="BAJA" <%= "BAJA".equals(severidadSel) ? "selected" : "" %>>BAJA</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="ponderacion" class="form-label">Ponderación (Puntos)</label>
                        <input type="number" id="ponderacion" name="ponderacion" class="form-control" 
                               min="1" max="100" placeholder="Ej. 20"
                               value="<%= valores.getPonderacion() > 0 ? String.valueOf(valores.getPonderacion()) : "" %>" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary">Registrar Regla</button>
            </form>
        </div>

        <% if (mensajeExito != null && dto != null) { %>
            <div class="card" style="border-left: 4px solid var(--accent-gold);">
                <h3 style="margin-bottom: 0.5rem;">📐 Regla registrada</h3>
                <p><strong>Motor:</strong> <%= HtmlUtil.escape(dto.getTipoMotor()) %></p>
                <p><strong>Nombre:</strong> <%= HtmlUtil.escape(dto.getNombreRepresentativo()) %></p>
                <p><strong>Parámetro:</strong> <%= HtmlUtil.escape(dto.getParametroExacto()) %></p>
                <p><strong>Severidad:</strong> <%= HtmlUtil.escape(dto.getNivelSeveridad()) %> | <strong>Puntos:</strong> <%= dto.getPonderacion() %></p>
            </div>
        <% } %>

        <% if (reglasVigentes != null && !reglasVigentes.isEmpty()) { %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="card-title" style="font-size: 1.25rem;">Catálogo de Reglas Técnicas Vigentes</h2>
                <p class="card-subtitle">Políticas de evaluación activas configuradas para la materia.</p>
                <div class="table-responsive">
                    <table class="table" style="width: 100%; border-collapse: collapse; margin-top: 1rem;">
                        <thead>
                            <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                                <th style="padding: 0.5rem;">ID</th>
                                <th style="padding: 0.5rem;">Nombre Representativo</th>
                                <th style="padding: 0.5rem;">Motor</th>
                                <th style="padding: 0.5rem;">Parámetro Exacto</th>
                                <th style="padding: 0.5rem;">Severidad</th>
                                <th style="padding: 0.5rem; text-align: right;">Puntos</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Regla r : reglasVigentes) { %>
                                <tr style="border-bottom: 1px solid var(--border-color);">
                                    <td style="padding: 0.5rem;"><strong>#<%= r.getId() %></strong></td>
                                    <td style="padding: 0.5rem;"><%= HtmlUtil.escape(r.getNombreRepresentativo()) %></td>
                                    <td style="padding: 0.5rem;"><%= r.getTipoMotor() != null ? r.getTipoMotor().name() : "—" %></td>
                                    <td style="padding: 0.5rem;"><code><%= HtmlUtil.escape(r.getParametroExacto()) %></code></td>
                                    <td style="padding: 0.5rem;">
                                        <% String s = r.getNivelSeveridad() != null ? r.getNivelSeveridad().name() : "BAJA"; %>
                                        <span class="badge badge-<%= s.toLowerCase() %>"><%= s %></span>
                                    </td>
                                    <td style="padding: 0.5rem; text-align: right;"><strong><%= r.getPonderacion() %> pts</strong></td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </main>

</body>
</html>