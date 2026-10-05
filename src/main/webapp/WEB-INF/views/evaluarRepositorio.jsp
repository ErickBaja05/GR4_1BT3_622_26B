<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.AuditoriaDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    AuditoriaDTO auditoriaDTO = (AuditoriaDTO) request.getAttribute("auditoriaDTO");
    ResumenDTO resumen = (ResumenDTO) request.getAttribute("resumenDTO");
    ResultadoDTO resultado = (ResultadoDTO) request.getAttribute("resultadoDTO");
    String urlActual = auditoriaDTO != null ? auditoriaDTO.getUrl() : "";
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

            <!-- Paso 1: solicitar la previa (reglas vigentes y puntaje máximo) -->
            <form action="${pageContext.request.contextPath}/auditoria" method="GET">
                <div class="form-group">
                    <label for="url" class="form-label">URL del Repositorio a Auditar</label>
                    <input type="url" id="url" name="url" class="form-control" 
                           placeholder="https://github.com/usuario/repositorio" 
                           value="<%= HtmlUtil.escape(urlActual) %>" required>
                </div>

                <div style="display: flex; gap: 1rem;">
                    <button type="submit" class="btn btn-primary">Solicitar Auditoría</button>
                </div>
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
                    <input type="hidden" name="urlGithub" value="<%= HtmlUtil.escape(urlActual) %>">
                    <button type="submit" class="btn btn-primary">Confirmar Auditoría</button>
                    <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-secondary">Cancelar</a>
                </form>
            </div>
        <% } %>

        <!-- Paso 3: resultado final -->
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
                <div style="margin-top: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/auditoria" class="btn btn-secondary">Nueva Auditoría</a>
                </div>
            </div>
        <% } %>
    </main>

</body>
</html>