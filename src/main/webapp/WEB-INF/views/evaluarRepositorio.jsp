<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.AuditoriaDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResumenDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Auditar Repositorio</title>
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
            <h1 class="card-title">Auditoría Evaluativa de Repositorio</h1>
            <p class="card-subtitle">Ejecute la inspección automatizada de reglas sobre la estructura de GitHub.</p>

            <% 
                String mensajeExito = (String) request.getAttribute("mensajeExito");
                if (mensajeExito != null) { 
            %>
                <div class="alert alert-success">
                    <%= mensajeExito %>
                </div>
            <% } %>

            <form action="auditoria" method="POST">
                <div class="form-group">
                    <label for="urlGithub" class="form-label">URL del Repositorio a Auditar</label>
                    <input type="url" id="urlGithub" name="urlGithub" class="form-control" 
                           placeholder="https://github.com/usuario/repositorio" 
                           value="<%= request.getAttribute("auditoriaDTO") != null ? ((AuditoriaDTO)request.getAttribute("auditoriaDTO")).getUrl() : "" %>" required>
                </div>

                <div style="display: flex; gap: 1rem;">
                    <button type="submit" class="btn btn-primary">Ejecutar Auditoría</button>
                </div>
            </form>
        </div>

        <!-- Sección de Resumen Previo (Si aplica) -->
        <% 
            ResumenDTO resumen = (ResumenDTO) request.getAttribute("resumenDTO");
            if (resumen != null) { 
        %>
            <div class="card" style="border-left: 4px solid var(--brand-blue);">
                <h3>📋 Previa de Auditoría</h3>
                <p><strong>Puntaje Máximo Alcanzable:</strong> <%= resumen.getPuntajeMaximo() %> pts</p>
            </div>
        <% } %>

        <!-- Sección de Resultados Finales -->
        <% 
            ResultadoDTO resultado = (ResultadoDTO) request.getAttribute("resultadoDTO");
            if (resultado != null) { 
        %>
            <div class="card" style="background: #F0FDF4; border: 1px solid #BBF7D0;">
                <h2 style="color: #166534; margin-bottom: 1rem;">🎯 Resultado de la Evaluación</h2>
                <p style="font-size: 1.1rem; margin-bottom: 0.5rem;">
                    <strong>Puntaje Obtenido:</strong> <%= resultado.getPuntajeObtenido() %> / <%= resultado.getPuntajeMaximo() %> pts
                </p>
                <p style="font-size: 1.1rem; color: #15803D;">
                    <strong>Cumplimiento de Calidad:</strong> <%= resultado.getPorcentaje() %>%
                </p>
            </div>
        <% } %>
    </main>

</body>
</html>