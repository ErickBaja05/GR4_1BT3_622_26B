<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.gr4.owlaudit.dto.UsuarioDTO" %>
<%@ page import="com.gr4.owlaudit.model.Curso" %>
<%@ page import="com.gr4.owlaudit.model.RolEnum" %>
<%@ page import="com.gr4.owlaudit.servlet.LoginServlet" %>
<%@ page import="com.gr4.owlaudit.common.util.HtmlUtil" %>
<%
    UsuarioDTO usuario = (UsuarioDTO) session.getAttribute(LoginServlet.ATRIBUTO_USUARIO);
    @SuppressWarnings("unchecked")
    List<Curso> cursos = (List<Curso>) request.getAttribute("cursos");
    String ctx = request.getContextPath();
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
    <div class="card">
        <h1 class="card-title">Mis Cursos Académicos</h1>
        <p class="card-subtitle">
            Consulte las asignaciones y políticas técnicas de calidad configuradas exclusivamente para sus cursos inscritos.
        </p>
    </div>

    <% String mensajeError = (String) request.getAttribute("mensajeError");
       if (mensajeError != null) { %>
        <div class="alert alert-error">
            <%= HtmlUtil.escape(mensajeError) %>
        </div>
    <% } %>

    <% String mensajeExito = (String) request.getAttribute("mensajeExito");
       if (mensajeExito != null) { %>
        <div class="alert alert-success">
            <%= HtmlUtil.escape(mensajeExito) %>
        </div>
    <% } %>

    <div class="menu-grid" style="margin-top: 1.5rem;">
        <% if (cursos != null && !cursos.isEmpty()) {
               for (Curso c : cursos) { %>
            <div class="card menu-card">
                <div class="menu-meta">
                    <span class="menu-icon">📚</span>
                    <span class="badge badge-primary"><%= HtmlUtil.escape(c.getCodigo()) %></span>
                </div>
                <h3><%= HtmlUtil.escape(c.getNombre()) %></h3>
                <p>
                    <strong>Periodo:</strong> <%= HtmlUtil.escape(c.getPeriodoAcademico()) %><br>
                    <% if (c.getDocente() != null && c.getDocente().getNombre() != null) { %>
                        <strong>Docente:</strong> <%= HtmlUtil.escape(c.getDocente().getNombre()) %><br>
                    <% } %>
                    <%= c.getDescripcion() != null ? HtmlUtil.escape(c.getDescripcion()) : "" %>
                </p>
                <div style="margin-top: 1rem;">
                    <% if (usuario != null && usuario.getRol() == RolEnum.ESTUDIANTE) { %>
                        <a class="btn btn-primary btn-block"
                           href="<%= ctx %>/asignaciones?idCurso=<%= c.getId() %>">
                            Ver asignaciones del curso
                        </a>
                    <% } else { %>
                        <a class="btn btn-primary btn-block"
                           href="<%= ctx %>/asignaciones?idCurso=<%= c.getId() %>">
                            Administrar asignaciones
                        </a>
                    <% } %>
                </div>
            </div>
        <%     }
           } else { %>
            <div class="card" style="grid-column: 1 / -1; text-align: center; padding: 2rem;">
                <p class="card-subtitle">Actualmente no se encuentra registrado en ningún curso activo.</p>
            </div>
        <% } %>
    </div>
</main>

</body>
</html>

