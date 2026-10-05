<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.SolicitudDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    @SuppressWarnings("unchecked")
    List<SolicitudDTO> solicitudesPendientes = (List<SolicitudDTO>) request.getAttribute("solicitudesPendientes");
    SolicitudDTO solicitudActual = (SolicitudDTO) request.getAttribute("solicitudDTO");

    Long solicitudSeleccionadaId = (solicitudActual != null && solicitudActual.getSolicitudId() != null) 
            ? solicitudActual.getSolicitudId() : 501L;
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
                    <% for (SolicitudDTO sol : solicitudesPendientes) { 
                           boolean esSeleccionada = sol.getSolicitudId().equals(solicitudSeleccionadaId);
                    %>
                        <div class="request-item" style="<%= esSeleccionada ? "border-color: var(--brand-blue); background-color: #F0F7FF;" : "" %>">
                            <div class="request-header">
                                <span class="request-title">
                                    Solicitud #<%= sol.getSolicitudId() %> &middot; <%= HtmlUtil.escape(sol.getNombreProyecto()) %>
                                </span>
                                <span class="badge badge-<%= sol.getEstado().toLowerCase() %>"><%= sol.getEstado() %></span>
                            </div>
                            <p style="margin-bottom: 0.5rem; font-size: 0.9rem;">
                                <strong>Regla:</strong> <%= HtmlUtil.escape(sol.getReglaNombre()) %> &middot; 
                                <span class="badge badge-<%= sol.getSeveridad().toLowerCase() %>"><%= sol.getSeveridad() %></span>
                            </p>
                            <p style="color: var(--text-muted); font-size: 0.88rem; margin-bottom: 0.75rem;">
                                <strong>Justificación del Estudiante:</strong> <em>"<%= HtmlUtil.escape(sol.getJustificacion()) %>"</em>
                            </p>
                            <div style="font-size: 0.8rem; color: var(--text-muted); display: flex; justify-content: space-between; align-items: center;">
                                <span>📅 Fecha: <%= sol.getFechaHora() %></span>
                                <a class="btn <%= esSeleccionada ? "btn-primary" : "btn-secondary" %>" 
                                   style="padding: 0.35rem 0.85rem; font-size: 0.82rem;" 
                                   href="${pageContext.request.contextPath}/retroalimentacion?accion=atender&solicitudId=<%= sol.getSolicitudId() %>">
                                    <%= esSeleccionada ? "Atendiendo Ahora" : "Seleccionar" %>
                                </a>
                            </div>
                        </div>
                    <% } %>
                </div>
            <% } else { %>
                <div class="alert alert-info">No se registran solicitudes pendientes en este momento.</div>
            <% } %>

            <!-- Formulario de Respuesta y Orientación Técnica (secuencia6.puml) -->
            <% if (solicitudesPendientes != null && !solicitudesPendientes.isEmpty()) { 
                   SolicitudDTO seleccionada = null;
                   for (SolicitudDTO s : solicitudesPendientes) {
                       if (s.getSolicitudId().equals(solicitudSeleccionadaId)) {
                           seleccionada = s;
                           break;
                       }
                   }
                   if (seleccionada == null && !solicitudesPendientes.isEmpty()) {
                       seleccionada = solicitudesPendientes.get(0);
                   }
            %>
                <div class="card" style="background: #F8FAFC; border: 1px solid var(--border-color); margin-bottom: 0;">
                    <h3 style="margin-bottom: 0.5rem; color: var(--brand-blue);">
                        Redactar Orientación Técnica para Solicitud #<%= seleccionada.getSolicitudId() %>
                    </h3>
                    
                    <div class="context-box">
                        <p><strong>📁 Proyecto:</strong> <%= HtmlUtil.escape(seleccionada.getNombreProyecto()) %></p>
                        <p><strong>📐 Regla:</strong> <%= HtmlUtil.escape(seleccionada.getReglaNombre()) %></p>
                        <p><strong>⚠️ Evidencia:</strong> <span class="evidence-code"><%= HtmlUtil.escape(seleccionada.getEvidencia()) %></span></p>
                        <p><strong>💬 Justificación recibida:</strong> <%= HtmlUtil.escape(seleccionada.getJustificacion()) %></p>
                    </div>

                    <form action="${pageContext.request.contextPath}/retroalimentacion" method="POST">
                        <input type="hidden" name="accion" value="atender">
                        <input type="hidden" name="solicitudId" value="<%= seleccionada.getSolicitudId() %>">
                        <input type="hidden" name="reglaNombre" value="<%= HtmlUtil.escape(seleccionada.getReglaNombre()) %>">
                        <input type="hidden" name="justificacion" value="<%= HtmlUtil.escape(seleccionada.getJustificacion()) %>">

                        <div class="form-group">
                            <label for="orientacion" class="form-label">
                                Orientación Técnica y Recomendaciones Correctivas <span style="color: #DC2626;">*</span>
                            </label>
                            <div class="textarea-wrapper">
                                <textarea id="orientacion" name="orientacion" class="form-control" rows="6" 
                                          placeholder="Escriba aquí las pautas de solución, buenas prácticas o aclaración formal del estándar técnico (mínimo 10 caracteres)..."
                                          minlength="10" maxlength="1000" required oninput="actualizarContadorDocente(this)"></textarea>
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
            const longitud = textarea.value.length;
            contador.textContent = longitud + ' / 1000 caracteres (mínimo 10)';
            if (longitud < 10) {
                contador.style.color = '#DC2626';
            } else {
                contador.style.color = '#64748B';
            }
        }
    </script>

</body>
</html>
