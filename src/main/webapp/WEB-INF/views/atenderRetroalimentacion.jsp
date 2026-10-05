<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.gr4.owlaudit.dto.SolicitudDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> solicitudesPendientes = (List<Map<String, Object>>) request.getAttribute("solicitudesPendientes");
    SolicitudDTO solicitudActual = (SolicitudDTO) request.getAttribute("solicitudDTO");

    Long solicitudSeleccionadaId = null;
    if (solicitudActual != null && solicitudActual.getSolicitudId() != null) {
        solicitudSeleccionadaId = solicitudActual.getSolicitudId();
    } else if (solicitudesPendientes != null && !solicitudesPendientes.isEmpty()) {
        solicitudSeleccionadaId = (Long) solicitudesPendientes.get(0).get("solicitudId");
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Atender Retroalimentación</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">📝 Bandeja de Retroalimentación Técnica</h1>
            <p class="card-subtitle">
                Atienda las dudas y justificaciones técnicas formuladas por los estudiantes sobre los hallazgos de calidad de software detectados en sus repositorios.
            </p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success">
                    <strong>¡Confirmado!</strong> <%= HtmlUtil.escape(mensajeExito) %>
                </div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error">
                    <strong>Alerta:</strong> <%= HtmlUtil.escape(mensajeError) %>
                </div>
            <% } %>

            <!-- Lista de Solicitudes Pendientes -->
            <h3 style="margin-bottom: 0.75rem; color: var(--bg-primary);">Solicitudes Pendientes de Revisión</h3>
            <% if (solicitudesPendientes != null && !solicitudesPendientes.isEmpty()) { %>
                <div style="margin-bottom: 2rem;">
                    <% for (Map<String, Object> sol : solicitudesPendientes) { 
                           Long solId = (Long) sol.get("solicitudId");
                           boolean esSeleccionada = solId != null && solId.equals(solicitudSeleccionadaId);
                           String estado = String.valueOf(sol.get("estado"));
                           String severidad = String.valueOf(sol.get("severidad"));
                           String nombreProyecto = (String) sol.get("nombreProyecto");
                           String reglaNombre = (String) sol.get("reglaNombre");
                           String justificacion = (String) sol.get("justificacion");
                           String fechaHora = (String) sol.get("fechaHora");
                    %>
                        <div class="request-item<%= esSeleccionada ? " selected" : "" %>">
                            <div class="request-header">
                                <span class="request-title">
                                    Solicitud #<%= solId %> &middot; <%= HtmlUtil.escape(nombreProyecto) %>
                                </span>
                                <span class="badge badge-<%= estado.toLowerCase() %>"><%= estado %></span>
                            </div>
                            <p style="margin-bottom: 0.5rem; font-size: 0.9rem;">
                                <strong>Regla:</strong> <%= HtmlUtil.escape(reglaNombre) %> &middot; 
                                <span class="badge badge-<%= severidad.toLowerCase() %>"><%= severidad %></span>
                            </p>
                            <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 0.75rem;">
                                <strong>Justificación del Estudiante:</strong> <em>"<%= HtmlUtil.escape(justificacion) %>"</em>
                            </p>
                            <div style="font-size: 0.8rem; color: var(--text-muted); display: flex; justify-content: space-between; align-items: center;">
                                <span>📅 Fecha: <%= fechaHora %></span>
                                <a class="btn <%= esSeleccionada ? "btn-primary" : "btn-secondary" %>" 
                                   style="padding: 0.35rem 0.85rem; font-size: 0.82rem;" 
                                   href="${pageContext.request.contextPath}/retroalimentacion?accion=atender&solicitudId=<%= solId %>">
                                    <%= esSeleccionada ? "Atendiendo Ahora" : "Seleccionar" %>
                                </a>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="alert alert-info">No existen solicitudes de retroalimentación pendientes.</div>
            <% } %>

            <!-- Formulario de Respuesta y Orientación Técnica (secuencia6.puml) -->
            <% if (solicitudesPendientes != null && !solicitudesPendientes.isEmpty()) { 
                   Map<String, Object> seleccionada = null;
                   for (Map<String, Object> s : solicitudesPendientes) {
                       Long sId = (Long) s.get("solicitudId");
                       if (sId != null && sId.equals(solicitudSeleccionadaId)) {
                           seleccionada = s;
                           break;
                       }
                   }
                   if (seleccionada == null && !solicitudesPendientes.isEmpty()) {
                       seleccionada = solicitudesPendientes.get(0);
                   }
                   Long selId = seleccionada != null ? (Long) seleccionada.get("solicitudId") : null;
                   String selProyecto = seleccionada != null ? (String) seleccionada.get("nombreProyecto") : "";
                   String selRegla = seleccionada != null ? (String) seleccionada.get("reglaNombre") : "";
                   String selEvidencia = seleccionada != null ? (String) seleccionada.get("evidencia") : "";
                   String selJustificacion = seleccionada != null ? (String) seleccionada.get("justificacion") : "";
                   String orientacionTexto = (solicitudActual != null && solicitudActual.getOrientacion() != null) 
                           ? solicitudActual.getOrientacion() : "";
            %>
                <div class="card" style="background: #F8FAFC; border: 1px solid var(--border-color); margin-bottom: 0;">
                    <h3 style="margin-bottom: 0.5rem; color: var(--brand-blue);">
                        Redactar Orientación Técnica para Solicitud #<%= selId %>
                    </h3>
                    
                    <div class="context-box">
                        <p><strong>📁 Proyecto:</strong> <%= HtmlUtil.escape(selProyecto) %></p>
                        <p><strong>📐 Regla:</strong> <%= HtmlUtil.escape(selRegla) %></p>
                        <p><strong>⚠️ Evidencia:</strong> <span class="evidence-code"><%= HtmlUtil.escape(selEvidencia) %></span></p>
                        <p><strong>💬 Justificación recibida:</strong> <%= HtmlUtil.escape(selJustificacion) %></p>
                    </div>

                    <form action="${pageContext.request.contextPath}/retroalimentacion" method="POST">
                        <input type="hidden" name="accion" value="atender">
                        <input type="hidden" name="solicitudId" value="<%= selId %>">

                        <div class="form-group">
                            <label for="orientacion" class="form-label">
                                Orientación Técnica y Recomendaciones Correctivas <span style="color: #DC2626;">*</span>
                            </label>
                            <div class="textarea-wrapper">
                                <textarea id="orientacion" name="orientacion" class="form-control" rows="6" 
                                          placeholder="Escriba aquí las pautas de solución, buenas prácticas o aclaración formal del estándar técnico (mínimo 10 caracteres)..."
                                          minlength="10" maxlength="1000" required oninput="actualizarContadorDocente(this)"><%= HtmlUtil.escape(orientacionTexto) %></textarea>
                                <div class="char-counter" id="contadorCharDocente">0 / 1000 caracteres (mínimo 10)</div>
                            </div>
                        </div>

                        <div style="display: flex; gap: 1rem; align-items: center; margin-top: 1.5rem;">
                            <button type="submit" class="btn btn-primary">Registrar Orientación Técnica</button>
                            <a href="${pageContext.request.contextPath}/vistas" class="btn btn-secondary">Cancelar</a>
                        </div>
                    </form>
                </div>
            <% } %>
        </div>
    </main>

    <script>
        function actualizarContadorDocente(textarea) {
            const contador = document.getElementById('contadorCharDocente');
            if (!contador || !textarea) return;
            const longitud = textarea.value.length;
            contador.textContent = longitud + ' / 1000 caracteres (mínimo 10)';
            if (longitud < 10) {
                contador.style.color = '#DC2626';
            } else {
                contador.style.color = '#64748B';
            }
        }
        document.addEventListener('DOMContentLoaded', function() {
            const ta = document.getElementById('orientacion');
            if (ta) actualizarContadorDocente(ta);
        });
    </script>

</body>
</html>
