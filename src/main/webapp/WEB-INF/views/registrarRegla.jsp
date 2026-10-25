<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");

    @SuppressWarnings("unchecked")
    List<Regla> reglasVigentes = (List<Regla>) request.getAttribute("reglasVigentes");
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("reglas");
    }
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("listaReglas");
    }

    NuevaReglaDTO valores = (mensajeError != null && dto != null) ? dto : new NuevaReglaDTO();
    String motorSel = valores.getTipoMotor();
    String severidadSel = valores.getNivelSeveridad();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Registrar Regla de Calidad</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
    <style>
        .badge-vigente {
            background-color: #DCFCE7; color: #166534; border: 1px solid #BBF7D0;
            display: inline-flex; align-items: center; gap: 0.35rem; padding: 0.22rem 0.6rem;
            border-radius: 999px; font-size: 0.75rem; font-weight: 700; letter-spacing: 0.3px;
        }
        .badge-no-vigente {
            background-color: #FEE2E2; color: #991B1B; border: 1px solid #FECACA;
            display: inline-flex; align-items: center; gap: 0.35rem; padding: 0.22rem 0.6rem;
            border-radius: 999px; font-size: 0.75rem; font-weight: 700; letter-spacing: 0.3px;
        }
        .status-dot { width: 7px; height: 7px; border-radius: 50%; display: inline-block; }
        .badge-vigente .status-dot { background-color: #16A34A; box-shadow: 0 0 0 2px rgba(22, 163, 74, 0.2); }
        .badge-no-vigente .status-dot { background-color: #DC2626; }

        .btn-action-status {
            display: inline-flex; align-items: center; gap: 0.35rem; padding: 0.38rem 0.85rem;
            font-size: 0.82rem; font-weight: 600; border-radius: 6px; border: 1px solid transparent;
            cursor: pointer; transition: all 0.2s; white-space: nowrap;
        }
        .btn-deactivate { background-color: #FFF1F2; color: #BE123C; border-color: #FECDD3; }
        .btn-deactivate:hover { background-color: #E11D48; color: #FFFFFF; border-color: #E11D48; box-shadow: 0 2px 4px rgba(225, 29, 72, 0.2); }
        .btn-activate { background-color: #F0FDF4; color: #15803D; border-color: #BBF7D0; }
        .btn-activate:hover { background-color: #16A34A; color: #FFFFFF; border-color: #16A34A; box-shadow: 0 2px 4px rgba(22, 163, 74, 0.2); }

        .alert-dismissible {
            position: relative; display: flex; align-items: flex-start; justify-content: space-between;
            padding: 1rem 1.25rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; box-shadow: var(--shadow-sm);
        }
        .alert-dismissible .btn-close {
            background: transparent; border: none; font-size: 1.4rem; line-height: 1;
            color: inherit; opacity: 0.6; cursor: pointer; padding: 0 0.25rem; margin-left: 1rem;
        }
        .alert-dismissible .btn-close:hover { opacity: 1; }
    </style>
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">

        <!-- Notificaciones y Feedback Descartables -->
        <% if (mensajeExito != null && !mensajeExito.trim().isEmpty()) { %>
            <div id="alertSuccess" class="alert-dismissible alert-success" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span>✅</span>
                    <div><%= HtmlUtil.escape(mensajeExito) %></div>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertSuccess')">&times;</button>
            </div>
        <% } %>

        <% if (mensajeError != null && !mensajeError.trim().isEmpty()) { %>
            <div id="alertError" class="alert-dismissible alert-error" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span>⚠️</span>
                    <div><%= HtmlUtil.escape(mensajeError) %></div>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertError')">&times;</button>
            </div>
        <% } %>

        <!-- Formulario de Registro de Regla -->
        <div class="card">
            <h1 class="card-title">Configurar Regla de Evaluación Técnica</h1>
            <p class="card-subtitle">Cree políticas de calidad de código para ser evaluadas automáticamente en los repositorios.</p>

            <form action="${pageContext.request.contextPath}/regla" method="POST">
                <input type="hidden" name="accion" value="registrar">
                <div class="form-group">
                    <label for="tipoMotor" class="form-label">Tipo de Motor de Evaluación</label>
                    <select id="tipoMotor" name="tipoMotor" class="form-control" required>
                        <option value="PRESENCIA_ARCHIVO" <%="PRESENCIA_ARCHIVO".equals(motorSel) ? "selected" : ""%>>Presencia de Archivo</option>
                        <option value="RESTRICCION_ARCHIVOS" <%="RESTRICCION_ARCHIVOS".equals(motorSel) ? "selected" : ""%>>Restricción de Archivos</option>
                        <option value="ESTRUCTURA_CARPETAS" <%="ESTRUCTURA_CARPETAS".equals(motorSel) ? "selected" : ""%>>Estructura de Carpetas</option>
                        <option value="CONVENCION_COMMITS" <%="CONVENCION_COMMITS".equals(motorSel) ? "selected" : ""%>>Convención de Commits</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="nombreRepresentativo" class="form-label">Nombre Representativo</label>
                    <input type="text" id="nombreRepresentativo" name="nombreRepresentativo" class="form-control"
                           placeholder="Ej. Verificar archivo README.md" maxlength="100"
                           value="<%= HtmlUtil.escape(valores.getNombreRepresentativo()) %>" required>
                </div>

                <div class="form-group">
                    <label for="descripcionDetallada" class="form-label">Descripción Detallada</label>
                    <textarea id="descripcionDetallada" name="descripcionDetallada" class="form-control" rows="3"
                              placeholder="Explique el propósito de la regla..." maxlength="200"
                              required><%= HtmlUtil.escape(valores.getDescripcionDetallada()) %></textarea>
                </div>

                <div class="form-group">
                    <label for="parametroExacto" class="form-label">Parámetro Exacto (Ruta / Expresión / Archivo)</label>
                    <input type="text" id="parametroExacto" name="parametroExacto" class="form-control"
                           placeholder="Ej. README.md, src/main/java o *.env" maxlength="100"
                           value="<%= HtmlUtil.escape(valores.getParametroExacto()) %>" required>
                </div>

                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label for="nivelSeveridad" class="form-label">Nivel de Severidad</label>
                        <select id="nivelSeveridad" name="nivelSeveridad" class="form-control" required>
                            <option value="ALTA" <%="ALTA".equals(severidadSel) ? "selected" : ""%>>ALTA</option>
                            <option value="MEDIA" <%="MEDIA".equals(severidadSel) ? "selected" : ""%>>MEDIA</option>
                            <option value="BAJA" <%="BAJA".equals(severidadSel) ? "selected" : ""%>>BAJA</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="ponderacion" class="form-label">Ponderación (Puntos)</label>
                        <input type="number" id="ponderacion" name="ponderacion" class="form-control" min="1" max="100"
                               placeholder="Ej. 20" value="<%= valores.getPonderacion() > 0 ? String.valueOf(valores.getPonderacion()) : "" %>" required>
                    </div>
                </div>

                <button type="submit" class="btn btn-primary">Registrar Regla</button>
            </form>
        </div>

        <% if (mensajeExito != null && dto != null) { %>
            <div class="card" style="border-left: 4px solid var(--accent-gold);">
                <h3 style="margin-bottom: 0.5rem;">📐 Regla registrada</h3>
                <p><strong>Motor:</strong> <%= HtmlUtil.escape(dto.getTipoMotor()) %></p>
                <p><strong>Nombre:</strong> <%= HtmlUtil.escape(dto.getNombreRepresentativo()) %></p>
                <p><strong>Parámetro:</strong> <%= HtmlUtil.escape(dto.getParametroExacto()) %></p>
                <p><strong>Severidad:</strong> <%= HtmlUtil.escape(dto.getNivelSeveridad()) %> | <strong>Puntos:</strong> <%= dto.getPonderacion() %></p>
            </div>
        <% } %>

        <!-- Catálogo de Reglas Técnicas con Badges de Estado y Botón de Cambio de Estado (HU-01) -->
        <% if (reglasVigentes != null && !reglasVigentes.isEmpty()) { %>
            <div class="card" style="margin-top: 1.5rem;">
                <h2 class="card-title" style="font-size: 1.25rem;">Catálogo de Reglas Técnicas</h2>
                <p class="card-subtitle">Políticas de evaluación técnica configuradas para las auditorías de repositorios.</p>

                <div class="table-responsive">
                    <table class="table" style="width: 100%; border-collapse: collapse; margin-top: 1rem;">
                        <thead>
                            <tr style="border-bottom: 2px solid var(--border-color); text-align: left;">
                                <th style="padding: 0.65rem 0.5rem; text-align: center; width: 55px;">ID</th>
                                <th style="padding: 0.65rem 0.5rem; min-width: 160px;">Nombre Representativo</th>
                                <th style="padding: 0.65rem 0.5rem;">Motor</th>
                                <th style="padding: 0.65rem 0.5rem;">Parámetro Exacto</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center;">Severidad</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center;">Puntos</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center; min-width: 110px;">Estado Actual</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center; min-width: 120px;">Acción</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Regla r : reglasVigentes) {
                                boolean esVigente = r.esVigente();
                                String nuevoEstado = esVigente ? "NO_VIGENTE" : "VIGENTE";
                                String s = (r.getNivelSeveridad() != null) ? r.getNivelSeveridad().name() : "BAJA";
                            %>
                                <tr style="border-bottom: 1px solid var(--border-color);">
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;"><strong>#<%= r.getId() %></strong></td>
                                    <td style="padding: 0.65rem 0.5rem;">
                                        <div style="font-weight: 600; color: var(--bg-primary);"><%= HtmlUtil.escape(r.getNombreRepresentativo()) %></div>
                                        <% if (r.getDescripcionDetallada() != null && !r.getDescripcionDetallada().isBlank()) { %>
                                            <div style="font-size: 0.8rem; color: var(--text-muted); line-height: 1.3;"><%= HtmlUtil.escape(r.getDescripcionDetallada()) %></div>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.65rem 0.5rem;"><%= (r.getTipoMotor() != null) ? r.getTipoMotor().name() : "—" %></td>
                                    <td style="padding: 0.65rem 0.5rem;"><code><%= HtmlUtil.escape(r.getParametroExacto()) %></code></td>
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;">
                                        <span class="badge badge-<%= s.toLowerCase() %>"><%= s %></span>
                                    </td>
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;"><strong><%= r.getPonderacion() %> pts</strong></td>
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;">
                                        <% if (esVigente) { %>
                                            <span class="badge-vigente" title="Regla habilitada para auditorías">
                                                <span class="status-dot"></span> VIGENTE
                                            </span>
                                        <% } else { %>
                                            <span class="badge-no-vigente" title="Regla inactiva">
                                                <span class="status-dot"></span> NO_VIGENTE
                                            </span>
                                        <% } %>
                                    </td>
                                    <td style="padding: 0.65rem 0.5rem; text-align: center;">
                                        <form action="${pageContext.request.contextPath}/regla" method="POST" style="margin: 0; display: inline-block;">
                                            <input type="hidden" name="accion" value="cambiarEstado">
                                            <input type="hidden" name="nombreRegla" value="<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>">
                                            <input type="hidden" name="idRegla" value="<%= r.getId() %>">
                                            <input type="hidden" name="nuevoEstado" value="<%= nuevoEstado %>">

                                            <% if (esVigente) { %>
                                                <button type="submit" class="btn-action-status btn-deactivate"
                                                        title="Haga clic para cambiar a NO_VIGENTE"
                                                        onclick="return confirmarCambioEstado('<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>', 'desactivar');">
                                                    <span>🚫</span> Desactivar
                                                </button>
                                            <% } else { %>
                                                <button type="submit" class="btn-action-status btn-activate"
                                                        title="Haga clic para cambiar a VIGENTE"
                                                        onclick="return confirmarCambioEstado('<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>', 'activar');">
                                                    <span>✅</span> Activar
                                                </button>
                                            <% } %>
                                        </form>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>
    </main>

    <script>
        function cerrarAlerta(elementId) {
            var alerta = document.getElementById(elementId);
            if (alerta) { alerta.style.display = 'none'; }
        }

        function confirmarCambioEstado(nombreRegla, accion) {
            var accionTexto = (accion === 'activar') ? 'activar (marcar como VIGENTE)' : 'desactivar (marcar como NO_VIGENTE)';
            return confirm("¿Está seguro de que desea " + accionTexto + " la regla técnica:\n\n\"" + nombreRegla + "\"?");
        }
    </script>
</body>
</html>