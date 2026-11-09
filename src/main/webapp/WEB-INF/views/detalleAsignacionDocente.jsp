<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.AuditoriaResumenDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.dto.ResultadoDTO.DetalleResultadoDTO" %>
<%@ page import="com.gr4.owlaudit.model.Asignacion" %>
<%@ page import="com.gr4.owlaudit.model.Curso" %>
<%@ page import="com.gr4.owlaudit.model.NivelSeveridadEnum" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String ctx = request.getContextPath();
    Asignacion asignacion = (Asignacion) request.getAttribute("asignacion");
    Curso curso = (Curso) request.getAttribute("curso");
    @SuppressWarnings("unchecked")
    List<AuditoriaResumenDTO> listaEntregas = (List<AuditoriaResumenDTO>) request.getAttribute("listaEntregas");
    ResultadoDTO detalleAuditoria = (ResultadoDTO) request.getAttribute("detalleAuditoria");

    String nombreAsig = (asignacion != null && asignacion.getNombre() != null) ? asignacion.getNombre() : "Asignación";
    String nombreCurso = (curso != null && curso.getNombre() != null) ? curso.getNombre() : "Curso";
    String codigoCurso = (curso != null && curso.getCodigo() != null) ? curso.getCodigo() : "";
    Double umbral = (asignacion != null && asignacion.getUmbralAprobacion() != null) ? asignacion.getUmbralAprobacion() : 70.0;
    String fechaLimite = (asignacion != null && asignacion.getFechaLimite() != null) ? asignacion.getFechaLimiteFormateada() : "Sin fecha límite";
    Long idCurso = (curso != null) ? curso.getId() : null;
    Long idAsignacion = (asignacion != null) ? asignacion.getId() : null;
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Entregas de Asignación: <%= HtmlUtil.escape(nombreAsig) %></title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
    <link rel="stylesheet" href="<%= ctx %>/css/styles.css">
    <style>
        .badge-dictamen-aprobada {
            background-color: #DCFCE7;
            color: #166534;
            border: 1px solid #BBF7D0;
            padding: 0.25rem 0.65rem;
            border-radius: 999px;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
        }
        .badge-dictamen-reprobada {
            background-color: #FEE2E2;
            color: #991B1B;
            border: 1px solid #FECACA;
            padding: 0.25rem 0.65rem;
            border-radius: 999px;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
        }
        .header-actions {
            display: flex;
            gap: 0.5rem;
            align-items: center;
            flex-wrap: wrap;
        }
        .info-pill {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            background: #F1F5F9;
            padding: 0.35rem 0.75rem;
            border-radius: 6px;
            font-size: 0.9rem;
            color: #334155;
        }
        .alert-info-empty {
            background-color: #EFF6FF;
            color: #1E40AF;
            border: 1px solid #BFDBFE;
            padding: 1.25rem;
            border-radius: 8px;
            display: flex;
            align-items: center;
            gap: 0.75rem;
            margin-top: 1rem;
        }
        .detail-box {
            background: #FFFFFF;
            border: 2px solid #3B82F6;
            border-radius: 8px;
            padding: 1.5rem;
            margin-top: 2rem;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
        }
    </style>
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container" style="max-width: 1200px; margin: 2rem auto; padding: 0 1rem;">
        
        <!-- Navegación Superior / Breadcrumbs -->
        <div style="margin-bottom: 1rem; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 0.5rem;">
            <div>
                <% if (idCurso != null) { %>
                    <a href="<%= ctx %>/asignaciones?idCurso=<%= idCurso %>" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                        &larr; Volver a Gestión de Asignaciones
                    </a>
                <% } else { %>
                    <a href="<%= ctx %>/curso" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                        &larr; Volver a Mis Cursos
                    </a>
                <% } %>
            </div>
            <div class="header-actions">
                <% if (listaEntregas != null && !listaEntregas.isEmpty()) { %>
                    <button type="button" class="btn btn-secondary btn-sm" onclick="exportarCSV()">
                        📥 Descargar CSV de Calificaciones
                    </button>
                <% } %>
                <a href="<%= ctx %>/curso" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                    🏠 Dashboard
                </a>
            </div>
        </div>

        <!-- Tarjeta de Información de la Asignación -->
        <div class="card" style="background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-bottom: 1.5rem;">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                <div>
                    <span class="badge badge-docente" style="margin-bottom: 0.5rem;">
                        <%= HtmlUtil.escape(codigoCurso) %> - <%= HtmlUtil.escape(nombreCurso) %>
                    </span>
                    <h1 class="card-title" style="margin: 0.25rem 0 0.5rem 0; font-size: 1.6rem; color: #0F172A;">
                        Entregas y Auditorías: <%= HtmlUtil.escape(nombreAsig) %>
                    </h1>
                    <% if (asignacion != null && asignacion.getDescripcion() != null && !asignacion.getDescripcion().isBlank()) { %>
                        <p style="color: #64748B; margin: 0 0 1rem 0;">
                            <%= HtmlUtil.escape(asignacion.getDescripcion()) %>
                        </p>
                    <% } %>
                </div>
            </div>

            <!-- Metadatos de la asignación -->
            <div style="display: flex; gap: 1rem; flex-wrap: wrap; margin-top: 0.75rem;">
                <div class="info-pill">
                    🎯 <strong>Umbral de Aprobación:</strong> <%= umbral %>%
                </div>
                <div class="info-pill">
                    📅 <strong>Fecha Límite:</strong> <%= HtmlUtil.escape(fechaLimite) %>
                </div>
                <div class="info-pill">
                    📊 <strong>Total de Entregas:</strong> <%= (listaEntregas != null) ? listaEntregas.size() : 0 %>
                </div>
            </div>
        </div>

        <!-- CA-2: Despliegue de Detalle Individual de Hallazgos (si se seleccionó una auditoría) -->
        <% if (detalleAuditoria != null) { %>
            <div id="seccionDetalle" class="detail-box">
                <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #E2E8F0; padding-bottom: 1rem; margin-bottom: 1rem;">
                    <div>
                        <h2 style="margin: 0; font-size: 1.3rem; color: #1E3A8A;">
                            🔍 Inspección Detallada de Entrega - Auditoría #<%= detalleAuditoria.getIdAuditoria() %>
                        </h2>
                        <span style="font-size: 0.9rem; color: #64748B;">
                            Desglose de reglas técnicas cumplidas, severidades y evidencias encontradas.
                        </span>
                    </div>
                    <a href="<%= ctx %>/docente/asignacion/entregas?idAsignacion=<%= idAsignacion %>" class="btn btn-secondary btn-sm" style="text-decoration: none;">
                        ✕ Cerrar Detalle
                    </a>
                </div>

                <div style="display: flex; gap: 1rem; margin-bottom: 1.25rem; flex-wrap: wrap;">
                    <div class="info-pill">
                        <strong>Calificación:</strong> <%= detalleAuditoria.getPorcentaje() %>% (<%= detalleAuditoria.getPuntajeObtenido() %>/<%= detalleAuditoria.getPuntajeMaximo() %> pts)
                    </div>
                    <div class="info-pill">
                        <strong>Dictamen:</strong>
                        <% if ("APROBADA".equalsIgnoreCase(detalleAuditoria.getDictamen())) { %>
                            <span class="badge-dictamen-aprobada">✓ APROBADA</span>
                        <% } else { %>
                            <span class="badge-dictamen-reprobada">✗ REPROBADA</span>
                        <% } %>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table data-table" style="width: 100%;">
                        <thead>
                            <tr style="background: #F8FAFC; text-align: left;">
                                <th style="padding: 0.75rem;">Regla de Evaluación</th>
                                <th style="padding: 0.75rem; text-align: center;">Estado</th>
                                <th style="padding: 0.75rem; text-align: center;">Severidad</th>
                                <th style="padding: 0.75rem; text-align: center;">Puntaje</th>
                                <th style="padding: 0.75rem;">Evidencias Detectadas</th>
                                <th style="padding: 0.75rem;">Recomendación Técnica</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (detalleAuditoria.getDetalles() != null && !detalleAuditoria.getDetalles().isEmpty()) {
                                    for (DetalleResultadoDTO det : detalleAuditoria.getDetalles()) {
                                        String severidadStr = (det.getNivelSeveridad() != null) ? det.getNivelSeveridad().name() : "MEDIA";
                                        String badgeClass = "badge-media";
                                        if ("ALTA".equalsIgnoreCase(severidadStr)) badgeClass = "badge-alta";
                                        else if ("BAJA".equalsIgnoreCase(severidadStr)) badgeClass = "badge-baja";
                            %>
                                <tr style="border-bottom: 1px solid #F1F5F9;">
                                    <td style="padding: 0.75rem; font-weight: 500;">
                                        <%= HtmlUtil.escape(det.getRegla()) %>
                                    </td>
                                    <td style="padding: 0.75rem; text-align: center;">
                                        <% if (det.isCumple()) { %>
                                            <span class="badge-dictamen-aprobada" style="font-size: 0.8rem;">✓ Cumple</span>
                                        <% } else { %>
                                            <span class="badge-dictamen-reprobada" style="font-size: 0.8rem;">✗ Incumple</span>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.75rem; text-align: center;">
                                        <span class="badge <%= badgeClass %>" style="font-size: 0.8rem;">
                                            <%= HtmlUtil.escape(severidadStr) %>
                                        </span>
                                    </td>
                                    <td style="padding: 0.75rem; text-align: center; font-weight: 600;">
                                        <%= det.getPuntosObtenidos() %> / <%= det.getPonderacion() %> pts
                                    </td>
                                    <td style="padding: 0.75rem; color: #475569; font-size: 0.9rem;">
                                        <%= (det.getEvidencia() != null && !det.getEvidencia().isBlank()) ? HtmlUtil.escape(det.getEvidencia()) : "<em>Sin evidencias de incumplimiento</em>" %>
                                    </td>
                                    <td style="padding: 0.75rem; color: #475569; font-size: 0.9rem;">
                                        <%= (det.getRecomendacion() != null && !det.getRecomendacion().isBlank()) ? HtmlUtil.escape(det.getRecomendacion()) : "-" %>
                                    </td>
                                </tr>
                            <%
                                    }
                                } else {
                            %>
                                <tr>
                                    <td colspan="6" style="text-align: center; padding: 1rem; color: #64748B;">
                                        No hay detalles registrados para esta auditoría.
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>

        <!-- CA-1.2: Escenario Negativo - Si ningún estudiante ha entregado -->
        <% if (listaEntregas == null || listaEntregas.isEmpty()) { %>
            <div class="card" style="background: white; border-radius: 8px; padding: 2rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 1.5rem;">
                <div class="alert-info-empty">
                    <span style="font-size: 1.8rem;">ℹ️</span>
                    <div>
                        <h3 style="margin: 0 0 0.25rem 0; font-size: 1.1rem; color: #1E3A8A;">Aún no existen entregas para esta asignación</h3>
                        <p style="margin: 0; color: #1E40AF; font-size: 0.95rem;">
                            Ningún estudiante ha enviado su repositorio para auditoría bajo esta asignación académica. Las entregas aparecerán automáticamente aquí cuando los alumnos ejecuten su auditoría.
                        </p>
                    </div>
                </div>

                <div class="table-responsive" style="margin-top: 1.5rem; opacity: 0.6;">
                    <table class="table data-table" style="width: 100%;">
                        <thead>
                            <tr style="background: #F8FAFC; text-align: left;">
                                <th style="padding: 0.75rem;">Estudiante</th>
                                <th style="padding: 0.75rem;">Proyecto / Repositorio</th>
                                <th style="padding: 0.75rem; text-align: center;">Fecha de Envío</th>
                                <th style="padding: 0.75rem; text-align: center;">Intento</th>
                                <th style="padding: 0.75rem; text-align: center;">Calificación</th>
                                <th style="padding: 0.75rem; text-align: center;">Dictamen</th>
                                <th style="padding: 0.75rem; text-align: center;">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td colspan="7" style="text-align: center; padding: 2rem; color: #64748B;">
                                    Tabla vacía: Esperando envíos de los estudiantes matriculados.
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } else { %>
            <!-- CA-1.1: Escenario Positivo - Despliegue del listado de entregas -->
            <div class="card" style="background: white; border-radius: 8px; padding: 1.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.1); margin-top: 1.5rem;">
                <h2 class="section-title" style="margin-top: 0; margin-bottom: 1rem; font-size: 1.25rem; color: #0F172A;">
                    Historial de Entregas de los Estudiantes (<%= listaEntregas.size() %>)
                </h2>

                <div class="table-responsive">
                    <table id="tablaEntregasDocente" class="table data-table" style="width: 100%; border-collapse: collapse;">
                        <thead>
                            <tr style="background: #F8FAFC; text-align: left; border-bottom: 2px solid #E2E8F0;">
                                <th style="padding: 0.85rem 0.75rem;">Estudiante</th>
                                <th style="padding: 0.85rem 0.75rem;">Proyecto / Repositorio</th>
                                <th style="padding: 0.85rem 0.75rem; text-align: center;">Fecha de Envío</th>
                                <th style="padding: 0.85rem 0.75rem; text-align: center;">Intento</th>
                                <th style="padding: 0.85rem 0.75rem; text-align: center;">Calificación</th>
                                <th style="padding: 0.85rem 0.75rem; text-align: center;">Dictamen</th>
                                <th style="padding: 0.85rem 0.75rem; text-align: center;">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                for (AuditoriaResumenDTO entrega : listaEntregas) {
                                    String nombreEst = (entrega.getNombreEstudiante() != null && !entrega.getNombreEstudiante().isBlank()) 
                                            ? entrega.getNombreEstudiante() : "Estudiante";
                                    String correoEst = (entrega.getCorreoEstudiante() != null) ? entrega.getCorreoEstudiante() : "";
                                    String nombreProy = (entrega.getNombreProyecto() != null && !entrega.getNombreProyecto().isBlank())
                                            ? entrega.getNombreProyecto() : "Repositorio";
                                    String urlRepo = (entrega.getUrlRepositorio() != null) ? entrega.getUrlRepositorio() : "";
                                    Long intentoNum = (entrega.getNumeroEnAsignacion() != null) ? entrega.getNumeroEnAsignacion() : 1L;
                                    boolean aprobada = "APROBADA".equalsIgnoreCase(entrega.getDictamen());
                            %>
                                <tr style="border-bottom: 1px solid #F1F5F9;">
                                    <td style="padding: 0.85rem 0.75rem;">
                                        <strong><%= HtmlUtil.escape(nombreEst) %></strong>
                                        <% if (!correoEst.isBlank()) { %>
                                            <div style="font-size: 0.82rem; color: #64748B;"><%= HtmlUtil.escape(correoEst) %></div>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem;">
                                        <div><%= HtmlUtil.escape(nombreProy) %></div>
                                        <% if (!urlRepo.isBlank()) { %>
                                            <a href="<%= HtmlUtil.escape(urlRepo) %>" target="_blank" rel="noopener noreferrer" style="font-size: 0.82rem; color: #2563EB; text-decoration: none; word-break: break-all;">
                                                🔗 <%= HtmlUtil.escape(urlRepo) %>
                                            </a>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem; text-align: center; color: #475569; font-size: 0.9rem;">
                                        <%= HtmlUtil.escape(entrega.getFechaFormateada()) %>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem; text-align: center;">
                                        <span class="badge badge-muted">
                                            # <%= intentoNum %>
                                        </span>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem; text-align: center;">
                                        <strong style="font-size: 1.05rem;"><%= String.format("%.1f", entrega.getPorcentaje()) %>%</strong>
                                        <div style="font-size: 0.78rem; color: #64748B;">
                                            <%= entrega.getPuntajeObtenido() %>/<%= entrega.getPuntajeMaximo() %> pts
                                        </div>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem; text-align: center;">
                                        <% if (aprobada) { %>
                                            <span class="badge-dictamen-aprobada">✓ APROBADA</span>
                                        <% } else { %>
                                            <span class="badge-dictamen-reprobada">✗ REPROBADA</span>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.85rem 0.75rem; text-align: center;">
                                        <a href="<%= ctx %>/docente/asignacion/entregas?idAsignacion=<%= idAsignacion %>&idAuditoria=<%= entrega.getId() %>#seccionDetalle" 
                                           class="btn btn-primary btn-sm" 
                                           style="text-decoration: none; padding: 0.35rem 0.75rem; font-size: 0.85rem; border-radius: 4px; display: inline-flex; align-items: center; gap: 0.3rem;">
                                            👁️ Ver Detalle
                                        </a>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>

    </main>

    <!-- Script para Exportación a CSV -->
    <script>
        function exportarCSV() {
            const table = document.getElementById("tablaEntregasDocente");
            if (!table) {
                alert("No hay datos para exportar.");
                return;
            }

            let csv = [];
            const rows = table.querySelectorAll("tr");

            for (let i = 0; i < rows.length; i++) {
                let row = [], cols = rows[i].querySelectorAll("th, td");
                // Exportar todas las columnas excepto la última (Acciones)
                for (let j = 0; j < cols.length - 1; j++) {
                    let data = cols[j].innerText.replace(/(\r\n|\n|\r)/gm, " ").trim();
                    data = data.replace(/"/g, '""');
                    row.push('"' + data + '"');
                }
                csv.push(row.join(","));
            }

            const csvString = csv.join("\n");
            const blob = new Blob(["\uFEFF" + csvString], { type: "text/csv;charset=utf-8;" });
            const link = document.createElement("a");
            const url = URL.createObjectURL(blob);
            link.setAttribute("href", url);
            link.setAttribute("download", "entregas_<%= HtmlUtil.escape(nombreAsig).replace(' ', '_') %>.csv");
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
        }
    </script>
</body>
</html>

