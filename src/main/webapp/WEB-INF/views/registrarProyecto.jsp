<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.NuevoProyectoDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Registrar Proyecto</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <header class="navbar">
        <div class="brand">🦉 OwlAudit</div>
        <nav>
            <a href="proyecto">Registrar Proyecto</a>
            <a href="regla">Reglas de Calidad</a>
            <a href="auditoria">Auditar Repositorio</a>
        </nav>
    </header>

    <main class="container">
        <div class="card">
            <h1 class="card-title">Registrar Nuevo Proyecto Académico</h1>
            <p class="card-subtitle">Vincule su repositorio de GitHub para habilitar las auditorías de código.</p>

            <% 
                String mensajeExito = (String) request.getAttribute("mensajeExito");
                if (mensajeExito != null) { 
            %>
                <div class="alert alert-success">
                    <%= mensajeExito %>
                </div>
            <% } %>

            <form action="proyecto" method="POST">
                <div class="form-group">
                    <label for="nombre" class="form-label">Nombre del Proyecto</label>
                    <input type="text" id="nombre" name="nombre" class="form-control" 
                           placeholder="Ej. Sistema de Reservas - Grupo 4" required>
                </div>

                <div class="form-group">
                    <label for="urlGithub" class="form-label">URL del Repositorio de GitHub</label>
                    <input type="url" id="urlGithub" name="urlGithub" class="form-control" 
                           placeholder="Ej. https://github.com/usuario/repositorio" required>
                </div>

                <button type="submit" class="btn btn-primary">Registrar Proyecto</button>
            </form>
        </div>

        <% 
            NuevoProyectoDTO dto = (NuevoProyectoDTO) request.getAttribute("proyectoDTO");
            if (dto != null) { 
        %>
            <div class="card" style="border-left: 4px solid var(--brand-blue);">
                <h3 style="margin-bottom: 0.5rem;">🔍 Verificación del DTO (Capa Presentación)</h3>
                <p><strong>Nombre:</strong> <%= dto.getNombre() %></p>
                <p><strong>URL GitHub:</strong> <%= dto.getUrlGithub() %></p>
            </div>
        <% } %>
    </main>

</body>
</html>