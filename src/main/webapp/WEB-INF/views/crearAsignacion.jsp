<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.model.Regla" %>
<%@ page import="com.gr4.owlaudit.model.NivelSeveridadEnum" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%@ page import="com.gr4.owlaudit.dao.ReglaDAOImpl" %>
<%
    // Manejo de notificaciones de éxito y error según acuerdos del equipo
    String mensajeExito = (String) request.getAttribute("exito");
    if (mensajeExito == null || mensajeExito.isBlank()) {
        mensajeExito = (String) request.getAttribute("mensajeExito");
    }

    String mensajeError = (String) request.getAttribute("error");
    if (mensajeError == null || mensajeError.isBlank()) {
        mensajeError = (String) request.getAttribute("mensajeError");
    }

    // Preservar valores ingresados por el usuario si ocurrió un error de validación
    String nombrePrevio = request.getParameter("nombre") != null ? request.getParameter("nombre") : "";
    String umbralPrevio = request.getParameter("umbralAprobacion") != null ? request.getParameter("umbralAprobacion") : "";

    // Obtención de reglas vigentes (provistas por doGet en AsignacionServlet o fallback resiliente)
    @SuppressWarnings("unchecked")
    List<Regla> reglasVigentes = (List<Regla>) request.getAttribute("reglasVigentes");
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("reglas");
    }
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("listaReglas");
    }
    if (reglasVigentes == null) {
        try {
            reglasVigentes = new ReglaDAOImpl().consultarReglasDeEvaluacionVigentes();
        } catch (Exception ignored) {
            reglasVigentes = java.util.Collections.emptyList();
        }
    }

    // Reglas previamente seleccionadas (si hubo error al enviar)
    String[] reglasSeleccionadas = request.getParameterValues("reglasIds");
    java.util.Set<String> setReglasPrevias = new java.util.HashSet<>();
    if (reglasSeleccionadas != null) {
        for (String rid : reglasSeleccionadas) {
            setReglasPrevias.add(rid.trim());
        }
    }

    // Listado de asignaciones existentes acordado con Franciel (List<Asignacion>)
    @SuppressWarnings("rawtypes")
    List asignacionesExistentes = (List) request.getAttribute("asignaciones");
    if (asignacionesExistentes == null) {
        asignacionesExistentes = (List) request.getAttribute("listaAsignaciones");
    }

    String ctx = request.getContextPath();
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>OwlAudit - Crear Asignación Académica (HU02)</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
</head>
<body>

    <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

    <main class="container">
        <!-- Tarjeta de Encabezado Principal -->
        <div class="card">
            <h1 class="card-title">Crear Asignación Académica</h1>
            <p class="card-subtitle" style="margin-bottom: 0;">
                Defina el nombre de la asignación, configure el umbral mínimo de aprobación y vincule las reglas técnicas vigentes para la evaluación automatizada.
            </p>
        </div>

        <!-- Contenedores de Notificación de Éxito o Validación de Error -->
        <% if (mensajeExito != null && !mensajeExito.isBlank()) { %>
            <div class="alert alert-success" id="contenedor-notificacion-exito" role="alert">
                <strong>✓ Operación exitosa:</strong> <%= HtmlUtil.escape(mensajeExito) %>
            </div>
        <% } %>

        <% if (mensajeError != null && !mensajeError.isBlank()) { %>
            <div class="alert alert-error" id="contenedor-notificacion-error" role="alert">
                <strong>⚠ Error de validación:</strong> <%= HtmlUtil.escape(mensajeError) %>
            </div>
        <% } %>

        <!-- Formulario de Creación de Asignación -->
        <div class="card">
            <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1.25rem; color: var(--bg-primary);">
                Formulario de Configuración de Asignación
            </h2>

            <form id="formCrearAsignacion" action="<%= ctx %>/asignaciones" method="POST">
                
                <!-- Campo: Nombre de la Asignación -->
                <div class="form-group">
                    <label for="nombre" class="form-label">Nombre de la Asignación *</label>
                    <input type="text" 
                           id="nombre" 
                           name="nombre" 
                           class="form-control" 
                           placeholder="Ej. Taller 1 - Convenciones de Git y Calidad de Código" 
                           value="<%= HtmlUtil.escape(nombrePrevio) %>" 
                           required 
                           autofocus>
                    <span class="input-hint">Ingrese una denominación representativa para identificar la asignación académica.</span>
                </div>

                <!-- Campo: Umbral de Aprobación (text para permitir validación en backend de escenarios 2.2 y 2.3) -->
                <div class="form-group">
                    <label for="umbralAprobacion" class="form-label">Umbral de Aprobación (%) *</label>
                    <input type="text" 
                           id="umbralAprobacion" 
                           name="umbralAprobacion" 
                           class="form-control" 
                           placeholder="Ej. 70" 
                           value="<%= HtmlUtil.escape(umbralPrevio) %>" 
                           required>
                    <span class="input-hint">El valor debe ser numérico y estar comprendido entre 50% y 100%.</span>
                </div>

                <!-- Campo: Listado de Reglas Vigentes Seleccionables -->
                <div class="form-group">
                    <label class="form-label">Reglas Técnicas Vigentes Seleccionables *</label>
                    <span class="input-hint" style="margin-bottom: 0.5rem;">
                        Seleccione las reglas de calidad que serán auditadas en esta asignación (debe seleccionar al menos una regla).
                    </span>

                    <div class="rules-selection-box">
                        <div class="rules-toolbar">
                            <div>
                                <span style="font-size: 0.85rem; font-weight: 600; color: var(--text-main);">
                                    Disponibles: <span id="total-reglas-count"><%= reglasVigentes.size() %></span> regla(s) vigente(s)
                                </span>
                                &middot;
                                <span style="font-size: 0.85rem; color: var(--text-muted);">
                                    Seleccionadas: <strong id="seleccionadas-reglas-count">0</strong>
                                </span>
                            </div>
                            <div>
                                <button type="button" class="btn-link-action" onclick="marcarTodasLasReglas(true)">Seleccionar todas</button>
                                &nbsp;|&nbsp;
                                <button type="button" class="btn-link-action" onclick="marcarTodasLasReglas(false)">Deseleccionar todas</button>
                            </div>
                        </div>

                        <% if (reglasVigentes.isEmpty()) { %>
                            <div style="text-align: center; padding: 2rem 1rem; color: var(--text-muted);">
                                <p style="margin-bottom: 0.5rem; font-size: 1.1rem;">⚠️ No existen reglas vigentes disponibles.</p>
                                <p style="font-size: 0.875rem;">
                                    Para crear una asignación debe existir al menos una regla activa. 
                                    Puede activarlas o crearlas desde el 
                                    <a href="<%= ctx %>/regla" style="color: var(--brand-blue); font-weight: 600;">Catálogo de Reglas</a>.
                                </p>
                            </div>
                        <% } else { %>
                            <div class="rules-list-container">
                                <% for (Regla regla : reglasVigentes) { 
                                       String strId = String.valueOf(regla.getId());
                                       boolean checked = setReglasPrevias.contains(strId);
                                       String severidadCss = "badge-media";
                                       if (regla.getNivelSeveridad() == NivelSeveridadEnum.ALTA) {
                                           severidadCss = "badge-alta";
                                       } else if (regla.getNivelSeveridad() == NivelSeveridadEnum.BAJA) {
                                           severidadCss = "badge-baja";
                                       }
                                %>
                                    <div class="rule-item-row">
                                        <div class="rule-checkbox-wrapper">
                                            <!-- Checkbox con atributo exacto name="reglasIds" value="${regla.id}" según acuerdos -->
                                            <input type="checkbox" 
                                                   name="reglasIds" 
                                                   value="<%= regla.getId() %>" 
                                                   id="regla_<%= regla.getId() %>" 
                                                   <%= checked ? "checked" : "" %> 
                                                   onchange="actualizarContadorReglas()">
                                        </div>
                                        <div class="rule-details">
                                            <div class="rule-title-line">
                                                <label for="regla_<%= regla.getId() %>">
                                                    <%= HtmlUtil.escape(regla.getNombreRepresentativo()) %>
                                                </label>
                                                <div class="rule-meta-badges">
                                                    <span class="badge <%= severidadCss %>">
                                                        <%= regla.getNivelSeveridad() %>
                                                    </span>
                                                    <span class="badge badge-muted">
                                                        <%= regla.getPonderacion() %> pts
                                                    </span>
                                                    <% if (regla.getTipoMotor() != null) { %>
                                                        <span class="badge badge-muted" style="font-size: 0.7rem;">
                                                            <%= regla.getTipoMotor() %>
                                                        </span>
                                                    <% } %>
                                                </div>
                                            </div>
                                            <% if (regla.getDescripcionDetallada() != null && !regla.getDescripcionDetallada().isBlank()) { %>
                                                <p class="rule-desc"><%= HtmlUtil.escape(regla.getDescripcionDetallada()) %></p>
                                            <% } %>
                                        </div>
                                    </div>
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                </div>

                <!-- Botones de Acción del Formulario -->
                <div style="display: flex; gap: 1rem; align-items: center; margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-primary" id="btnCrearAsignacion">
                        Crear Asignación
                    </button>
                    <a href="<%= ctx %>/vistas" class="btn btn-secondary">
                        Cancelar
                    </a>
                </div>
            </form>
        </div>

        <!-- Listado de Asignaciones Existentes (Requerimiento de Franciel) -->
        <div class="card">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--bg-primary); margin-bottom: 0;">
                    Asignaciones Académicas Registradas
                </h2>
                <span class="badge badge-muted">
                    Total: <%= asignacionesExistentes != null ? asignacionesExistentes.size() : 0 %>
                </span>
            </div>

            <% if (asignacionesExistentes == null || asignacionesExistentes.isEmpty()) { %>
                <div style="text-align: center; padding: 2rem 1rem; background-color: #F8FAFC; border-radius: var(--radius-md); border: 1px dashed var(--border-color);">
                    <p style="color: var(--text-muted); margin-bottom: 0;">
                        No existen asignaciones académicas registradas aún. Utilice el formulario superior para crear la primera.
                    </p>
                </div>
            <% } else { %>
                <div class="table-responsive">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th style="width: 80px;">ID</th>
                                <th>Nombre de la Asignación</th>
                                <th style="width: 160px; text-align: center;">Umbral de Aprobación</th>
                                <th style="width: 160px; text-align: center;">Reglas Vinculadas</th>
                                <th style="width: 160px; text-align: center;">Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                               for (Object objAsig : asignacionesExistentes) {
                                   Long idAsig = null;
                                   Long idVisible = null;
                                   String nomAsig = "";
                                   Object umbralAsig = "";
                                   int cantidadReglas = 0;
                                   java.util.List<Long> reglaIdsList = new java.util.ArrayList<>();
                                   try {
                                       java.lang.reflect.Method mId = objAsig.getClass().getMethod("getId");
                                       idAsig = (Long) mId.invoke(objAsig);
                                   } catch (Exception ignored) {}
                                   try {
                                       java.lang.reflect.Method mIdVis = objAsig.getClass().getMethod("getIdVisible");
                                       idVisible = (Long) mIdVis.invoke(objAsig);
                                   } catch (Exception ignored) {}
                                   if (idVisible == null) {
                                       try {
                                           java.lang.reflect.Method mIdSec = objAsig.getClass().getMethod("getIdSecuencial");
                                           idVisible = (Long) mIdSec.invoke(objAsig);
                                       } catch (Exception ignored) {}
                                   }
                                   if (idVisible == null) {
                                       idVisible = idAsig;
                                   }
                                   try {
                                       java.lang.reflect.Method mNom = objAsig.getClass().getMethod("getNombre");
                                       nomAsig = (String) mNom.invoke(objAsig);
                                   } catch (Exception ignored) {}
                                   try {
                                       java.lang.reflect.Method mUmb = objAsig.getClass().getMethod("getUmbralAprobacion");
                                       umbralAsig = mUmb.invoke(objAsig);
                                   } catch (Exception ignored) {}
                                   try {
                                       java.lang.reflect.Method mReg = objAsig.getClass().getMethod("getReglas");
                                       Object listaR = mReg.invoke(objAsig);
                                       if (listaR instanceof java.util.Collection) {
                                           java.util.Collection<?> col = (java.util.Collection<?>) listaR;
                                           cantidadReglas = col.size();
                                           for (Object rObj : col) {
                                               java.lang.reflect.Method mRegId = rObj.getClass().getMethod("getId");
                                               reglaIdsList.add((Long) mRegId.invoke(rObj));
                                           }
                                       }
                                   } catch (Exception ignored) {}

                                   StringBuilder sbReglasJson = new StringBuilder("[");
                                   for (int i = 0; i < reglaIdsList.size(); i++) {
                                       if (i > 0) sbReglasJson.append(",");
                                       sbReglasJson.append(reglaIdsList.get(i));
                                   }
                                   sbReglasJson.append("]");
                            %>
                                <tr>
                                    <td><strong>#<%= idVisible != null ? idVisible : "-" %></strong></td>
                                    <td>
                                        <span style="font-weight: 600; color: var(--text-main);">
                                            <%= HtmlUtil.escape(nomAsig) %>
                                        </span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="badge badge-docente" style="font-size: 0.85rem;">
                                            <%= HtmlUtil.escape(umbralAsig) %>%
                                        </span>
                                    </td>
                                    <td style="text-align: center;">
                                        <span class="badge badge-muted">
                                            <%= cantidadReglas %> regla(s)
                                        </span>
                                    </td>
                                    <td style="text-align: center;">
                                        <div style="display: inline-flex; align-items: center; gap: 0.4rem; justify-content: center; flex-wrap: wrap;">
                                            <button type="button" class="btn-edit"
                                                    onclick="abrirModalEditarAsignacion('<%= idAsig %>', '<%= HtmlUtil.escape(nomAsig) %>', '<%= HtmlUtil.escape(umbralAsig) %>', <%= sbReglasJson.toString() %>)">
                                                ✏️ Editar
                                            </button>
                                            <button type="button" class="btn-delete"
                                                    onclick="confirmarEliminarAsignacion('<%= idAsig %>', '<%= HtmlUtil.escape(nomAsig) %>')">
                                                🗑️ Eliminar
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>

        <!-- Formulario Oculto para Eliminar Asignación -->
        <form id="formEliminarAsignacion" action="<%= ctx %>/asignaciones" method="POST" style="display:none;">
            <input type="hidden" name="accion" value="eliminar">
            <input type="hidden" id="deleteIdAsignacion" name="idAsignacion" value="">
        </form>

        <!-- Modal para Editar Asignación Académica -->
        <div id="modalEditarAsignacion" class="modal-overlay">
            <div class="modal-content">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem; border-bottom: 1px solid var(--border-color); padding-bottom: 0.75rem;">
                    <h3 style="margin: 0; font-size: 1.2rem; color: var(--bg-primary);">Editar Asignación Académica</h3>
                    <button type="button" onclick="cerrarModalEditarAsignacion()" style="background: none; border: none; font-size: 1.5rem; cursor: pointer;">&times;</button>
                </div>
                <form action="<%= ctx %>/asignaciones" method="POST">
                    <input type="hidden" name="accion" value="actualizar">
                    <input type="hidden" id="editIdAsignacion" name="idAsignacion" value="">

                    <div class="form-group" style="margin-bottom: 1rem;">
                        <label for="editNombreAsignacion" class="form-label">Nombre de la Asignación *</label>
                        <input type="text" id="editNombreAsignacion" name="nombre" class="form-control" required>
                    </div>

                    <div class="form-group" style="margin-bottom: 1rem;">
                        <label for="editUmbralAprobacion" class="form-label">Umbral de Aprobación (%) *</label>
                        <input type="text" id="editUmbralAprobacion" name="umbralAprobacion" class="form-control" required>
                        <span class="input-hint">El valor debe ser entre 50% y 100%. Las auditorías previas se revalorizarán automáticamente con las nuevas reglas y umbral.</span>
                    </div>

                    <div class="form-group" style="margin-bottom: 1rem;">
                        <label class="form-label">Reglas Técnicas Vigentes *</label>
                        <div class="rules-selection-box" style="max-height: 220px; overflow-y: auto; margin-top: 0.35rem;">
                            <% if (reglasVigentes != null) {
                                   for (Regla regla : reglasVigentes) { %>
                                <div class="rule-item-row" style="padding: 0.5rem 0.75rem;">
                                    <div class="rule-checkbox-wrapper">
                                        <input type="checkbox" name="reglasIds" value="<%= regla.getId() %>" class="edit-modal-regla-cb" id="edit_regla_<%= regla.getId() %>">
                                    </div>
                                    <div class="rule-details">
                                        <label for="edit_regla_<%= regla.getId() %>" style="font-size: 0.9rem; font-weight: 600; cursor: pointer;">
                                            <%= HtmlUtil.escape(regla.getNombreRepresentativo()) %>
                                        </label>
                                        <span class="badge badge-muted" style="margin-left: 0.5rem; font-size: 0.75rem;">
                                            <%= regla.getPonderacion() %> pts
                                        </span>
                                    </div>
                                </div>
                            <%     }
                               } %>
                        </div>
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 0.75rem; margin-top: 1.25rem;">
                        <button type="button" class="btn btn-secondary" onclick="cerrarModalEditarAsignacion()">Cancelar</button>
                        <button type="submit" class="btn btn-primary">Guardar Cambios y Revalorizar</button>
                    </div>
                </form>
            </div>
        </div>
    </main>

    <script>
        function actualizarContadorReglas() {
            const checkboxes = document.querySelectorAll('input[name="reglasIds"]:checked:not(.edit-modal-regla-cb)');
            const contadorEl = document.getElementById('seleccionadas-reglas-count');
            if (contadorEl) {
                contadorEl.textContent = checkboxes.length;
            }
        }

        function marcarTodasLasReglas(marcar) {
            const checkboxes = document.querySelectorAll('input[name="reglasIds"]:not(.edit-modal-regla-cb)');
            checkboxes.forEach(cb => {
                cb.checked = marcar;
            });
            actualizarContadorReglas();
        }

        function abrirModalEditarAsignacion(id, nombre, umbral, idsReglas) {
            document.getElementById('editIdAsignacion').value = id;
            document.getElementById('editNombreAsignacion').value = nombre;
            document.getElementById('editUmbralAprobacion').value = umbral;
            
            const cbs = document.querySelectorAll('.edit-modal-regla-cb');
            const idsSet = new Set((idsReglas || []).map(String));
            cbs.forEach(cb => {
                cb.checked = idsSet.has(String(cb.value));
            });
            
            document.getElementById('modalEditarAsignacion').style.display = 'flex';
        }

        function cerrarModalEditarAsignacion() {
            document.getElementById('modalEditarAsignacion').style.display = 'none';
        }

        function confirmarEliminarAsignacion(id, nombre) {
            if (confirm('¿Está seguro de eliminar la asignación "' + nombre + '"? Se eliminarán en cascada todas sus auditorías asociadas, hallazgos y retroalimentaciones vinculadas.')) {
                document.getElementById('deleteIdAsignacion').value = id;
                document.getElementById('formEliminarAsignacion').submit();
            }
        }

        // Ejecución al cargar la página para reflejar el estado inicial de selección
        document.addEventListener('DOMContentLoaded', function() {
            actualizarContadorReglas();
        });
    </script>
</body>
</html>
