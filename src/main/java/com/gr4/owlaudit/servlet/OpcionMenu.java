package com.gr4.owlaudit.servlet;

import com.gr4.owlaudit.model.RolEnum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Catálogo único de funcionalidades del sistema y los roles que pueden acceder a cada una.
 * Basado en los diagramas de casos de uso:
 *  - Incremento 1: CU01 (Docente), CU02 (Estudiante), CU03 (Estudiante y Docente).
 *  - Incremento 2: CU04 (Estudiante y Docente), CU05 (Estudiante), CU06 (Docente) -> aún no implementados.
 *
 * Lo utilizan VistasServlet (menú y redirección), AutenticacionFilter (control de acceso) y la barra de navegación.
 */
public enum OpcionMenu {

    REGISTRAR_REGLA("regla", "CU01", "Registrar Regla de Evaluación", "Reglas de Calidad",
            "Defina las políticas técnicas que se evaluarán automáticamente en los repositorios.",
            "📐", "/regla", true, RolEnum.DOCENTE),

    REGISTRAR_PROYECTO("proyecto", "CU02", "Registrar Proyecto Académico", "Registrar Proyecto",
            "Vincule el repositorio de GitHub de su proyecto para habilitar las auditorías.",
            "📁", "/proyecto", true, RolEnum.ESTUDIANTE),

    EJECUTAR_AUDITORIA("auditoria", "CU03", "Ejecutar Auditoría de Calidad", "Auditar Repositorio",
            "Inspeccione un repositorio con las reglas vigentes y obtenga su puntaje de calidad.",
            "🔍", "/auditoria", true, RolEnum.ESTUDIANTE, RolEnum.DOCENTE),

    CONSULTAR_EVOLUCION("evolucion", "CU04", "Consultar Evolución de Calidad", "Evolución",
            "Revise el historial de auditorías y la evolución del puntaje del proyecto.",
            "📈", "/evolucion", true, RolEnum.ESTUDIANTE, RolEnum.DOCENTE),

    SOLICITAR_RETROALIMENTACION("solicitarFeedback", "CU05", "Solicitar Retroalimentación", "Solicitar Feedback",
            "Solicite una revisión o aclaración sobre un hallazgo de auditoría.",
            "💬", null, false, RolEnum.ESTUDIANTE),

    ATENDER_RETROALIMENTACION("atenderFeedback", "CU06", "Atender Retroalimentación", "Atender Feedback",
            "Responda las solicitudes de retroalimentación enviadas por los estudiantes.",
            "📝", null, false, RolEnum.DOCENTE);

    private final String clave;
    private final String casoUso;
    private final String titulo;
    private final String tituloCorto;
    private final String descripcion;
    private final String icono;
    private final String ruta;
    private final boolean disponible;
    private final Set<RolEnum> roles;

    OpcionMenu(String clave, String casoUso, String titulo, String tituloCorto, String descripcion,
               String icono, String ruta, boolean disponible, RolEnum... roles) {
        this.clave = clave;
        this.casoUso = casoUso;
        this.titulo = titulo;
        this.tituloCorto = tituloCorto;
        this.descripcion = descripcion;
        this.icono = icono;
        this.ruta = ruta;
        this.disponible = disponible;
        this.roles = Collections.unmodifiableSet(EnumSet.copyOf(Arrays.asList(roles)));
    }

    public boolean permiteRol(RolEnum rol) {
        return rol != null && roles.contains(rol);
    }

    /** Opciones (disponibles o no) asociadas a un rol, en el orden del catálogo. */
    public static List<OpcionMenu> opcionesPara(RolEnum rol) {
        List<OpcionMenu> opciones = new ArrayList<>();
        for (OpcionMenu opcion : values()) {
            if (opcion.permiteRol(rol)) {
                opciones.add(opcion);
            }
        }
        return opciones;
    }

    public static OpcionMenu porClave(String clave) {
        if (clave == null) {
            return null;
        }
        for (OpcionMenu opcion : values()) {
            if (opcion.clave.equals(clave)) {
                return opcion;
            }
        }
        return null;
    }

    /** Busca la opción asociada a la ruta de un Servlet (p. ej. "/regla"). */
    public static OpcionMenu porRuta(String ruta) {
        if (ruta == null) {
            return null;
        }
        for (OpcionMenu opcion : values()) {
            if (ruta.equals(opcion.ruta)) {
                return opcion;
            }
        }
        return null;
    }

    public String getClave() { return clave; }
    public String getCasoUso() { return casoUso; }
    public String getTitulo() { return titulo; }
    public String getTituloCorto() { return tituloCorto; }
    public String getDescripcion() { return descripcion; }
    public String getIcono() { return icono; }
    public String getRuta() { return ruta; }
    public boolean isDisponible() { return disponible; }
    public Set<RolEnum> getRoles() { return roles; }
}
