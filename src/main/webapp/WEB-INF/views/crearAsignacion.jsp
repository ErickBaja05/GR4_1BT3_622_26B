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
    String fechaLimitePrevia = request.getParameter("fechaLimite") != null ? request.getParameter("fechaLimite") : "";
    String intentosPrevios = request.getParameter("intentosPermitidos") != null ? request.getParameter("intentosPermitidos") : (request.getParameter("numeroIntentos") != null ? request.getParameter("numeroIntentos") : "3");
    String idCursoParam = request.getParameter("idCurso") != null ? request.getParameter("idCurso") : "";
    if (idCursoParam.isBlank()) {
        Object idCursoAttr = request.getAttribute("idCurso");
        if (idCursoAttr != null) {
            idCursoParam = String.valueOf(idCursoAttr);
        } else {
            com.gr4.owlaudit.model.Curso cAct = (com.gr4.owlaudit.model.Curso) request.getAttribute("cursoActual");
            if (cAct != null && cAct.getId() != null) {
                idCursoParam = String.valueOf(cAct.getId());
            }
        }
    }

    // Obtención de reglas vigentes (provistas por doGet en AsignacionServlet o fallback resiliente específico del curso)
    @SuppressWarnings("unchecked")
    List<Regla> reglasVigentes = (List<Regla>) request.getAttribute("reglasVigentes");
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("reglas");
    }
    if (reglasVigentes == null) {
        reglasVigentes = (List<Regla>) request.getAttribute("listaReglas");
    }
    if (reglasVigentes == null && idCursoParam != null && !idCursoParam.isBlank()) {
        try {
            reglasVigentes = new ReglaDAOImpl().listarVigentesPorCurso(Long.parseLong(idCursoParam.trim()));
        } catch (Exception ignored) {
            reglasVigentes = java.util.Collections.emptyList();
        }
    }
    if (reglasVigentes == null) {
        reglasVigentes = java.util.Collections.emptyList();
    }

    // Filtro estricto: asegurar que sólo aparezcan reglas pertenecientes al curso actual
    if (idCursoParam != null && !idCursoParam.isBlank() && !reglasVigentes.isEmpty()) {
        try {
            Long idCursoActualLong = Long.parseLong(idCursoParam.trim());
            java.util.List<Regla> reglasDelCurso = new java.util.ArrayList<>();
            for (Regla r : reglasVigentes) {
                if (r.getCurso() != null && idCursoActualLong.equals(r.getCurso().getId())) {
                    reglasDelCurso.add(r);
                }
            }
            reglasVigentes = reglasDelCurso;
        } catch (Exception ignored) {
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
        <div class="card" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
            <div>
                <a href="${pageContext.request.contextPath}/curso" class="back-link">&larr; Volver a Mis Cursos</a>
                <h1 class="card-title" style="margin-top: 0.5rem;">
                    Crear Asignación Académica<%= (request.getAttribute("cursoActual") != null) ? " - " + HtmlUtil.escape(((com.gr4.owlaudit.model.Curso) request.getAttribute("cursoActual")).getNombre()) : "" %>
                </h1>
                <p class="card-subtitle" style="margin-bottom: 0;">
                    Defina el nombre de la asignación, configure el umbral mínimo de aprobación y vincule las reglas técnicas vigentes para la evaluación automatizada.
                </p>
            </div>
            <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                <div style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                    <a href="<%= ctx %>/regla?idCurso=<%= HtmlUtil.escape(idCursoParam) %>" class="btn btn-secondary">
                        📋 Catálogo de Reglas del Curso
                    </a>
                    <a href="<%= ctx %>/regla?vista=registro&idCurso=<%= HtmlUtil.escape(idCursoParam) %>" class="btn btn-primary">
                        ➕ Crear Nueva Regla
                    </a>
                </div>
            <% } %>
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
                <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                    <input type="hidden" name="idCurso" value="<%= HtmlUtil.escape(idCursoParam) %>">
                <% } %>
                
                <!-- Campo: Nombre de la Asignación -->
                <div class="form-group">
                    <label for="nombre" class="form-label">Título de la Asignación *</label>
                    <input type="text" 
                           id="nombre" 
                           name="nombre" 
                           class="form-control" 
                           placeholder="Ej. Entrega Parcial 1" 
                           value="<%= HtmlUtil.escape(nombrePrevio) %>" 
                           required 
                           autofocus>
                    <span class="input-hint">Ingrese una denominación representativa para identificar la asignación académica.</span>
                </div>

                <!-- Campo: Fecha Límite de Entrega -->
                <div class="form-group">
                    <label for="fechaLimite" class="form-label">Fecha Límite *</label>
                    <input type="datetime-local" 
                           id="fechaLimite" 
                           name="fechaLimite" 
                           class="form-control" 
                           value="<%= HtmlUtil.escape(fechaLimitePrevia) %>" 
                           required>
                    <span class="input-hint">La fecha límite debe ser posterior a la fecha actual.</span>
                </div>

                <!-- Campo: Número de Intentos Permitidos -->
                <div class="form-group">
                    <label for="intentosPermitidos" class="form-label">Intentos Permitidos *</label>
                    <input type="number" 
                           id="intentosPermitidos" 
                           name="intentosPermitidos" 
                           class="form-control" 
                           placeholder="Ej. 3" 
                           min="1" 
                           value="<%= HtmlUtil.escape(intentosPrevios) %>" 
                           required>
                    <span class="input-hint">El número de intentos permitidos debe ser al menos 1.</span>
                </div>

                <!-- Campo: Umbral de Aprobación -->
                <div class="form-group">
                    <label for="umbralAprobacion" class="form-label">Umbral de Aprobación (%) *</label>
                    <input type="text" 
                           id="umbralAprobacion" 
                           name="umbralAprobacion" 
                           class="form-control" 
                           placeholder="Ej. 70" 
                           value="<%= HtmlUtil.escape(umbralPrevio) %>" 
                           required>
                    <span class="input-hint">El umbral de aprobación debe estar comprendido entre 50% y 100%.</span>
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
                                <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                                    <a href="<%= ctx %>/regla?vista=registro&idCurso=<%= HtmlUtil.escape(idCursoParam) %>" class="btn-link-action" style="margin-right: 0.75rem; font-weight: 600; color: var(--brand-blue, #2563eb);">➕ Crear nueva regla</a>
                                    &nbsp;|&nbsp;
                                <% } %>
                                <button type="button" class="btn-link-action" onclick="marcarTodasLasReglas(true)">Seleccionar todas</button>
                                &nbsp;|&nbsp;
                                <button type="button" class="btn-link-action" onclick="marcarTodasLasReglas(false)">Deseleccionar todas</button>
                            </div>
                        </div>

                        <% if (reglasVigentes.isEmpty()) { %>
                            <div style="text-align: center; padding: 2rem 1rem; color: var(--text-muted); background: var(--bg-surface, #f8fafc); border-radius: var(--radius-md, 6px); border: 1px dashed var(--border-color, #cbd5e1);">
                                <p style="margin-bottom: 0.5rem; font-size: 1.1rem; font-weight: 600; color: var(--text-main, #1e293b);">⚠️ No existen reglas vigentes disponibles para este curso.</p>
                                <p style="font-size: 0.875rem; margin-bottom: 1rem;">
                                    Para crear una asignación debe existir al menos una regla activa asociada a este curso.
                                </p>
                                <div style="display: flex; gap: 0.75rem; justify-content: center; flex-wrap: wrap;">
                                    <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                                        <a href="<%= ctx %>/regla?vista=registro&idCurso=<%= HtmlUtil.escape(idCursoParam) %>" 
                                           class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                                            ➕ Crear nueva regla para este curso
                                        </a>
                                        <a href="<%= ctx %>/regla?idCurso=<%= HtmlUtil.escape(idCursoParam) %>" 
                                           class="btn btn-secondary" style="display: inline-flex; align-items: center; gap: 0.4rem;">
                                            📋 Catálogo de Reglas del Curso
                                        </a>
                                    <% } else { %>
                                        <a href="<%= ctx %>/regla" class="btn btn-secondary">
                                            Catálogo de Reglas
                                        </a>
                                    <% } %>
                                </div>
                            </div>
                        <% } else { %>
                            <div class="rules-list-container">
                                <% for (Regla regla : reglasVigentes) { 
                                       String strId = String.valueOf(regla.getId());
                                       boolean checked = setReglasPrevias.contains(strId);
                                %>
                                    <div class="rule-item-row" style="display: flex; align-items: center; justify-content: space-between; gap: 1rem; padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-color, #e2e8f0);">
                                        <div style="display: flex; align-items: flex-start; gap: 0.75rem; flex: 1;">
                                            <div class="rule-checkbox-wrapper" style="margin-top: 0.25rem;">
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
                                                    <label for="regla_<%= regla.getId() %>" style="font-weight: 600; cursor: pointer;">
                                                        <%= HtmlUtil.escape(regla.getNombreRepresentativo()) %>
                                                    </label>
                                                    <% if (regla.getTipoMotor() != null) { %>
                                                        <span class="badge badge-muted" style="margin-left: 0.5rem; font-size: 0.75rem;">
                                                            <%= regla.getTipoMotor() %>
                                                        </span>
                                                    <% } %>
                                                </div>
                                                <% if (regla.getDescripcionDetallada() != null && !regla.getDescripcionDetallada().isBlank()) { %>
                                                    <p class="rule-desc" style="font-size: 0.825rem; color: var(--text-muted, #64748b); margin: 0.25rem 0 0 0;"><%= HtmlUtil.escape(regla.getDescripcionDetallada()) %></p>
                                                <% } %>
                                            </div>
                                        </div>
                                        <div style="display: flex; gap: 0.75rem; align-items: center; flex-shrink: 0;">
                                            <div>
                                                <label for="severidad_<%= regla.getId() %>" style="font-size: 0.75rem; display: block; color: var(--text-muted, #64748b); margin-bottom: 0.2rem;">Severidad</label>
                                                <select name="severidad_<%= regla.getId() %>" id="severidad_<%= regla.getId() %>" class="form-control" style="width: 110px; padding: 0.35rem 0.5rem; font-size: 0.85rem;">
                                                    <option value="ALTA">ALTA</option>
                                                    <option value="MEDIA" selected>MEDIA</option>
                                                    <option value="BAJA">BAJA</option>
                                                </select>
                                            </div>
                                            <div>
                                                <label for="ponderacion_<%= regla.getId() %>" style="font-size: 0.75rem; display: block; color: var(--text-muted, #64748b); margin-bottom: 0.2rem;">Ponderación</label>
                                                <input type="number" 
                                                       name="ponderacion_<%= regla.getId() %>" 
                                                       id="ponderacion_<%= regla.getId() %>" 
                                                       class="form-control" 
                                                       min="1" 
                                                       max="100" 
                                                       placeholder="Ej. 25" 
                                                       value="25" 
                                                       style="width: 90px; padding: 0.35rem 0.5rem; font-size: 0.85rem;">
                                            </div>
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
                                           cantidadReglas = ((java.util.Collection<?>) listaR).size();
                                       }
                                   } catch (Exception ignored) {}
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
            <% if (idCursoParam != null && !idCursoParam.isBlank()) { %>
                <input type="hidden" name="idCurso" value="<%= HtmlUtil.escape(idCursoParam) %>">
            <% } %>
        </form>
    </main>

    <script>
        function actualizarContadorReglas() {
            const checkboxes = document.querySelectorAll('input[name="reglasIds"]:checked');
            const contadorEl = document.getElementById('seleccionadas-reglas-count');
            if (contadorEl) {
                contadorEl.textContent = checkboxes.length;
            }
        }

        function marcarTodasLasReglas(marcar) {
            const checkboxes = document.querySelectorAll('input[name="reglasIds"]');
            checkboxes.forEach(cb => {
                cb.checked = marcar;
            });
            actualizarContadorReglas();
        }

        function confirmarEliminarAsignacion(id, nombre) {
            if (confirm('¿Está seguro de eliminar la asignación "' + nombre + '"? Se eliminarán en cascada todas sus auditorías asociadas, hallazgos y retroalimentaciones vinculadas.')) {
                document.getElementById('deleteIdAsignacion').value = id;
                document.getElementById('formEliminarAsignacion').submit();
            }
        }

        // Ejecución al cargar la página para reflejar el estado inicial de selección y validación submit
        document.addEventListener('DOMContentLoaded', function() {
            actualizarContadorReglas();

            const formCrear = document.getElementById('formCrearAsignacion');
            if (formCrear) {
                formCrear.addEventListener('submit', function(e) {
                    const cbs = document.querySelectorAll('input[name="reglasIds"]:checked');
                    if (cbs.length === 0) {
                        e.preventDefault();
                        alert('Se debe asociar al menos una regla de evaluación a la asignación.');
                        const contenedorReglas = document.querySelector('.rules-selection-box');
                        if (contenedorReglas) {
                            contenedorReglas.scrollIntoView({ behavior: 'smooth' });
                            contenedorReglas.style.border = '2px solid var(--danger-color, #dc2626)';
                        }
                    }
                });
            }
        });
    </script>
</body>
</html>
