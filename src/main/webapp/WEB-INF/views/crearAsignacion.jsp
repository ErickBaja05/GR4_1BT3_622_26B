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
    <style>
        .rules-selection-box {
            background: #ffffff;
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 1rem;
            margin-top: 0.5rem;
            margin-bottom: 1.5rem;
        }
        .rules-toolbar {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding-bottom: 0.75rem;
            margin-bottom: 0.75rem;
            border-bottom: 1px solid var(--border-color);
            flex-wrap: wrap;
            gap: 0.5rem;
        }
        .rule-item-row {
            display: flex;
            align-items: center;
            gap: 0.85rem;
            padding: 0.75rem 1rem;
            border-bottom: 1px solid #F1F5F9;
            transition: background-color 0.15s;
        }
        .rule-item-row:last-child {
            border-bottom: none;
        }
        .rule-item-row:hover {
            background-color: #F8FAFC;
        }
        .rule-checkbox-wrapper {
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .rule-checkbox-wrapper input[type="checkbox"] {
            width: 1.15rem;
            height: 1.15rem;
            cursor: pointer;
            accent-color: var(--brand-blue);
        }
        .rule-details {
            flex: 1;
        }
        .rule-title-line {
            display: flex;
            align-items: center;
            gap: 0.65rem;
            margin-bottom: 0.25rem;
        }
        .rule-title-line label {
            font-weight: 600;
            font-size: 0.95rem;
            color: var(--text-main);
            cursor: pointer;
        }
        .rule-desc {
            font-size: 0.825rem;
            color: var(--text-muted);
            margin: 0;
        }
        .rule-meta-badges {
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }
        .btn-link-action {
            background: none;
            border: none;
            color: var(--brand-blue);
            font-size: 0.85rem;
            font-weight: 600;
            cursor: pointer;
            text-decoration: underline;
            padding: 0;
        }
        .btn-link-action:hover {
            color: var(--brand-blue-hover);
        }
        .input-hint {
            display: block;
            margin-top: 0.35rem;
            font-size: 0.82rem;
            color: var(--text-muted);
        }
    </style>
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
                                <th style="width: 180px; text-align: center;">Reglas Vinculadas</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% 
                               for (Object objAsig : asignacionesExistentes) {
                                   Long idAsig = null;
                                   String nomAsig = "";
                                   Object umbralAsig = "";
                                   int cantidadReglas = 0;
                                   try {
                                       java.lang.reflect.Method mId = objAsig.getClass().getMethod("getId");
                                       idAsig = (Long) mId.invoke(objAsig);
                                   } catch (Exception ignored) {}
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
                                    <td><strong>#<%= idAsig != null ? idAsig : "-" %></strong></td>
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
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
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

        // Ejecución al cargar la página para reflejar el estado inicial de selección
        document.addEventListener('DOMContentLoaded', function() {
            actualizarContadorReglas();
        });
    </script>
</body>
</html>
