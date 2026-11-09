<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.dto.NuevaReglaDTO" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    String mensajeExito = (String) request.getAttribute("mensajeExito");
    String mensajeError = (String) request.getAttribute("mensajeError");
    NuevaReglaDTO dto = (NuevaReglaDTO) request.getAttribute("reglaDTO");

    @SuppressWarnings("unchecked")
    List<Regla> reglas = (List<Regla>) request.getAttribute("reglas");
    if (reglas == null) {
        reglas = (List<Regla>) request.getAttribute("reglasVigentes");
    }
    // CA1.2: el catálogo solo muestra reglas del curso cargadas por ReglaServlet (sin consulta global de respaldo)

    NuevaReglaDTO valores = (mensajeError != null && dto != null) ? dto : new NuevaReglaDTO();
    String motorSel = valores.getTipoMotor();
    boolean mostrarFormularioAbierto = (mensajeError != null && dto != null) || "true".equals(request.getParameter("abrirForm"));
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Catálogo de Reglas Técnicas</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <!-- Encabezado con navegación principal -->
        <% 
            Object idCursoObj = request.getAttribute("idCurso");
            String idCursoParam = (idCursoObj != null) ? String.valueOf(idCursoObj) : request.getParameter("idCurso");
        %>
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <div style="display: flex; gap: 1.25rem; align-items: center; margin-bottom: 0.5rem; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/curso" class="back-link">&larr; Volver a Mis Cursos</a>
                    <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                        <a href="${pageContext.request.contextPath}/asignaciones?idCurso=<%= HtmlUtil.escape(idCursoParam) %>" class="back-link" style="color: var(--brand-blue, #2563eb); font-weight: 600;">
                            📝 Volver a Asignaciones del Curso
                        </a>
                    <% } %>
                </div>
                <h1 class="page-title" style="margin-top: 0.25rem;">Catálogo de Reglas Técnicas</h1>
                <p class="card-subtitle" style="margin-bottom: 0;">Políticas de evaluación técnica y gestión de su vigencia para las auditorías.</p>
            </div>
            <div style="display: flex; gap: 0.75rem; align-items: center; flex-wrap: wrap;">
                <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                    <a href="${pageContext.request.contextPath}/asignaciones?idCurso=<%= HtmlUtil.escape(idCursoParam) %>" class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                        <span>📝</span> Asignaciones del Curso
                    </a>
                <% } %>
                <button type="button" id="btnToggleFormulario" class="btn btn-primary" onclick="toggleFormularioRegistro()"
                        style="display: inline-flex; align-items: center; gap: 0.4rem;">
                    <span id="iconoToggle">➕</span> <span id="textoToggle">Crear nueva Regla</span>
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

            <% 
                Object idCursoReg = request.getAttribute("idCurso");
                if (idCursoReg == null && dto != null) idCursoReg = dto.getIdCurso();
                if (idCursoReg == null && request.getParameter("idCurso") != null && !request.getParameter("idCurso").isBlank()) {
                    idCursoReg = request.getParameter("idCurso");
                }
            %>
            <form action="${pageContext.request.contextPath}/regla" method="POST">
                <input type="hidden" name="accion" value="registrar">
                <% if (idCursoReg != null) { %>
                    <input type="hidden" name="idCurso" value="<%= idCursoReg %>">
                <% } %>

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

                <div style="display: flex; gap: 0.75rem; margin-top: 1rem;">
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
                                <th style="padding: 0.65rem 0.5rem; text-align: center; min-width: 110px;">Estado Actual</th>
                                <th style="padding: 0.65rem 0.5rem; text-align: center; min-width: 120px;">Acción</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Regla r : reglas) {
                                boolean esVigente = r.esVigente();
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
                                        <button type="button" class="btn-edit"
                                                data-id="<%= r.getId() %>"
                                                data-nombre="<%= HtmlUtil.escape(r.getNombreRepresentativo()) %>"
                                                data-motor="<%= (r.getTipoMotor() != null) ? r.getTipoMotor().name() : "" %>"
                                                data-descripcion="<%= HtmlUtil.escape(r.getDescripcionDetallada()) %>"
                                                data-vigencia="<%= r.getVigencia() %>"
                                                onclick="abrirModalModificarRegla(this)">
                                            ✏️ Modificar Regla
                                        </button>
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

        <!-- Modal para Modificar Regla (HU-02): solo descripción y vigencia son editables -->
        <div id="modalModificarRegla" class="modal-overlay">
            <div class="modal-content">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem;">
                    <h3 style="margin: 0; font-size: 1.2rem; color: var(--bg-primary);">Modificar Regla</h3>
                    <button type="button" onclick="cerrarModalModificarRegla()" style="background: none; border: none; font-size: 1.5rem; cursor: pointer;">&times;</button>
                </div>
                <form action="${pageContext.request.contextPath}/regla" method="POST">
                    <input type="hidden" name="accion" value="modificarRegla">
                    <input type="hidden" name="idCurso" value="<%= HtmlUtil.escape(request.getAttribute("idCurso")) %>">
                    <input type="hidden" id="modIdRegla" name="idRegla" value="">

                    <div class="form-group">
                        <label for="modNombre" class="form-label">Nombre Representativo</label>
                        <input type="text" id="modNombre" class="form-control" disabled>
                    </div>

                    <div class="form-group">
                        <label for="modMotor" class="form-label">Tipo de Motor de Evaluación</label>
                        <input type="text" id="modMotor" class="form-control" disabled>
                    </div>

                    <div class="form-group">
                        <label for="modDescripcion" class="form-label">Descripción Detallada</label>
                        <textarea id="modDescripcion" name="descripcionDetallada" class="form-control" rows="3" maxlength="200"></textarea>
                    </div>

                    <div class="form-group">
                        <label for="modVigencia" class="form-label">Estado de Vigencia</label>
                        <select id="modVigencia" name="vigencia" class="form-control">
                            <option value="VIGENTE">VIGENTE</option>
                            <option value="NO_VIGENTE">NO_VIGENTE</option>
                        </select>
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.25rem;">
                        <button type="button" class="btn btn-secondary" onclick="cerrarModalModificarRegla()">Cancelar</button>
                        <button type="submit" class="btn btn-primary">Guardar cambios</button>
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

        function abrirModalModificarRegla(boton) {
            document.getElementById('modIdRegla').value = boton.dataset.id;
            document.getElementById('modNombre').value = boton.dataset.nombre;
            document.getElementById('modMotor').value = boton.dataset.motor;
            document.getElementById('modDescripcion').value = boton.dataset.descripcion;
            document.getElementById('modVigencia').value = boton.dataset.vigencia;
            document.getElementById('modalModificarRegla').style.display = 'flex';
        }

        function cerrarModalModificarRegla() {
            document.getElementById('modalModificarRegla').style.display = 'none';
        }

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
                if (btnTexto) btnTexto.textContent = 'Crear nueva Regla';
                if (btnIcono) btnIcono.textContent = '➕';
            }
        }
    </script>
</body>
</html>
