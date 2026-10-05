package com.gr4.owlaudit.dto;

/**
 * DTO para la solicitud y atención de retroalimentación técnica sobre hallazgos (CU05 y CU06).
 * Basado en diagramaClasesIncremento2.puml, secuencia5.puml y secuencia6.puml.
 */
public class SolicitudDTO {

    private Long hallazgoId;
    private Long solicitudId;
    private String justificacion;
    private String orientacion;

    // Campos auxiliares opcionales para contexto en las vistas
    private Long estudianteId;
    private String nombreProyecto;
    private String reglaNombre;
    private String severidad;
    private String evidencia;
    private String estado; // "PENDIENTE", "ATENDIDA"
    private String fechaHora;

    public SolicitudDTO() {
    }

    /**
     * Constructor para CU05: Solicitar retroalimentación por parte del estudiante.
     */
    public SolicitudDTO(Long hallazgoId, String justificacion) {
        this.hallazgoId = hallazgoId;
        this.justificacion = justificacion;
    }

    /**
     * Constructor para CU06: Atender retroalimentación por parte del docente.
     */
    public SolicitudDTO(Long solicitudId, String orientacion, boolean esAtencionDocente) {
        this.solicitudId = solicitudId;
        this.orientacion = orientacion;
    }

    /**
     * Constructor completo según diagrama de clases.
     */
    public SolicitudDTO(Long hallazgoId, Long solicitudId, String justificacion, String orientacion) {
        this.hallazgoId = hallazgoId;
        this.solicitudId = solicitudId;
        this.justificacion = justificacion;
        this.orientacion = orientacion;
    }

    public Long getHallazgoId() {
        return hallazgoId;
    }

    public void setHallazgoId(Long hallazgoId) {
        this.hallazgoId = hallazgoId;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getOrientacion() {
        return orientacion;
    }

    public void setOrientacion(String orientacion) {
        this.orientacion = orientacion;
    }

    public Long getEstudianteId() {
        return estudianteId;
    }

    public void setEstudianteId(Long estudianteId) {
        this.estudianteId = estudianteId;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public String getReglaNombre() {
        return reglaNombre;
    }

    public void setReglaNombre(String reglaNombre) {
        this.reglaNombre = reglaNombre;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    @Override
    public String toString() {
        return "SolicitudDTO{" +
                "hallazgoId=" + hallazgoId +
                ", solicitudId=" + solicitudId +
                ", justificacion='" + justificacion + '\'' +
                ", orientacion='" + orientacion + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
