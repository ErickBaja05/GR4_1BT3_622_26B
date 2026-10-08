<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.NuevoProyectoDTO" %>
<%@ page import="com.gr4.owlaudit.model.Proyecto" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="java.util.List" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevoProyectoDTO dto = (NuevoProyectoDTO) request.getAttribute("proyectoDTO");
    Proyecto proyectoRegistrado = (Proyecto) request.getAttribute("proyectoRegistrado");
    @SuppressWarnings("unchecked")
    List<Proyecto> proyectos = (List<Proyecto>) request.getAttribute("proyectos");
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
                <% if (proyectoRegistrado != null && proyectoRegistrado.getId() != null) { %>
                    <p><strong>ID Asignado:</strong> <%= proyectoRegistrado.getId() %></p>
                    <div style="margin-top: 1rem;">
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/auditoria?proyectoId=<%= proyectoRegistrado.getId() %>">Auditar este repositorio</a>
                    </div>
                <% } else { %>
                    <div style="margin-top: 1rem;">
                        <a class="btn btn-secondary" href="${pageContext.request.contextPath}/auditoria?url=<%= java.net.URLEncoder.encode(dto.getUrlGithub().trim(), "UTF-8") %>">Auditar este repositorio</a>
                    </div>
                <% } %>
            </div>
        <% } %>

        <% if (proyectos != null && !proyectos.isEmpty()) { %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="card-title" style="font-size: 1.25rem;">Proyectos Registrados en el Sistema</h2>
                <p class="card-subtitle">Repositorios académicos registrados disponibles para evaluación de calidad.</p>
                <table class="table" style="width: 100%; border-collapse: collapse; margin-top: 1rem;">
                    <thead>
                        <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                            <th style="padding: 0.5rem;">ID</th>
                            <th style="padding: 0.5rem;">Nombre</th>
                            <th style="padding: 0.5rem;">URL GitHub</th>
                            <th style="padding: 0.5rem; text-align: center;">Acción</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (Proyecto p : proyectos) { %>
                            <tr style="border-bottom: 1px solid var(--border-color);">
                                <td style="padding: 0.5rem;"><strong><%= p.getId() %></strong></td>
                                <td style="padding: 0.5rem;"><%= HtmlUtil.escape(p.getNombre()) %></td>
                                <td style="padding: 0.5rem;">
                                    <a href="<%= HtmlUtil.escape(p.getUrlGithub()) %>" target="_blank" rel="noopener noreferrer">
                                        <%= HtmlUtil.escape(p.getUrlGithub()) %>
                                    </a>
                                </td>
                                <td style="padding: 0.5rem; text-align: center;">
                                    <a class="btn btn-sm btn-secondary" href="${pageContext.request.contextPath}/auditoria?proyectoId=<%= p.getId() %>">Auditar</a>
                                </td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        <% } %>
    </main>

</body>
</html>