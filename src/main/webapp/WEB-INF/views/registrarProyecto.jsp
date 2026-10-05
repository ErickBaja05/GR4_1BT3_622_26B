<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.NuevoProyectoDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevoProyectoDTO dto = (NuevoProyectoDTO) request.getAttribute("proyectoDTO");
    // Si hubo error se conservan los datos ingresados para que el usuario los corrija
    NuevoProyectoDTO valores = (mensajeError != null && dto != null) ? dto : new NuevoProyectoDTO();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Registrar Proyecto</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">Registrar Nuevo Proyecto Académico</h1>
            <p class="card-subtitle">Vincule su repositorio de GitHub para habilitar las auditorías de código.</p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success"><%= HtmlUtil.escape(mensajeExito) %></div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } %>

            <form action="${pageContext.request.contextPath}/proyecto" method="POST">
                <div class="form-group">
                    <label for="nombre" class="form-label">Nombre del Proyecto</label>
                    <input type="text" id="nombre" name="nombre" class="form-control" 
                           placeholder="Ej. Sistema de Reservas - Grupo 4"
                           value="<%= HtmlUtil.escape(valores.getNombre()) %>" maxlength="150" required>
                </div>

                <div class="form-group">
                    <label for="urlGithub" class="form-label">URL del Repositorio de GitHub</label>
                    <input type="url" id="urlGithub" name="urlGithub" class="form-control" 
                           placeholder="Ej. https://github.com/usuario/repositorio"
                           value="<%= HtmlUtil.escape(valores.getUrlGithub()) %>" maxlength="255" required>
                </div>

                <button type="submit" class="btn btn-primary">Registrar Proyecto</button>
            </form>
        </div>

        <% if (mensajeExito != null && dto != null) { %>
            <div class="card" style="border-left: 4px solid var(--brand-blue);">
                <h3 style="margin-bottom: 0.5rem;">📁 Proyecto registrado</h3>
                <p><strong>Nombre:</strong> <%= HtmlUtil.escape(dto.getNombre()) %></p>
                <p><strong>URL GitHub:</strong> <%= HtmlUtil.escape(dto.getUrlGithub()) %></p>
                <div style="margin-top: 1rem;">
                    <a class="btn btn-secondary" href="${pageContext.request.contextPath}/auditoria?url=<%= java.net.URLEncoder.encode(dto.getUrlGithub().trim(), "UTF-8") %>">Auditar este repositorio</a>
                </div>
            </div>
        <% } %>
    </main>

</body>
</html>