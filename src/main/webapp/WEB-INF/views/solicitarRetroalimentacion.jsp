<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.SolicitudDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    SolicitudDTO dto = (SolicitudDTO) request.getAttribute("solicitudDTO");
    // Si hubo error se conservan los datos ingresados para que el estudiante los corrija
    SolicitudDTO valores = (mensajeError != null && dto != null) ? dto : new SolicitudDTO();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Solicitar Retroalimentación</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/incremento2.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <div class="card-header">
                <span class="card-icon">💬</span>
                <div>
                    <h1 class="card-title">Solicitar Retroalimentación</h1>
                    <p class="card-subtitle">Pida al docente una revisión u orientación técnica sobre un hallazgo de su auditoría.</p>
                </div>
            </div>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success"><%= HtmlUtil.escape(mensajeExito) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/solicitarFeedback" method="POST">
                <div class="form-group">
                    <label for="hallazgoId" class="form-label">Hallazgo de la Auditoría</label>
                    <input type="number" id="hallazgoId" name="hallazgoId" class="form-control"
                           min="1" step="1" placeholder="Identificador del hallazgo (Ej. 4)"
                           value="<%= valores.getHallazgoId() != null ? valores.getHallazgoId() : "" %>" required>
                </div>

                <div class="form-group">
                    <label for="justificacion" class="form-label">Justificación Técnica</label>
                    <textarea id="justificacion" name="justificacion" class="form-control" rows="6"
                              minlength="10" maxlength="1000" data-contador="contadorJustificacion"
                              placeholder="Explique su duda o el argumento técnico por el que solicita la revisión del hallazgo..."
                              required><%= HtmlUtil.escape(valores.getJustificacion()) %></textarea>
                    <div class="form-hint">
                        <span>Mínimo 10 caracteres.</span>
                        <span class="contador" id="contadorJustificacion">0 / 1000</span>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">Enviar Solicitud</button>
                    <a href="${pageContext.request.contextPath}/vistas" class="btn btn-secondary">Cancelar</a>
                </div>
            </form>
        </div>

        <% if (mensajeExito != null && dto != null) { %>
            <div class="card card-accent">
                <h3 style="margin-bottom: 1rem;">📨 Solicitud enviada al docente</h3>
                <dl class="detalle">
                    <dt>Hallazgo</dt>
                    <dd>#<%= HtmlUtil.escape(dto.getHallazgoId()) %></dd>
                    <dt>Justificación</dt>
                    <dd><%= HtmlUtil.escape(dto.getJustificacion()) %></dd>
                    <dt>Estado</dt>
                    <dd><span class="badge badge-persistente">PENDIENTE</span></dd>
                </dl>
            </div>
        <% } %>
    </main>

    <script>
        // Contador de caracteres para los campos de texto largos
        document.querySelectorAll('textarea[data-contador]').forEach(function (campo) {
            var contador = document.getElementById(campo.dataset.contador);
            var minimo = parseInt(campo.getAttribute('minlength'), 10) || 0;
            var maximo = campo.getAttribute('maxlength');
            function actualizar() {
                var largo = campo.value.trim().length;
                contador.textContent = largo + ' / ' + maximo;
                contador.classList.toggle('invalido', largo > 0 && largo < minimo);
            }
            campo.addEventListener('input', actualizar);
            actualizar();
        });
    </script>

</body>
</html>
