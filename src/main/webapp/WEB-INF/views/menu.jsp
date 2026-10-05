<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.UsuarioDTO" %>
<%@ page import="com.gr4.owlaudit.servlet.OpcionMenu" %>
<%@ page import="com.gr4.owlaudit.servlet.LoginServlet" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);
    @SuppressWarnings("unchecked")
    List<OpcionMenu> opciones = (List<OpcionMenu>) request.getAttribute("opciones");
    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Panel Principal</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <div class="card">
            <h1 class="card-title">Bienvenido, <%= HtmlUtil.escape(usuario.getNombre()) %></h1>
            <p class="card-subtitle" style="margin-bottom: 0;">
                Ha iniciado sesión como
                <span class="badge badge-<%= usuario.getRol().name().toLowerCase() %>"><%= usuario.getRol() %></span>
                &middot; <%= HtmlUtil.escape(usuario.getCorreo()) %>.
                Seleccione la funcionalidad con la que desea trabajar.
            </p>
        </div>

        <% String mensajeError = (String) request.getAttribute("mensajeError");
           if (mensajeError != null) { %>
            <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
        <% } %>

        <h2 class="section-title">Funcionalidades disponibles</h2>
        <div class="menu-grid">
            <% for (OpcionMenu opcion : opciones) {
                   if (!opcion.isDisponible()) continue; %>
                <div class="card menu-card">
                    <div class="menu-meta">
                        <span class="menu-icon"><%= opcion.getIcono() %></span>
                        <span class="badge badge-muted"><%= opcion.getCasoUso() %></span>
                    </div>
                    <h3><%= opcion.getTitulo() %></h3>
                    <p><%= opcion.getDescripcion() %></p>
                    <a class="btn btn-primary btn-block" href="<%= ctx %>/vistas?opcion=<%= opcion.getClave() %>">Ingresar</a>
                </div>
            <% } %>
        </div>

        <% 
           boolean tieneProximas = false;
           for (OpcionMenu op : opciones) {
               if (!op.isDisponible()) { tieneProximas = true; break; }
           }
           if (tieneProximas) { 
        %>
        <h2 class="section-title">Próximamente</h2>
        <div class="menu-grid">
            <% for (OpcionMenu opcion : opciones) {
                   if (opcion.isDisponible()) continue; %>
                <div class="card menu-card menu-card-disabled">
                    <div class="menu-meta">
                        <span class="menu-icon"><%= opcion.getIcono() %></span>
                        <span class="badge badge-muted"><%= opcion.getCasoUso() %></span>
                    </div>
                    <h3><%= opcion.getTitulo() %></h3>
                    <p><%= opcion.getDescripcion() %></p>
                    <button type="button" class="btn btn-block disabled" disabled>En desarrollo</button>
                </div>
            <% } %>
        </div>
        <% } %>
    </main>

</body>
</html>
