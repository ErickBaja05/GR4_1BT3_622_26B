<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Registrar Regla de Calidad</title>
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
            <h1 class="card-title">Configurar Regla de Evaluación Técnica</h1>
            <p class="card-subtitle">Cree políticas de calidad de código para ser evaluadas automáticamente en los repositorios.</p>

            <% 
                String mensajeExito = (String) request.getAttribute("mensajeExito");
                if (mensajeExito != null) { 
            %>
                <div class="alert alert-success">
                    <%= mensajeExito %>
                </div>
            <% } %>

            <form action="regla" method="POST">
                <div class="form-group">
                    <label for="tipoMotor" class="form-label">Tipo de Motor de Evaluación</label>
                    <select id="tipoMotor" name="tipoMotor" class="form-control" required>
                        <option value="PRESENCIA_ARCHIVO">Presencia de Archivo</option>
                        <option value="RESTRICCION_ARCHIVOS">Restricción de Archivos</option>
                        <option value="ESTRUCTURA_CARPETAS">Estructura de Carpetas</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="nombreRepresentativo" class="form-label">Nombre Representativo</label>
                    <input type="text" id="nombreRepresentativo" name="nombreRepresentativo" class="form-control" 
                           placeholder="Ej. Verificar archivo README.md" required>
                </div>

                <div class="form-group">
                    <label for="descripcionDetallada" class="form-label">Descripción Detallada</label>
                    <textarea id="descripcionDetallada" name="descripcionDetallada" class="form-control" rows="3" 
                              placeholder="Explique el propósito de la regla..." required></textarea>
                </div>

                <div class="form-group">
                    <label for="parametroExacto" class="form-label">Parámetro Exacto (Ruta / Expresión / Archivo)</label>
                    <input type="text" id="parametroExacto" name="parametroExacto" class="form-control" 
                           placeholder="Ej. /README.md o src/main/java" required>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label for="nivelSeveridad" class="form-label">Nivel de Severidad</label>
                        <select id="nivelSeveridad" name="nivelSeveridad" class="form-control" required>
                            <option value="ALTA">ALTA</option>
                            <option value="MEDIA">MEDIA</option>
                            <option value="BAJA">BAJA</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="ponderacion" class="form-label">Ponderación (Puntos)</label>
                        <input type="number" id="ponderacion" name="ponderacion" class="form-control" 
                               min="1" max="100" placeholder="Ej. 20" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary">Registrar Regla</button>
            </form>
        </div>

        <% 
            NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");
            if (dto != null) { 
        %>
            <div class="card" style="border-left: 4px solid var(--accent-gold);">
                <h3 style="margin-bottom: 0.5rem;">🔍 Verificación del DTO (Capa Presentación)</h3>
                <p><strong>Motor:</strong> <%= dto.getTipoMotor() %></p>
                <p><strong>Nombre:</strong> <%= dto.getNombreRepresentativo() %></p>
                <p><strong>Parámetro:</strong> <%= dto.getParametroExacto() %></p>
                <p><strong>Severidad:</strong> <%= dto.getNivelSeveridad() %> | <strong>Puntos:</strong> <%= dto.getPonderacion() %></p>
            </div>
        <% } %>
    </main>

</body>
</html>