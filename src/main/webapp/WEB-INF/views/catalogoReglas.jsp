<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="com.gr4.owlaudit.dao.ReglaDAOImpl" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");

    @SuppressWarnings("unchecked")
    List<Regla> reglas = (List<Regla>) request.getAttribute("reglas");
    if (reglas == null) {
        reglas = (List<Regla>) request.getAttribute("reglasVigentes");
    }
    if (reglas == null) {
        try {
            reglas = new ReglaDAOImpl().consultarTodasLasReglas();
        } catch (Exception ignored) {}
    }

    NuevaReglaDTO valores = (mensajeError != null && dto != null) ? dto : new NuevaReglaDTO();
    String motorSel = valores.getTipoMotor();
    String severidadSel = valores.getNivelSeveridad();
    boolean mostrarFormularioAbierto = (mensajeError != null && dto != null) || "true".equals(request.getParameter("abrirForm"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Catálogo de Reglas Técnicas</title>
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
        .btn-activate:hover { background-color: #16A34A; color: #FFFFFF; border-color: #16A34A; box-shadow: 0 2px 4px rgba(225, 29, 72, 0.2); }
        .btn-edit { background-color: #EFF6FF; color: #1D4ED8; border-color: #BFDBFE; }
        .btn-edit:hover { background-color: #2563EB; color: #FFFFFF; border-color: #2563EB; box-shadow: 0 2px 4px rgba(37, 99, 235, 0.2); }
        .btn-delete { background-color: #FEF2F2; color: #B91C1C; border-color: #FECACA; }
        .btn-delete:hover { background-color: #DC2626; color: #FFFFFF; border-color: #DC2626; box-shadow: 0 2px 4px rgba(220, 38, 38, 0.2); }

        .modal-overlay {
            position: fixed; top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px);
            display: none; align-items: center; justify-content: center; z-index: 1000;
        }
        .modal-content {
            background: #FFFFFF; border-radius: 12px; max-width: 620px; width: 92%;
            padding: 1.75rem; max-height: 90vh; overflow-y: auto; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
        }

        .alert-dismissible {
            position: relative; display: flex; align-items: flex-start; justify-content: space-between;
            padding: 1rem 1.25rem; border-radius: var(--radius-md); margin-bottom: 1.5rem; box-shadow: var(--shadow-sm);
        }
        .alert-dismissible .btn-close {
            background: none; border: none; font-size: 1.25rem; line-height: 1;
            cursor: pointer; color: inherit; opacity: 0.7; padding: 0 0 0 0.5rem;
        }
        .alert-dismissible .btn-close:hover { opacity: 1; }
    </style>
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <!-- Encabezado con navegación principal -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <a href="${pageContext.request.contextPath}/vistas" class="back-link">&larr; Volver al Menú Principal</a>
                <h1 class="page-title" style="margin-top: 0.5rem;">Catálogo de Reglas Técnicas</h1>
                <p class="card-subtitle" style="margin-bottom: 0;">Políticas de evaluación técnica y gestión de su vigencia para las auditorías.</p>
            </div>
            <div>
                <button type="button" id="btnToggleFormulario" class="btn btn-primary" onclick="toggleFormularioRegistro()"
                        style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <span id="iconoToggle">➕</span> <span id="textoToggle">Registrar Nueva Regla</span>
                </button>
            </div>
        </div>

        <!-- Mensajes de Confirmación o Error (HU-01 y CU01) -->
        <% if (mensajeExito != null) { %>
            <div id="alertaExito" class="alert alert-success alert-dismissible" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span style="font-size: 1.2rem;">✅</span>
                    <span><%= HtmlUtil.escape(mensajeExito) %></span>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertaExito')">&times;</button>
            </div>
        <% } %>

        <% if (mensajeError != null) { %>
            <div id="alertaError" class="alert alert-error alert-dismissible" role="alert">
                <div style="display: flex; align-items: center; gap: 0.5rem;">
                    <span style="font-size: 1.2rem;">⚠️</span>
                    <span><%= HtmlUtil.escape(mensajeError) %></span>
                </div>
                <button type="button" class="btn-close" aria-label="Cerrar" onclick="cerrarAlerta('alertaError')">&times;</button>
            </div>
        <% } %>

        <!-- Sección de Registro de Nueva Regla (Desplegable) -->
        <div id="cardRegistroRegla" class="card" style="display: <%= mostrarFormularioAbierto ? "block" : "none" %>; margin-bottom: 1.5rem; border-left: 4px solid var(--primary-color);">
            <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 1rem;">
                <div>
                    <h2 class="card-title" style="font-size: 1.2rem; margin-bottom: 0.25rem;">Configurar Regla de Evaluación Técnica</h2>
                    <p class="card-subtitle" style="margin-bottom: 0;">Complete los parámetros para registrar una nueva regla en el catálogo.</p>
                </div>
                <button type="button" class="btn btn-secondary" onclick="toggleFormularioRegistro()" style="padding: 0.35rem 0.75rem; font-size: 0.85rem;">
                    ✕ Cancelar
                </button>
            </div>

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
                    <textarea id="descripcionDetallada" name="descripcionDetallada" class="form-control" rows="2"
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

                <div style="display: flex; gap: 0.75rem; margin-top: 0.5rem;">
                    <button type="submit" class="btn btn-primary">Registrar Regla</button>
                    <button type="button" class="btn btn-secondary" onclick="toggleFormularioRegistro()">Cancelar</button>
                </div>
            </form>
        </div>

        <!-- Tabla del Catálogo de Reglas Técnicas (Módulo Principal) -->
        <div class="card">
            <h2 class="card-title" style="font-size: 1.25rem;">Políticas de Evaluación Configuradas</h2>
            <p class="card-subtitle">Listado de reglas técnicas registradas con su estado actual de vigencia para las auditorías.</p>

            <% if (reglas != null && !reglas.isEmpty()) { %>
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
                            <% for (Regla r : reglas) {
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
                                        <div style="display: inline-flex; align-items: center; gap: 0.35rem; flex-wrap: wrap; justify-content: center;">
                                            <form action="${pageContext.request.contextPath}/regla" method="POST" style="margin: 0; display: inline-block;">
                                                <input type="hidden" name="accion" value="cambiarVigencia">
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

                                            <button type="button" class="btn-action-status btn-edit"
                                                    title="Editar esta regla técnica"
                                                    onclick="abrirModalEditar(
                                                        '<%= r.getId() %>',
                                                        '<%= r.getTipoMotor() != null ? r.getTipoMotor().name() : "" %>',
                                                        '<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>',
                                                        '<%= HtmlUtil.escape(r.getDescripcionDetallada() != null ? r.getDescripcionDetallada() : "") %>',
                                                        '<%= HtmlUtil.escape(r.getParametroExacto()) %>',
                                                        '<%= r.getNivelSeveridad() != null ? r.getNivelSeveridad().name() : "" %>',
                                                        '<%= r.getPonderacion() %>'
                                                    )">
                                                <span>✏️</span> Editar
                                            </button>

                                            <form action="${pageContext.request.contextPath}/regla" method="POST" style="margin: 0; display: inline-block;">
                                                <input type="hidden" name="accion" value="eliminar">
                                                <input type="hidden" name="idRegla" value="<%= r.getId() %>">
                                                <button type="submit" class="btn-action-status btn-delete"
                                                        title="Eliminar permanentemente esta regla"
                                                        onclick="return confirmarEliminarRegla('<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>');">
                                                    <span>🗑️</span> Eliminar
                                                </button>
                                            </form>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } else { %>
                <div class="empty-state" style="padding: 2.5rem 1rem; text-align: center;">
                    <p style="color: var(--text-muted); font-size: 1rem; margin-bottom: 1rem;">No se encontraron reglas de evaluación registradas.</p>
                    <button type="button" class="btn btn-primary" onclick="toggleFormularioRegistro()">Registrar la primera regla</button>
                </div>
            <% } %>
        </div>

        <!-- Modal para Editar Regla Técnica -->
        <div id="modalEditarRegla" class="modal-overlay">
            <div class="modal-content">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem;">
                    <h3 style="margin: 0; font-size: 1.2rem; color: var(--bg-primary);">Editar Regla Técnica</h3>
                    <button type="button" class="btn-close" onclick="cerrarModalEditar()" style="background: none; border: none; font-size: 1.5rem; cursor: pointer;">&times;</button>
                </div>
                <form action="${pageContext.request.contextPath}/regla" method="POST">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" id="editIdRegla" name="idRegla" value="">

                    <div class="form-group">
                        <label for="editTipoMotor" class="form-label">Tipo de Motor de Evaluación</label>
                        <select id="editTipoMotor" name="tipoMotor" class="form-control" required>
                            <option value="PRESENCIA_ARCHIVO">Presencia de Archivo</option>
                            <option value="RESTRICCION_ARCHIVOS">Restricción de Archivos</option>
                            <option value="ESTRUCTURA_CARPETAS">Estructura de Carpetas</option>
                            <option value="CONVENCION_COMMITS">Convención de Commits</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="editNombreRepresentativo" class="form-label">Nombre Representativo</label>
                        <input type="text" id="editNombreRepresentativo" name="nombreRepresentativo" class="form-control" maxlength="100" required>
                    </div>

                    <div class="form-group">
                        <label for="editDescripcionDetallada" class="form-label">Descripción Detallada</label>
                        <textarea id="editDescripcionDetallada" name="descripcionDetallada" class="form-control" rows="2" maxlength="200" required></textarea>
                    </div>

                    <div class="form-group">
                        <label for="editParametroExacto" class="form-label">Parámetro Exacto</label>
                        <input type="text" id="editParametroExacto" name="parametroExacto" class="form-control" maxlength="100" required>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                        <div class="form-group">
                            <label for="editNivelSeveridad" class="form-label">Nivel de Severidad</label>
                            <select id="editNivelSeveridad" name="nivelSeveridad" class="form-control" required>
                                <option value="ALTA">ALTA</option>
                                <option value="MEDIA">MEDIA</option>
                                <option value="BAJA">BAJA</option>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="editPonderacion" class="form-label">Ponderación (Puntos)</label>
                            <input type="number" id="editPonderacion" name="ponderacion" class="form-control" min="1" max="100" required>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.25rem;">
                        <button type="button" class="btn btn-secondary" onclick="cerrarModalEditar()">Cancelar</button>
                        <button type="submit" class="btn btn-primary">Guardar Cambios</button>
                    </div>
                </form>
            </div>
        </div>
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

        function confirmarEliminarRegla(nombreRegla) {
            return confirm("¿Está seguro de que desea eliminar permanentemente la regla técnica:\n\n\"" + nombreRegla + "\"?\n\nEsta acción no se puede deshacer.");
        }

        function abrirModalEditar(id, motor, nombre, desc, param, severidad, ponderacion) {
            document.getElementById('editIdRegla').value = id;
            document.getElementById('editTipoMotor').value = motor;
            document.getElementById('editNombreRepresentativo').value = nombre;
            document.getElementById('editDescripcionDetallada').value = desc;
            document.getElementById('editParametroExacto').value = param;
            document.getElementById('editNivelSeveridad').value = severidad;
            document.getElementById('editPonderacion').value = ponderacion;
            document.getElementById('modalEditarRegla').style.display = 'flex';
        }

        function cerrarModalEditar() {
            document.getElementById('modalEditarRegla').style.display = 'none';
        }

        window.onclick = function(event) {
            var modal = document.getElementById('modalEditarRegla');
            if (event.target === modal) {
                cerrarModalEditar();
            }
        };

        function toggleFormularioRegistro() {
            var card = document.getElementById('cardRegistroRegla');
            var btnTexto = document.getElementById('textoToggle');
            var btnIcono = document.getElementById('iconoToggle');
            if (card.style.display === 'none' || card.style.display === '') {
                card.style.display = 'block';
                if (btnTexto) btnTexto.textContent = 'Ocultar Formulario';
                if (btnIcono) btnIcono.textContent = '▲';
                card.scrollIntoView({ behavior: 'smooth', block: 'start' });
            } else {
                card.style.display = 'none';
                if (btnTexto) btnTexto.textContent = 'Registrar Nueva Regla';
                if (btnIcono) btnIcono.textContent = '➕';
            }
        }
    </script>
</body>
</html>
