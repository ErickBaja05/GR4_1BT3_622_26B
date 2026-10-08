<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.SolicitudDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    SolicitudDTO solicitudDTO = (SolicitudDTO) request.getAttribute("solicitudDTO");

    Long hallazgoId = (solicitudDTO != null && solicitudDTO.getHallazgoId() != null) ? solicitudDTO.getHallazgoId() : null;
    String justificacion = (solicitudDTO != null && solicitudDTO.getJustificacion() != null) ? solicitudDTO.getJustificacion() : "";
    String reglaNombre = (String) request.getAttribute("reglaNombre");
    String severidad = (String) request.getAttribute("severidad");
    String evidencia = (String) request.getAttribute("evidencia");
    String proyectoNombre = (String) request.getAttribute("nombreProyecto");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Solicitar Retroalimentación</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">💬 Solicitar Retroalimentación de Hallazgo</h1>
            <p class="card-subtitle">
                Consulte a su docente o tutor sobre un hallazgo reportado en su auditoría técnica, argumentando su justificación técnica o duda de implementación.
            </p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success">
                    <strong>¡Éxito!</strong> <%= HtmlUtil.escape(mensajeExito) %>
                </div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error">
                    <strong>Atención:</strong> <%= HtmlUtil.escape(mensajeError) %>
                </div>
            <% } %>

            <% if (hallazgoId != null) { %>
                <!-- Contexto del Hallazgo a Consultar -->
                <div class="context-box">
                    <% if (proyectoNombre != null && !proyectoNombre.isBlank()) { %>
                        <p><strong>📁 Proyecto:</strong> <%= HtmlUtil.escape(proyectoNombre) %></p>
                    <% } %>
                    <p>
                        <strong>🔍 Hallazgo Referencia:</strong> #<%= hallazgoId %>
                        <% if (severidad != null && !severidad.isBlank()) { %>
                            &middot; <span class="badge badge-<%= severidad.toLowerCase() %>">Severidad <%= HtmlUtil.escape(severidad) %></span>
                        <% } %>
                    </p>
                    <% if (reglaNombre != null && !reglaNombre.isBlank()) { %>
                        <p><strong>📐 Regla Evaluada:</strong> <%= HtmlUtil.escape(reglaNombre) %></p>
                    <% } %>
                    <% if (evidencia != null && !evidencia.isBlank()) { %>
                        <p><strong>⚠️ Evidencia Detectada:</strong> <span class="evidence-code"><%= HtmlUtil.escape(evidencia) %></span></p>
                    <% } %>
                </div>

                <!-- Formulario de Solicitud (secuencia5.puml) -->
                <form action="${pageContext.request.contextPath}/retroalimentacion" method="POST">
                    <input type="hidden" name="accion" value="solicitar">
                    <input type="hidden" name="hallazgoId" value="<%= hallazgoId %>">
                    <% if (reglaNombre != null) { %><input type="hidden" name="reglaNombre" value="<%= HtmlUtil.escape(reglaNombre) %>"><% } %>
                    <% if (severidad != null) { %><input type="hidden" name="severidad" value="<%= HtmlUtil.escape(severidad) %>"><% } %>

                    <div class="form-group">
                        <label for="justificacion" class="form-label">
                            Justificación Técnica o Consulta <span style="color: #DC2626;">*</span>
                        </label>
                        <div class="textarea-wrapper">
                            <textarea id="justificacion" name="justificacion" class="form-control" rows="6" 
                                      placeholder="Explique las razones técnicas, excepciones o dudas de diseño respecto a este hallazgo (mínimo 10 caracteres)..."
                                      minlength="10" maxlength="1000" required oninput="actualizarContador(this)"><%= HtmlUtil.escape(justificacion) %></textarea>
                            <div class="char-counter" id="contadorChar">0 / 1000 caracteres (mínimo 10)</div>
                        </div>
                    </div>

                    <div style="display: flex; gap: 1rem; align-items: center; margin-top: 1.5rem;">
                        <button type="submit" class="btn btn-primary">Enviar Solicitud</button>
                        <a href="${pageContext.request.contextPath}/vistas" class="btn btn-secondary">Cancelar</a>
                    </div>
                </form>
            <% } else { %>
                <div class="alert alert-info" style="margin-top: 1rem;">
                    <strong>Aviso:</strong> No ha seleccionado un hallazgo técnico sobre el cual solicitar retroalimentación. 
                    Por favor, consulte los hallazgos en la 
                    <a href="${pageContext.request.contextPath}/auditoria">Evaluación de Repositorio</a> o en la 
                    <a href="${pageContext.request.contextPath}/evolucion">Evolución de Calidad</a> y presione el botón <strong>Solicitar Feedback</strong> correspondiente.
                </div>
                <div style="margin-top: 1.5rem;">
                    <a href="${pageContext.request.contextPath}/vistas" class="btn btn-secondary">Volver al Menú Principal</a>
                </div>
            <% } %>
        </div>

        <% if (mensajeExito != null) { %>
            <div class="card" style="border-left: 4px solid #16A34A;">
                <h3 style="color: #166534; margin-bottom: 0.5rem;">✅ Estado de la Solicitud: PENDIENTE</h3>
                <p class="card-subtitle">
                    Su docente ha sido notificado y revisará la justificación técnica registrada. Podrá consultar la orientación técnica brindada una vez sea atendida.
                </p>
                <div style="margin-top: 1rem;">
                    <a href="${pageContext.request.contextPath}/evolucion" class="btn btn-secondary">Volver a Evolución</a>
                </div>
            </div>
        <% } %>
    </main>

    <script>
        function actualizarContador(textarea) {
            const contador = document.getElementById('contadorChar');
            const longitud = textarea.value.length;
            contador.textContent = longitud + ' / 1000 caracteres (mínimo 10)';
            if (longitud < 10) {
                contador.style.color = '#DC2626';
            } else {
                contador.style.color = '#64748B';
            }
        }
        document.addEventListener('DOMContentLoaded', function() {
            const ta = document.getElementById('justificacion');
            if (ta) actualizarContador(ta);
        });
    </script>

</body>
</html>
