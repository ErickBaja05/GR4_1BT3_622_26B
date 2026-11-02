<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.gr4.owlaudit.dto.SolicitudDTO" %>
<%@ page import="com.gr4.owlaudit.model.Proyecto" %>
<%@ page import="com.gr4.owlaudit.model.Auditoria" %>
<%@ page import="com.gr4.owlaudit.model.Asignacion" %>
<%@ page import="com.gr4.owlaudit.model.SolicitudRetroalimentacion" %>
<%@ page import="com.gr4.owlaudit.model.EstadoRetroEnum" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    String mensajeInfo = (String) request.getAttribute("mensajeInfo");
    SolicitudDTO solicitudDTO = (SolicitudDTO) request.getAttribute("solicitudDTO");

    Long hallazgoId = (solicitudDTO != null && solicitudDTO.getHallazgoId() != null) ? solicitudDTO.getHallazgoId() : null;
    String justificacion = (solicitudDTO != null && solicitudDTO.getJustificacion() != null) ? solicitudDTO.getJustificacion() : "";
    String reglaNombre = (String) request.getAttribute("reglaNombre");
    String severidad = (String) request.getAttribute("severidad");
    String evidencia = (String) request.getAttribute("evidencia");
    String recomendacion = (String) request.getAttribute("recomendacion");
    String proyectoNombre = (String) request.getAttribute("nombreProyecto");
    String urlGithub = (String) request.getAttribute("urlGithub");
    Long auditoriaId = (Long) request.getAttribute("auditoriaId");
    String fechaAuditoria = (String) request.getAttribute("fechaAuditoria");
    String codigoVisibleAuditoria = (String) request.getAttribute("codigoVisibleAuditoria");
    String tipoAuditoria = (String) request.getAttribute("tipoAuditoria");
    String nombreAsignacion = (String) request.getAttribute("nombreAsignacion");
    SolicitudRetroalimentacion solicitudExistente = (SolicitudRetroalimentacion) request.getAttribute("solicitudExistente");

    @SuppressWarnings("unchecked")
    List<Proyecto> proyectosDisponibles = (List<Proyecto>) request.getAttribute("proyectosDisponibles");
    Long proyectoIdFiltro = (Long) request.getAttribute("proyectoIdFiltro");
    String tipoAuditoriaFiltro = (String) request.getAttribute("tipoAuditoriaFiltro");
    Long asignacionIdFiltro = (Long) request.getAttribute("asignacionIdFiltro");
    Long auditoriaIdFiltro = (Long) request.getAttribute("auditoriaIdFiltro");

    @SuppressWarnings("unchecked")
    List<Asignacion> asignacionesDisponibles = (List<Asignacion>) request.getAttribute("asignacionesDisponibles");

    @SuppressWarnings("unchecked")
    List<Auditoria> auditoriasDisponibles = (List<Auditoria>) request.getAttribute("auditoriasDisponibles");

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> hallazgosDisponibles = (List<Map<String, Object>>) request.getAttribute("hallazgosDisponibles");

    @SuppressWarnings("unchecked")
    List<SolicitudRetroalimentacion> misSolicitudes = (List<SolicitudRetroalimentacion>) request.getAttribute("misSolicitudes");
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Solicitar Retroalimentación (CU05)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        
        <!-- Encabezado del Caso de Uso CU05 -->
        <div class="card">
            <h1 class="card-title">💬 Solicitar Retroalimentación de Hallazgo</h1>
            <p class="card-subtitle">
                <strong>CU05:</strong> Consulte a su docente sobre un hallazgo técnico observado en el reporte de auditoría de su proyecto, sustentando su justificación técnica o duda de implementación.
            </p>

            <% if (mensajeExito != null) { %>
                <div class="alert alert-success">
                    <strong>¡Éxito!</strong> <%= HtmlUtil.escape(mensajeExito) %>
                </div>
            <% } %>
            <% if (mensajeError != null) { %>
                <div class="alert alert-error">
                    <strong>Atención:</strong> <%= HtmlUtil.escape(mensajeError) %>
                </div>
            <% } %>
            <% if (mensajeInfo != null) { %>
                <div class="alert alert-info">
                    <strong>Aviso:</strong> <%= HtmlUtil.escape(mensajeInfo) %>
                </div>
            <% } %>

            <!-- SECCIÓN FORMULARIO (bForm): Si se ha especificado un hallazgo observado -->
            <% if (hallazgoId != null) { %>
                <div class="context-box finding-selected" style="margin-top: 1rem;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 0.5rem;">
                        <div>
                            <h3 style="margin: 0 0 0.5rem; color: #1E40AF;">
                                🔍 Hallazgo Seleccionado: #<%= hallazgoId %>
                                <% if (severidad != null && !severidad.isBlank()) { %>
                                    &middot; <span class="badge badge-<%= severidad.toLowerCase() %>">Severidad <%= HtmlUtil.escape(severidad) %></span>
                                <% } %>
                            </h3>
                            <% if (proyectoNombre != null && !proyectoNombre.isBlank()) { %>
                                <p style="margin: 0.25rem 0;"><strong>📁 Proyecto:</strong> <%= HtmlUtil.escape(proyectoNombre) %></p>
                            <% } %>
                            <% if (urlGithub != null && !urlGithub.isBlank()) { %>
                                <p style="margin: 0.25rem 0;"><strong>🔗 URL Repositorio:</strong> 
                                    <a href="<%= HtmlUtil.escape(urlGithub) %>" target="_blank" rel="noopener noreferrer" style="color: var(--brand-blue); text-decoration: underline;">
                                        <%= HtmlUtil.escape(urlGithub) %> ↗
                                    </a>
                                </p>
                            <% } %>
                            <% if (auditoriaId != null) { %>
                                <p style="margin: 0.25rem 0;">
                                    <strong>📋 Reporte Auditoría:</strong> 
                                    <span class="badge <%= "ASIGNACION".equalsIgnoreCase(tipoAuditoria) ? "badge-alta" : "badge-media" %>">
                                        <%= codigoVisibleAuditoria != null ? HtmlUtil.escape(codigoVisibleAuditoria) : ("#" + auditoriaId) %>
                                    </span>
                                    <% if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria) && nombreAsignacion != null) { %>
                                        &middot; <strong>📚 Asignación:</strong> <%= HtmlUtil.escape(nombreAsignacion) %>
                                    <% } %>
                                    <%= fechaAuditoria != null ? "(" + HtmlUtil.escape(fechaAuditoria) + ")" : "" %>
                                </p>
                            <% } %>
                            <% if (reglaNombre != null && !reglaNombre.isBlank()) { %>
                                <p style="margin: 0.25rem 0;"><strong>📐 Regla Evaluada:</strong> <%= HtmlUtil.escape(reglaNombre) %></p>
                            <% } %>
                            <% if (evidencia != null && !evidencia.isBlank()) { %>
                                <p style="margin: 0.25rem 0;"><strong>⚠️ Evidencia Detectada:</strong> <span class="evidence-code"><%= HtmlUtil.escape(evidencia) %></span></p>
                            <% } %>
                            <% if (recomendacion != null && !recomendacion.isBlank()) { %>
                                <p style="margin: 0.25rem 0; color: #475569;"><strong>💡 Recomendación Técnica:</strong> <%= HtmlUtil.escape(recomendacion) %></p>
                            <% } %>
                        </div>
                        <div>
                            <%
                                String returnFilters = "";
                                if (proyectoIdFiltro != null) returnFilters += "&proyectoId=" + proyectoIdFiltro;
                                if (tipoAuditoriaFiltro != null && !tipoAuditoriaFiltro.isBlank()) returnFilters += "&tipoAuditoria=" + HtmlUtil.escape(tipoAuditoriaFiltro);
                                if (asignacionIdFiltro != null) returnFilters += "&asignacionId=" + asignacionIdFiltro;
                                if (auditoriaIdFiltro != null) returnFilters += "&auditoriaId=" + auditoriaIdFiltro;
                            %>
                            <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar<%= returnFilters %>" 
                               class="btn btn-secondary" style="font-size: 0.85rem; padding: 0.4rem 0.8rem;">
                                🔄 Cambiar de Hallazgo
                            </a>
                        </div>
                    </div>
                </div>

                <% if (solicitudExistente != null) { %>
                    <div class="alert alert-<%= solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA ? "success" : "info" %>" style="margin-top: 1.25rem;">
                        <h4 style="margin: 0 0 0.5rem;">
                            <%= solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA ? "✅ Solicitud Atendida por el Docente" : "⏳ Hallazgo en Proceso de Revisión" %>
                        </h4>
                        <p style="margin: 0.25rem 0;">
                            <strong>Su Justificación enviada:</strong> "<%= HtmlUtil.escape(solicitudExistente.getJustificacion() != null ? solicitudExistente.getJustificacion() : "") %>"
                        </p>
                        <% if (solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA && solicitudExistente.getOrientacionTecnica() != null) { %>
                            <div style="background: white; border: 1px solid #86EFAC; border-radius: 6px; padding: 0.85rem; margin-top: 0.75rem;">
                                <strong style="color: #166534;">👨‍🏫 Orientación Técnica del Docente:</strong>
                                <p style="margin: 0.5rem 0 0; font-size: 1rem; color: #1E293B;">
                                    <%= HtmlUtil.escape(solicitudExistente.getOrientacionTecnica()) %>
                                </p>
                            </div>
                        <% } else { %>
                            <p style="margin: 0.5rem 0 0; color: #64748B; font-style: italic;">
                                Su solicitud se encuentra pendiente de respuesta por parte del docente.
                            </p>
                        <% } %>
                    </div>
                <% } %>

                <% if (solicitudExistente == null || solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA) { %>
                    <!-- Formulario de Justificación Técnica (secuencia5.puml & robustez5.puml bForm) -->
                    <form action="${pageContext.request.contextPath}/retroalimentacion" method="POST" style="margin-top: 1.25rem;"
                          onsubmit="var btn=this.querySelector('button[type=submit]'); if(this.submitted) return false; this.submitted=true; if(btn){btn.disabled=true; btn.innerText='Enviando...';}">
                        <input type="hidden" name="accion" value="solicitar">
                        <input type="hidden" name="hallazgoId" value="<%= hallazgoId %>">
                        <% if (reglaNombre != null) { %><input type="hidden" name="reglaNombre" value="<%= HtmlUtil.escape(reglaNombre) %>"><% } %>
                        <% if (severidad != null) { %><input type="hidden" name="severidad" value="<%= HtmlUtil.escape(severidad) %>"><% } %>

                        <% if (solicitudExistente != null && solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA) { %>
                            <div class="alert alert-info" style="margin-bottom: 1rem;">
                                <strong>💡 Re-consulta de Retroalimentación:</strong> ¿Tiene dudas sobre la orientación técnica recibida? Ingrese una nueva consulta para continuar el seguimiento de este hallazgo.
                            </div>
                        <% } %>

                        <div class="form-group">
                            <label for="justificacion" class="form-label">
                                <%= (solicitudExistente != null && solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA) 
                                        ? "Nueva Justificación Técnica o Re-consulta sobre el Hallazgo" 
                                        : "Justificación Técnica o Consulta sobre el Hallazgo" %> <span style="color: #DC2626;">*</span>
                            </label>
                            <div class="textarea-wrapper">
                                <textarea id="justificacion" name="justificacion" class="form-control" rows="5" 
                                          placeholder="Explique detalladamente las razones técnicas, excepciones de arquitectura o dudas sobre este hallazgo (mínimo 10 caracteres, máximo 1000)..."
                                          minlength="10" maxlength="1000" required oninput="actualizarContador(this)"><%= HtmlUtil.escape(justificacion) %></textarea>
                                <div class="char-counter" id="contadorChar">0 / 1000 caracteres (mínimo 10)</div>
                            </div>
                        </div>

                        <div style="display: flex; gap: 1rem; align-items: center; margin-top: 1.5rem;">
                            <button type="submit" class="btn btn-primary">
                                <%= (solicitudExistente != null && solicitudExistente.getEstado() == EstadoRetroEnum.ATENDIDA) ? "📤 Enviar Re-consulta" : "📤 Enviar Solicitud" %>
                            </button>
                            <button type="submit" name="cancelar" value="true" class="btn btn-secondary" formnovalidate>Cancelar</button>
                        </div>
                    </form>
                <% } %>

            <% } else { %>
                <!-- SECCIÓN CONSULTAR REPORTE DE AUDITORÍA (bReporte): Paso 1 y 2 de CU05 -->
                <div style="margin-top: 1rem;">
                    <h3 style="color: #1E293B; margin-bottom: 0.75rem;">📋 Paso 1: Consultar Reporte de Auditoría y Especificar Hallazgo</h3>
                    <p style="color: #64748B; font-size: 0.95rem; margin-bottom: 1.25rem;">
                        Seleccione el proyecto y examine los hallazgos generados en sus auditorías de calidad para solicitar la orientación del docente.
                    </p>

                    <!-- Barra de Filtros Multicriterio (Proyecto, Tipo de Auditoría, Asignación, Auditoría Específica) -->
                    <div class="filter-bar" style="background: #F8FAFC; padding: 1rem; border-radius: 8px; border: 1px solid #E2E8F0; margin-bottom: 1.5rem; display: flex; flex-wrap: wrap; gap: 1rem; align-items: flex-end;">
                        <!-- Filtro 1: Proyecto -->
                        <div style="display: flex; flex-direction: column; gap: 0.25rem; min-width: 220px; flex: 1;">
                            <label for="proyectoFiltro" style="font-size: 0.88rem; font-weight: 600; color: #334155;">Proyecto:</label>
                            <select id="proyectoFiltro" onchange="aplicarFiltros()" style="width: 100%;">
                                <option value="">-- Todos los Proyectos Registrados --</option>
                                <% if (proyectosDisponibles != null) {
                                       int idxSol = 1;
                                       for (Proyecto p : proyectosDisponibles) { 
                                           boolean sel = proyectoIdFiltro != null && proyectoIdFiltro.equals(p.getId());
                                           long idMostrado = (p.getIdEstudiante() != null) ? p.getIdEstudiante() : (long) idxSol;
                                %>
                                    <option value="<%= p.getId() %>" <%= sel ? "selected" : "" %>>
                                        #<%= idMostrado %> &middot; <%= HtmlUtil.escape(p.getNombre()) %>
                                    </option>
                                <%         idxSol++;
                                       }
                                   } %>
                            </select>
                        </div>

                        <!-- Filtro 2: Tipo de Auditoría -->
                        <div style="display: flex; flex-direction: column; gap: 0.25rem; min-width: 190px;">
                            <label for="tipoAuditoriaFiltro" style="font-size: 0.88rem; font-weight: 600; color: #334155;">Tipo de Auditoría:</label>
                            <select id="tipoAuditoriaFiltro" onchange="cambiarTipoAuditoria(this.value)" style="width: 100%;">
                                <option value="">-- Todos los Tipos --</option>
                                <option value="GENERAL" <%= "GENERAL".equalsIgnoreCase(tipoAuditoriaFiltro) ? "selected" : "" %>>🌐 Auditorías Generales</option>
                                <option value="ASIGNACION" <%= "ASIGNACION".equalsIgnoreCase(tipoAuditoriaFiltro) ? "selected" : "" %>>📚 Auditorías por Asignación</option>
                            </select>
                        </div>

                        <!-- Filtro 3: Asignación Académica (Dinámico) -->
                        <div id="grupoAsignacionFiltro" style="display: <%= "ASIGNACION".equalsIgnoreCase(tipoAuditoriaFiltro) ? "flex" : "none" %>; flex-direction: column; gap: 0.25rem; min-width: 220px; flex: 1;">
                            <label for="asignacionIdFiltro" style="font-size: 0.88rem; font-weight: 600; color: #334155;">Asignación Académica:</label>
                            <select id="asignacionIdFiltro" onchange="aplicarFiltros()" style="width: 100%;">
                                <option value="">-- Todas las Asignaciones --</option>
                                <% if (asignacionesDisponibles != null) {
                                       for (Asignacion asig : asignacionesDisponibles) {
                                           boolean sel = asignacionIdFiltro != null && asignacionIdFiltro.equals(asig.getId());
                                %>
                                    <option value="<%= asig.getId() %>" <%= sel ? "selected" : "" %>>
                                        <%= HtmlUtil.escape(asig.getNombre()) %> (Umbral: <%= asig.getUmbralAprobacion() %>%)
                                    </option>
                                <%     }
                                   } %>
                            </select>
                        </div>

                        <!-- Filtro 4: Auditoría Específica -->
                        <div style="display: flex; flex-direction: column; gap: 0.25rem; min-width: 240px; flex: 1.2;">
                            <label for="auditoriaIdFiltro" style="font-size: 0.88rem; font-weight: 600; color: #334155;">Auditoría Específica:</label>
                            <select id="auditoriaIdFiltro" onchange="aplicarFiltros()" style="width: 100%;">
                                <option value="">-- Todas las Auditorías --</option>
                                <% if (auditoriasDisponibles != null) {
                                       for (Auditoria audItem : auditoriasDisponibles) {
                                           boolean sel = auditoriaIdFiltro != null && auditoriaIdFiltro.equals(audItem.getId());
                                           String cod = audItem.getCodigoVisible();
                                           String fecha = audItem.getFechaHora() != null ? audItem.getFechaHora().toString().replace('T', ' ') : "";
                                           if (fecha.length() > 16) fecha = fecha.substring(0, 16);
                                           String asigBadge = (audItem.getAsignacion() != null) ? " [" + audItem.getAsignacion().getNombre() + "]" : "";
                                           String proyNom = (audItem.getProyecto() != null) ? " · " + audItem.getProyecto().getNombre() : "";
                                %>
                                    <option value="<%= audItem.getId() %>" <%= sel ? "selected" : "" %>>
                                        <%= cod != null ? cod : ("#" + audItem.getId()) %> &middot; <%= fecha %><%= asigBadge %><%= proyNom %>
                                    </option>
                                <%     }
                                   } %>
                            </select>
                        </div>

                        <!-- Botón Limpiar Filtros -->
                        <% if (proyectoIdFiltro != null || (tipoAuditoriaFiltro != null && !tipoAuditoriaFiltro.isBlank()) || asignacionIdFiltro != null || auditoriaIdFiltro != null) { %>
                            <div style="display: flex; align-items: flex-end;">
                                <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar" class="btn btn-secondary" style="font-size: 0.85rem; padding: 0.5rem 0.85rem;">
                                    ✕ Limpiar Filtros
                                </a>
                            </div>
                        <% } %>
                    </div>

                    <!-- Tabla de Hallazgos Disponibles -->
                    <% if (hallazgosDisponibles != null && !hallazgosDisponibles.isEmpty()) { %>
                        <div class="table-responsive">
                            <table class="data-table">
                                <thead>
                                    <tr>
                                        <th>Proyecto</th>
                                        <th>Auditoría</th>
                                        <th>Regla Incumplida</th>
                                        <th>Severidad</th>
                                        <th>Evidencia</th>
                                        <th>Estado Solicitud</th>
                                        <th style="text-align: center;">Acción</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% for (Map<String, Object> hMap : hallazgosDisponibles) { 
                                           boolean tieneSol = Boolean.TRUE.equals(hMap.get("tieneSolicitud"));
                                           String estSol = (String) hMap.get("estadoSolicitud");
                                           String sev = (String) hMap.get("severidad");
                                           String targetParams = "";
                                           if (proyectoIdFiltro != null) targetParams += "&proyectoId=" + proyectoIdFiltro;
                                           if (tipoAuditoriaFiltro != null && !tipoAuditoriaFiltro.isBlank()) targetParams += "&tipoAuditoria=" + HtmlUtil.escape(tipoAuditoriaFiltro);
                                           if (asignacionIdFiltro != null) targetParams += "&asignacionId=" + asignacionIdFiltro;
                                           if (auditoriaIdFiltro != null) targetParams += "&auditoriaId=" + auditoriaIdFiltro;
                                    %>
                                        <tr>
                                            <td><strong><%= HtmlUtil.escape((String) hMap.get("proyectoNombre")) %></strong></td>
                                            <td>
                                                <strong><%= hMap.get("codigoVisible") != null ? HtmlUtil.escape((String) hMap.get("codigoVisible")) : ("#" + hMap.get("auditoriaId")) %></strong>
                                                <% if ("ASIGNACION".equalsIgnoreCase((String) hMap.get("tipoAuditoria")) && hMap.get("asignacionNombre") != null) { %>
                                                    <br><span class="badge badge-alta" style="font-size: 0.72rem;"><%= HtmlUtil.escape((String) hMap.get("asignacionNombre")) %></span>
                                                <% } else { %>
                                                    <br><span class="badge badge-media" style="font-size: 0.72rem;">General</span>
                                                <% } %>
                                                <br><span style="font-size: 0.78rem; color: #64748B;"><%= HtmlUtil.escape((String) hMap.get("fechaHora")) %></span>
                                            </td>
                                            <td><%= HtmlUtil.escape((String) hMap.get("reglaNombre")) %></td>
                                            <td>
                                                <span class="badge badge-<%= sev != null ? sev.toLowerCase() : "media" %>">
                                                    <%= HtmlUtil.escape(sev != null ? sev : "MEDIA") %>
                                                </span>
                                            </td>
                                            <td>
                                                <span class="evidence-code" style="max-width: 180px; display: inline-block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
                                                    <%= HtmlUtil.escape((String) hMap.get("evidencia")) %>
                                                </span>
                                            </td>
                                            <td>
                                                <% if (!tieneSol) { %>
                                                    <span class="badge badge-corregido">Sin solicitud</span>
                                                <% } else if ("ATENDIDA".equalsIgnoreCase(estSol)) { %>
                                                    <span class="badge badge-atendida">✓ Atendida</span>
                                                <% } else { %>
                                                    <span class="badge badge-pendiente">⏳ En Revisión</span>
                                                <% } %>
                                            </td>
                                            <td class="text-center">
                                                <% if (!tieneSol) { %>
                                                    <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar&hallazgoId=<%= hMap.get("hallazgoId") %><%= targetParams %>" 
                                                       class="btn-feedback">
                                                        💬 Solicitar Feedback
                                                    </a>
                                                <% } else if ("ATENDIDA".equalsIgnoreCase(estSol)) { %>
                                                    <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar&hallazgoId=<%= hMap.get("hallazgoId") %><%= targetParams %>" 
                                                       class="btn-feedback btn-feedback-consultar">
                                                        ✓ Re-consultar Feedback
                                                    </a>
                                                <% } else { %>
                                                    <a href="${pageContext.request.contextPath}/retroalimentacion?accion=solicitar&hallazgoId=<%= hMap.get("hallazgoId") %><%= targetParams %>" 
                                                       class="btn-feedback btn-feedback-revision">
                                                        ⏳ En Revisión
                                                    </a>
                                                <% } %>
                                            </td>
                                        </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        </div>
                    <% } else { %>
                        <div class="alert alert-info">
                            <strong>Aviso:</strong> No se encontraron hallazgos técnicos registrados para los criterios de búsqueda seleccionados.
                            Para generar hallazgos sobre los cuales solicitar retroalimentación, primero debe 
                            <a href="${pageContext.request.contextPath}/auditoria"><strong>ejecutar una auditoría de repositorio</strong></a>.
                        </div>
                    <% } %>
                </div>
            <% } %>
        </div>

        <!-- SECCIÓN HISTORIAL DEL ESTUDIANTE: Visualización de Solicitudes y Respuestas Recibidas -->
        <div class="card" style="margin-top: 1.5rem;">
            <h2 style="color: #1E293B; font-size: 1.25rem; margin-bottom: 0.5rem;">📑 Mis Solicitudes de Retroalimentación Enviadas</h2>
            <p class="card-subtitle">
                Consulte el historial de sus solicitudes y las orientaciones técnicas emitidas por el cuerpo docente.
            </p>

            <% if (misSolicitudes != null && !misSolicitudes.isEmpty()) { %>
                <div class="table-responsive" style="margin-top: 1rem;">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th># Solicitud</th>
                                <th>Proyecto y Regla</th>
                                <th>Severidad</th>
                                <th>Justificación Enviada</th>
                                <th>Estado</th>
                                <th>Orientación Técnica del Docente</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (SolicitudRetroalimentacion sol : misSolicitudes) { 
                                   String pNombre = "Proyecto";
                                   String rNombre = "Regla Técnica";
                                   String sev = "MEDIA";
                                   String codigoAudSol = null;
                                   String tipoAudSol = null;
                                   String asigNomSol = null;
                                   if (sol.getHallazgo() != null) {
                                       if (sol.getHallazgo().getNivelSeveridad() != null) {
                                           sev = sol.getHallazgo().getNivelSeveridad().name();
                                       }
                                       if (sol.getHallazgo().getResultadoRegla() != null) {
                                           if (sol.getHallazgo().getResultadoRegla().getRegla() != null) {
                                               rNombre = sol.getHallazgo().getResultadoRegla().getRegla().getNombreRepresentativo();
                                           }
                                           if (sol.getHallazgo().getResultadoRegla().getAuditoria() != null) {
                                               Auditoria aRef = sol.getHallazgo().getResultadoRegla().getAuditoria();
                                               codigoAudSol = aRef.getCodigoVisible();
                                               tipoAudSol = aRef.getTipoAuditoria();
                                               if (aRef.getAsignacion() != null) {
                                                   asigNomSol = aRef.getAsignacion().getNombre();
                                               }
                                               if (aRef.getProyecto() != null) {
                                                   pNombre = aRef.getProyecto().getNombre();
                                               }
                                           }
                                       }
                                   }
                            %>
                                <tr>
                                    <td><strong>#<%= sol.getId() %></strong></td>
                                    <td>
                                        <strong><%= HtmlUtil.escape(pNombre) %></strong>
                                        <% if (codigoAudSol != null) { %>
                                            &middot; <span class="badge <%= "ASIGNACION".equalsIgnoreCase(tipoAudSol) ? "badge-alta" : "badge-media" %>" style="font-size: 0.72rem;"><%= HtmlUtil.escape(codigoAudSol) %></span>
                                        <% } %>
                                        <% if (asigNomSol != null) { %>
                                            <span class="badge badge-alta" style="font-size: 0.72rem;"><%= HtmlUtil.escape(asigNomSol) %></span>
                                        <% } %>
                                        <br>
                                        <small style="color: #64748B;"><%= HtmlUtil.escape(rNombre) %></small>
                                    </td>
                                    <td>
                                        <span class="badge badge-<%= sev.toLowerCase() %>"><%= sev %></span>
                                    </td>
                                    <td>
                                        <p style="margin: 0; font-size: 0.9rem; max-width: 280px;">
                                            "<%= HtmlUtil.escape(sol.getJustificacion() != null ? sol.getJustificacion() : "") %>"
                                        </p>
                                    </td>
                                    <td>
                                        <% if (sol.getEstado() == EstadoRetroEnum.ATENDIDA) { %>
                                            <span class="badge badge-atendida">✓ ATENDIDA</span>
                                        <% } else { %>
                                            <span class="badge badge-pendiente">⏳ PENDIENTE</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <% if (sol.getEstado() == EstadoRetroEnum.ATENDIDA && sol.getOrientacionTecnica() != null) { %>
                                            <div style="background: #F0FDF4; border: 1px solid #BBF7D0; border-radius: 6px; padding: 0.5rem; font-size: 0.9rem; color: #166534;">
                                                <strong>Orientación:</strong> <%= HtmlUtil.escape(sol.getOrientacionTecnica()) %>
                                            </div>
                                        <% } else { %>
                                            <span style="color: #94A3B8; font-style: italic; font-size: 0.85rem;">En espera de revisión...</span>
                                        <% } %>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <p style="color: #64748B; font-style: italic; margin-top: 0.75rem;">
                    Aún no ha enviado ninguna solicitud de retroalimentación.
                </p>
            <% } %>
        </div>

    </main>

    <script>
        function actualizarContador(textarea) {
            const contador = document.getElementById('contadorChar');
            if (!contador) return;
            const longitud = textarea.value.length;
            contador.textContent = longitud + ' / 1000 caracteres (mínimo 10)';
            if (longitud < 10) {
                contador.style.color = '#DC2626';
            } else {
                contador.style.color = '#64748B';
            }
        }

        function cambiarTipoAuditoria(tipo) {
            const grp = document.getElementById('grupoAsignacionFiltro');
            if (grp) {
                grp.style.display = (tipo === 'ASIGNACION') ? 'flex' : 'none';
            }
            if (tipo !== 'ASIGNACION') {
                const asigSel = document.getElementById('asignacionIdFiltro');
                if (asigSel) asigSel.value = '';
            }
            aplicarFiltros();
        }

        function aplicarFiltros() {
            let url = '${pageContext.request.contextPath}/retroalimentacion?accion=solicitar';
            const proy = document.getElementById('proyectoFiltro') ? document.getElementById('proyectoFiltro').value : '';
            const tipo = document.getElementById('tipoAuditoriaFiltro') ? document.getElementById('tipoAuditoriaFiltro').value : '';
            const asig = document.getElementById('asignacionIdFiltro') ? document.getElementById('asignacionIdFiltro').value : '';
            const aud = document.getElementById('auditoriaIdFiltro') ? document.getElementById('auditoriaIdFiltro').value : '';

            if (proy) url += '&proyectoId=' + encodeURIComponent(proy);
            if (tipo) url += '&tipoAuditoria=' + encodeURIComponent(tipo);
            if (tipo === 'ASIGNACION' && asig) url += '&asignacionId=' + encodeURIComponent(asig);
            if (aud) url += '&auditoriaId=' + encodeURIComponent(aud);

            window.location.href = url;
        }

        document.addEventListener('DOMContentLoaded', function() {
            const ta = document.getElementById('justificacion');
            if (ta) actualizarContador(ta);
        });
    </script>
            if (proyectoId && proyectoId !== '') {
                url += '&proyectoId=' + encodeURIComponent(proyectoId);
            }
            window.location.href = url;
        }

        document.addEventListener('DOMContentLoaded', function() {
            const ta = document.getElementById('justificacion');
            if (ta) actualizarContador(ta);
        });
    </script>

</body>
</html>
