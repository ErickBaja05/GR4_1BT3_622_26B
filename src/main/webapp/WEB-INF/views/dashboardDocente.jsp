<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ page import="com.gr4.owlaudit.dto.UsuarioDTO" %>
        <%@ page import="com.gr4.owlaudit.servlet.LoginServlet" %>
            <%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
                <% UsuarioDTO usuario=(UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO); String
                    ctx=request.getContextPath(); %>
                    <!DOCTYPE html>
                    <html lang="es">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <title>OwlAudit - Panel del Docente</title>
                        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
                    </head>

                    <body>

                        <%@ include file="/WEB-INF/views/fragments/navbar.jspf" %>

                            <main class="container">
                                <div class="card">
                                    <h1 class="card-title">Panel del Docente - <%= usuario !=null ?
                                            HtmlUtil.escape(usuario.getNombre()) : "Docente" %>
                                    </h1>
                                    <p class="card-subtitle" style="margin-bottom: 0;">
                                        Gestione las asignaciones académicas, configure las reglas técnicas de
                                        evaluación y atienda retroalimentaciones de los estudiantes.
                                    </p>
                                </div>

                                <h2 class="section-title">Gestión Académica y Evaluación</h2>
                                <div class="menu-grid">
                                    <!-- HU02: Crear Asignación (Control de navegación para la funcionalidad) -->
                                    <div class="card menu-card">
                                        <div class="menu-meta">
                                            <span class="menu-icon">📋</span>
                                            <span class="badge badge-docente">HU02</span>
                                        </div>
                                        <h3>Crear Asignación</h3>
                                        <p>Configure los parámetros de una nueva asignación, defina el umbral de
                                            aprobación y vincule el conjunto de reglas técnicas vigentes.</p>
                                        <a class="btn btn-primary btn-block" href="<%= ctx %>/asignaciones"
                                            id="btnNavegarCrearAsignacion">Crear Asignación</a>
                                    </div>

                                    <!-- CU01: Catálogo de Reglas Técnicas -->
                                    <div class="card menu-card">
                                        <div class="menu-meta">
                                            <span class="menu-icon">📐</span>
                                            <span class="badge badge-muted">CU01</span>
                                        </div>
                                        <h3>Catálogo de Reglas Técnicas</h3>
                                        <p>Consulte las políticas técnicas configuradas, configure su vigencia y
                                            registre nuevas reglas.</p>
                                        <a class="btn btn-secondary btn-block" href="<%= ctx %>/regla">Gestionar
                                            Reglas</a>
                                    </div>

                                    <!-- CU06: Atender Retroalimentación -->
                                    <div class="card menu-card">
                                        <div class="menu-meta">
                                            <span class="menu-icon">📝</span>
                                            <span class="badge badge-muted">CU06</span>
                                        </div>
                                        <h3>Atender Retroalimentación</h3>
                                        <p>Responda las solicitudes de retroalimentación enviadas por los estudiantes
                                            sobre los hallazgos de auditoría.</p>
                                        <a class="btn btn-secondary btn-block"
                                            href="<%= ctx %>/retroalimentacion?accion=atender">Revisar Solicitudes</a>
                                    </div>

                                    <!-- CU04: Consultar Evolución de Calidad -->
                                    <div class="card menu-card">
                                        <div class="menu-meta">
                                            <span class="menu-icon">📈</span>
                                            <span class="badge badge-muted">CU04</span>
                                        </div>
                                        <h3>Consultar Evolución</h3>
                                        <p>Revise el historial de auditorías y la evolución del puntaje de calidad de
                                            los proyectos.</p>
                                        <a class="btn btn-secondary btn-block" href="<%= ctx %>/evolucion">Ver
                                            Evolución</a>
                                    </div>
                                </div>
                            </main>

                    </body>

                    </html>