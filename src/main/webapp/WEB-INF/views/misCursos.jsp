<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="java.util.List" %>
        <%@ page import="com.gr4.owlaudit.dto.UsuarioDTO" %>
            <%@ page import="com.gr4.owlaudit.model.Curso" %>
                <%@ page import="com.gr4.owlaudit.model.RolEnum" %>
                    <%@ page import="com.gr4.owlaudit.servlet.LoginServlet" %>
                        <%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
                            <% UsuarioDTO usuario=(UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO); if
                                (usuario==null) { usuario=(UsuarioDTO) session.getAttribute("usuario"); }
                                @SuppressWarnings("unchecked") List<Curso> cursos = (List<Curso>)
                                    request.getAttribute("cursos");
                                    String ctx = request.getContextPath();

                                    // Captura de mensajes de error (compatible con "error" y "mensajeError") y éxito
                                    String mensajeError = (String) request.getAttribute("error");
                                    if (mensajeError == null) {
                                    mensajeError = (String) request.getAttribute("mensajeError");
                                    }
                                    String mensajeExito = (String) request.getAttribute("mensajeExito");

                                    // Valores previos del formulario en caso de error de validación
                                    String nombreIngresado = (String) request.getAttribute("nombreIngresado");
                                    if (nombreIngresado == null && request.getParameter("nombre") != null) {
                                    nombreIngresado = request.getParameter("nombre");
                                    }
                                    String codigoIngresado = (String) request.getAttribute("codigoIngresado");
                                    if (codigoIngresado == null && request.getParameter("codigo") != null) {
                                    codigoIngresado = request.getParameter("codigo");
                                    }
                                    String descripcionIngresada = (String) request.getAttribute("descripcionIngresada");
                                    if (descripcionIngresada == null && request.getParameter("descripcion") != null) {
                                    descripcionIngresada = request.getParameter("descripcion");
                                    }

                                    boolean esDocente = (usuario != null && usuario.getRol() == RolEnum.DOCENTE);
                                    boolean mostrarFormularioAbierto = (mensajeError != null);
                                    String estiloFormulario = mostrarFormularioAbierto ? "margin-top: 1.5rem; display: block;" : "margin-top: 1.5rem; display: none;";
                                    %>
                                    <!DOCTYPE html>
                                    <html lang="es">

                                    <head>
                                        <meta charset="UTF-8">
                                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                        <title>OwlAudit - Mis Cursos</title>
                                        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
                                    </head>

                                    <body>

                                        <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

                                            <main class="container">
                                                <div class="card"
                                                    style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                                                    <div>
                                                        <h1 class="card-title">Mis Cursos Académicos</h1>
                                                        <p class="card-subtitle">
                                                            <% if (esDocente) { %>
                                                                Gestione sus cursos, comparta el código único con sus
                                                                estudiantes y configure las asignaciones académicas.
                                                            <% } else { %>
                                                                Revise sus cursos inscritos y entregue sus asignaciones académicas a tiempo.
                                                            <% } %>
                                                        </p>
                                                    </div>

                                                    <% if (esDocente) { %>
                                                        <button type="button" class="btn btn-primary"
                                                            onclick="toggleFormularioCurso()">
                                                            + Crear un nuevo curso
                                                        </button>
                                                    <% } else { %>
                                                        <button type="button" class="btn btn-primary"
                                                            onclick="abrirModalUnirseCurso()">
                                                            + Unirme a un curso
                                                        </button>
                                                    <% } %>
                                                </div>

                                                <!-- Alertas de Error (CA2, CA3) y Éxito (CA1.1) -->
                                                <% if (mensajeError !=null && !mensajeError.isBlank()) { %>
                                                    <div class="alert alert-error" style="margin-top: 1rem;">
                                                        <%= HtmlUtil.escape(mensajeError) %>
                                                    </div>
                                                    <% } %>
                                                        <% if (mensajeExito != null && !mensajeExito.isBlank()) { %>
                                                            <div class="alert alert-success alert-dismissible" style="margin-top: 1rem;">
                                                                <span>
                                                                    <strong>¡Operación exitosa!</strong> <%= HtmlUtil.escape(mensajeExito) %>
                                                                </span>
                                                            </div>
                                                        <% } %>

                                                                <!-- Formulario Desplegable para Crear Curso (Solo Docente - HU-01) -->
                                                                <% if (esDocente) { %>
                                                                    <div id="contenedorFormCurso" class="card"
                                                                        style="margin-top: 1.5rem;"
                                                                        <%=!mostrarFormularioAbierto ? "hidden" : "" %>>
                                                                        <h2 class="card-title"
                                                                            style="font-size: 1.25rem; margin-bottom: 1rem;">
                                                                            Crear un nuevo curso</h2>
                                                                        <form action="<%= ctx %>/curso" method="post"
                                                                            novalidate>
                                                                            <div class="form-group"
                                                                                style="margin-bottom: 1rem;">
                                                                                <label for="nombre"
                                                                                    style="display: block; font-weight: 600; margin-bottom: 0.4rem;">Nombre
                                                                                    del curso *</label>
                                                                                <input type="text" id="nombre"
                                                                                    name="nombre" class="form-control"
                                                                                    style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 6px;"
                                                                                    placeholder="Ej. Construcción de Software"
                                                                                    value="<%= nombreIngresado != null ? HtmlUtil.escape(nombreIngresado) : "" %>"
                                                                                    required>
                                                                            </div>

                                                                            <div class="form-group"
                                                                                style="margin-bottom: 1rem;">
                                                                                <label for="codigo"
                                                                                    style="display: block; font-weight: 600; margin-bottom: 0.4rem;">Código
                                                                                    único *</label>
                                                                                <input type="text" id="codigo"
                                                                                    name="codigo" class="form-control"
                                                                                    style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 6px;"
                                                                                    placeholder="Ej. CSW-2026B (Solo caracteres alfanuméricos y guiones)"
                                                                                    value="<%= codigoIngresado != null ? HtmlUtil.escape(codigoIngresado) : "" %>"
                                                                                    required>
                                                                            </div>

                                                                            <div class="form-group"
                                                                                style="margin-bottom: 1.25rem;">
                                                                                <label for="descripcion"
                                                                                    style="display: block; font-weight: 600; margin-bottom: 0.4rem;">Descripción
                                                                                    *</label>
                                                                                <textarea id="descripcion"
                                                                                    name="descripcion" rows="3"
                                                                                    class="form-control"
                                                                                    style="width: 100%; padding: 0.6rem; border: 1px solid #ccc; border-radius: 6px;"
                                                                                    placeholder="Ej. Curso de ingeniería de software grupo 4"
                                                                                    required><%= descripcionIngresada != null ? HtmlUtil.escape(descripcionIngresada) : "" %></textarea>
                                                                            </div>

                                                                            <div style="display: flex; gap: 0.75rem;">
                                                                                <button type="submit"
                                                                                    class="btn btn-primary">Crear
                                                                                    curso</button>
                                                                                <button type="button"
                                                                                    class="btn btn-secondary"
                                                                                    onclick="toggleFormularioCurso()">Cancelar</button>
                                                                            </div>
                                                                        </form>
                                                                    </div>
                                                                    <% } %>
                                                                <!-- Modal Emergente para Unirse a Curso (Solo Estudiante) -->
                                                                <% if (!esDocente) { %>
                                                                    <div id="modalUnirseCurso" class="modal-overlay">
                                                                        <div class="modal-content">
                                                                            <h2 class="card-title" style="font-size: 1.25rem; margin-bottom: 0.5rem;">Unirme a un curso</h2>
                                                                            <p class="card-subtitle" style="margin-bottom: 1.25rem;">
                                                                                Pídele a tu profesor el código del curso e introdúcelo aquí.
                                                                            </p>
                                                                            <form action="<%= ctx %>/curso" method="post" novalidate>
                                                                                <input type="hidden" name="accion" value="unirse">
                                                                                <div class="form-group" style="margin-bottom: 1.5rem;">
                                                                                    <label for="codigoCursoEstudiante" class="form-label" style="font-weight: 600;">Código del curso *</label>
                                                                                    <input type="text" id="codigoCursoEstudiante" name="codigo" class="form-control"
                                                                                        placeholder="Código de clase ej: CSW-2026B" required>
                                                                                </div>
                                                                                <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
                                                                                    <button type="button" class="btn btn-secondary" onclick="cerrarModalUnirseCurso()">Cancelar</button>
                                                                                    <button type="submit" class="btn btn-primary">Unirme</button>
                                                                                </div>
                                                                            </form>
                                                                        </div>
                                                                    </div>
                                                                <% } %>
                                                                        <!-- Listado de Tarjetas de Cursos (CA1.2) -->
                                                                        <div class="menu-grid"
                                                                            style="margin-top: 1.5rem;">
                                                                            <% if (cursos !=null && !cursos.isEmpty()) {
                                                                                for (Curso c : cursos) { int
                                                                                totalMatriculados=(c.getListadoEstudiantes()
                                                                                !=null) ?
                                                                                c.getListadoEstudiantes().size() : 0; %>
                                                                                <div class="card menu-card">
                                                                                    <div class="menu-meta">
                                                                                        <span
                                                                                            class="menu-icon">📚</span>
                                                                                        <span
                                                                                            class="badge badge-primary">
                                                                                            <%= HtmlUtil.escape(c.getCodigo())
                                                                                                %>
                                                                                        </span>
                                                                                    </div>
                                                                                    <h3>
                                                                                        <%= HtmlUtil.escape(c.getNombre())
                                                                                            %>
                                                                                    </h3>
                                                                                    <p>
                                                                                        <% if (esDocente && c.getPeriodoAcademico() != null && !c.getPeriodoAcademico().isBlank()) { %>
                                                                                            <strong>Periodo:</strong> <%= HtmlUtil.escape(c.getPeriodoAcademico()) %><br>
                                                                                        <% } %>
                                                                                        <% if (c.getDocente() != null && c.getDocente().getNombre() != null) { %>
                                                                                            <strong>Docente:</strong> <%= HtmlUtil.escape(c.getDocente().getNombre()) %><br>
                                                                                        <% } %>
                                                                                        <% if (esDocente) { %>
                                                                                            <strong>Estudiantes matriculados:</strong> <%= totalMatriculados %><br>
                                                                                        <% } %>
                                                                                        <% if (c.getDescripcion() != null && !c.getDescripcion().isBlank()) { %>
                                                                                            <span style="display: block; margin-top: 0.5rem;">
                                                                                                <%= HtmlUtil.escape(c.getDescripcion()) %>
                                                                                            </span>
                                                                                        <% } %>
                                                                                    </p>
                                                                                    <div style="margin-top: 1rem;">
                                                                                        <% if (!esDocente) { %>
                                                                                            <a class="btn btn-primary btn-block"
                                                                                                href="<%= ctx %>/asignaciones?idCurso=<%= c.getId() %>">
                                                                                                Ver asignaciones
                                                                                            </a>
                                                                                        <% } else { %>
                                                                                            <a class="btn btn-primary btn-block"
                                                                                                href="<%= ctx %>/asignaciones?idCurso=<%= c.getId() %>">
                                                                                                Ver detalles del curso
                                                                                            </a>
                                                                                            <a class="btn btn-secondary btn-block" style="margin-top: 0.5rem;"
                                                                                                href="<%= ctx %>/regla?idCurso=<%= c.getId() %>">
                                                                                                Gestionar Reglas del Curso
                                                                                            </a>
                                                                                        <% } %>
                                                                                    </div>
                                                                                </div>
                                                                                <% } } else { %>
                                                                                    <div class="card"
                                                                                        style="grid-column: 1 / -1; text-align: center; padding: 2rem;">
                                                                                        <p class="card-subtitle">
                                                                                            <% if (esDocente) { %>
                                                                                                Aún no ha creado ningún curso. Utilice el botón <strong>"+ Crear un nuevo curso"</strong> para registrar el primero.
                                                                                            <% } else { %>
                                                                                                Aún no se ha unido a ningún curso. Utilice el botón <strong>"+ Unirme a un curso"</strong> para ingresar el código proporcionado por su docente e inscribirse al primero.

                                                                                            <% } %>
                                                                                        </p>
                                                                                    </div>
                                                                                    <% } %>
                                                                        </div>
                                            </main>
                                            <script>
                                                function toggleFormularioCurso() {
                                                    var formDiv = document.getElementById('contenedorFormCurso');
                                                    if (formDiv) {
                                                        formDiv.hidden = !formDiv.hidden;
                                                    }
                                                }

                                                function abrirModalUnirseCurso() {
                                                    var modal = document.getElementById('modalUnirseCurso');
                                                    if (modal) {
                                                        modal.style.display = 'flex';
                                                    }
                                                }

                                                function cerrarModalUnirseCurso() {
                                                    var modal = document.getElementById('modalUnirseCurso');
                                                    if (modal) {
                                                        modal.style.display = 'none';
                                                    }
                                                }
                                            </script>

                                    </body>

                                    </html>
