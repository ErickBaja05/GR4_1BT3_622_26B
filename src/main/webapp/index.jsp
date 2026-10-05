<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.servlet.LoginServlet" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    // Si el usuario ya tiene una sesión activa, se envía directamente al menú principal
    if (session.getAttribute(LoginServlet.ATRIBUTO_USUARIO) != null) {
        response.sendRedirect(request.getContextPath() + "/vistas");
        return;
    }
    String mensajeError = (String) request.getAttribute("mensajeError");
    String correo = (String) request.getAttribute("correo");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Iniciar Sesión</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <header class="navbar">
        <div class="brand">🦉 OwlAudit</div>
    </header>

    <main class="login-wrapper">
        <div class="card login-card">
            <div class="login-brand">
                <div class="logo">🦉</div>
                <h1>Owl<span>Audit</span></h1>
                <p class="card-subtitle" style="margin-bottom: 0;">Auditoría de calidad de repositorios académicos</p>
            </div>

            <% if (mensajeError != null) { %>
                <div class="alert alert-error"><%= HtmlUtil.escape(mensajeError) %></div>
            <% } else if (request.getParameter("logout") != null) { %>
                <div class="alert alert-success">Sesión cerrada correctamente.</div>
            <% } else if (request.getParameter("sesion") != null) { %>
                <div class="alert alert-info">Inicie sesión para acceder a las funcionalidades del sistema.</div>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="POST">
                <div class="form-group">
                    <label for="correo" class="form-label">Correo Institucional</label>
                    <input type="email" id="correo" name="correo" class="form-control"
                           placeholder="usuario@epn.edu.ec" value="<%= HtmlUtil.escape(correo) %>"
                           autocomplete="username" required autofocus>
                </div>

                <div class="form-group">
                    <label for="contrasena" class="form-label">Contraseña</label>
                    <input type="password" id="contrasena" name="contrasena" class="form-control"
                           placeholder="••••••••" autocomplete="current-password" required>
                </div>

                <button type="submit" class="btn btn-primary btn-block">Iniciar Sesión</button>
            </form>

            <p class="login-footer">Acceso exclusivo para Docentes y Estudiantes registrados.</p>
        </div>
    </main>

</body>
</html>
