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
    com.gr4.owlaudit.dto.DeudaTecnicaDTO deudaTecnica =
        (com.gr4.owlaudit.dto.DeudaTecnicaDTO) request.getAttribute("deudaTecnica");
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
                            <% 
                               int idxEval = 1;
                               for (Proyecto p : proyectos) {
                                   boolean esSel = proyectoIdActual != null && proyectoIdActual.equals(p.getId());
                                   if (!esSel && urlActual != null && urlActual.equals(p.getUrlGithub())) {
                                       esSel = true;
                                   }
                                   long idMostrado = (p.getIdEstudiante() != null) ? p.getIdEstudiante() : (long) idxEval;
                            %>
                                <option value="<%= p.getId() %>" data-url="<%= HtmlUtil.escape(p.getUrlGithub()) %>" <%= esSel ? "selected" : "" %>>
                                    #<%= idMostrado %> &middot; <%= HtmlUtil.escape(p.getNombre()) %> &middot; (<%= HtmlUtil.escape(p.getUrlGithub()) %>)
                                </option>
                            <% 
                                   idxEval++;
                               } 
                            %>
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
                <p style="font-size: 1.1rem; margin-top: 0.5rem; margin-bottom: 0.5rem;">
                    <strong>Dictamen Final:</strong>
                    <% if ("APROBADA".equals(resultado.getDictamen())) { %>
                        <span class="badge badge-corregido" style="font-size: 0.95rem; padding: 0.25rem 0.65rem;">✓ APROBADA</span>
                    <% } else { %>
                        <span class="badge badge-persistente" style="font-size: 0.95rem; padding: 0.25rem 0.65rem;">✗ NO APROBADA</span>
                    <% } %>
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

                <%-- ===== BLOQUE: Deuda Tecnica Estimada ===== --%>
                <% if (deudaTecnica != null) { %>
                <div style="margin-top: 2rem; padding: 1.5rem; background-color: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);">

                    <h3 style="color: #1e293b; margin-bottom: 1.25rem; font-size: 1.15rem; font-weight: 700; display: flex; align-items: center; gap: 0.5rem;">
                        &#128336; Deuda T&eacute;cnica Estimada
                    </h3>

                    <div style="display: flex; gap: 1rem; flex-wrap: wrap; margin-bottom: 1.25rem;">
                        <%-- ALTA --%>
                        <div style="flex: 1; min-width: 140px; background-color: #fef2f2; border: 1px solid #fecaca; border-radius: 8px; padding: 1rem; text-align: center;">
                            <div style="font-size: 1.6rem; font-weight: 700; color: #dc2626;">
                                <%= deudaTecnica.getCantidadAlta() %>
                            </div>
                            <div style="color: #991b1b; font-size: 0.8rem; font-weight: 600; margin-top: 0.25rem;">
                                Hallazgos ALTA
                            </div>
                            <div style="color: #ef4444; font-size: 0.75rem; margin-top: 0.4rem;">
                                <%= deudaTecnica.getCantidadAlta() * 120 %> min
                            </div>
                        </div>

                        <%-- MEDIA --%>
                        <div style="flex: 1; min-width: 140px; background-color: #fff7ed; border: 1px solid #fed7aa; border-radius: 8px; padding: 1rem; text-align: center;">
                            <div style="font-size: 1.6rem; font-weight: 700; color: #ea580c;">
                                <%= deudaTecnica.getCantidadMedia() %>
                            </div>
                            <div style="color: #9a3412; font-size: 0.8rem; font-weight: 600; margin-top: 0.25rem;">
                                Hallazgos MEDIA
                            </div>
                            <div style="color: #f97316; font-size: 0.75rem; margin-top: 0.4rem;">
                                <%= deudaTecnica.getCantidadMedia() * 60 %> min
                            </div>
                        </div>

                        <%-- BAJA --%>
                        <div style="flex: 1; min-width: 140px; background-color: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1rem; text-align: center;">
                            <div style="font-size: 1.6rem; font-weight: 700; color: #475569;">
                                <%= deudaTecnica.getCantidadBaja() %>
                            </div>
                            <div style="color: #334155; font-size: 0.8rem; font-weight: 600; margin-top: 0.25rem;">
                                Hallazgos BAJA
                            </div>
                            <div style="color: #64748b; font-size: 0.75rem; margin-top: 0.4rem;">
                                <%= deudaTecnica.getCantidadBaja() * 30 %> min
                            </div>
                        </div>

                        <%-- Total --%>
                        <div style="flex: 1; min-width: 160px; background-color: #f0f9ff; border: 2px solid #0284c7; border-radius: 8px; padding: 1rem; text-align: center;">
                            <div style="font-size: 1.8rem; font-weight: 800; color: #0284c7;">
                                <%= deudaTecnica.getTiempoFormateado() %>
                            </div>
                            <div style="color: #0369a1; font-size: 0.85rem; font-weight: 600; margin-top: 0.25rem;">
                                Tiempo total estimado
                            </div>
                            <div style="color: #0ea5e9; font-size: 0.75rem; margin-top: 0.4rem;">
                                <%= deudaTecnica.getTotalMinutos() %> minutos
                            </div>
                        </div>
                    </div>

                    <p style="color: #64748b; font-size: 0.82rem; margin: 0; border-top: 1px solid #f1f5f9; padding-top: 0.75rem;">
                        &#9432; El tiempo estimado considera: ALTA = 120 min, MEDIA = 60 min, BAJA = 30 min por hallazgo.
                    </p>
                </div>
                <% } %>
                <%-- ===== FIN: Deuda Tecnica Estimada ===== --%>

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
        document.addEventListener('DOMContentLoaded', function () {
            const selectElement = document.getElementById('proyectoId');
            if (selectElement && !document.getElementById('url').value) {
                actualizarUrlDesdeSelect(selectElement);
            }
        });
    </script>

</body>
</html>