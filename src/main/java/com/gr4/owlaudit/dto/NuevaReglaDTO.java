package com.gr4.owlaudit.dto;

/**
 * DTO para la creación de una regla de evaluación técnica asociada a un curso.
 * Siguiendo la nueva arquitectura, las reglas ya no almacenan severidad ni ponderación,
 * las cuales pertenecen exclusivamente a la asociación con una asignación (AsignacionRegla).
 */
public class NuevaReglaDTO {
    private Long idCurso;
    private String tipoMotor;
    private String nombreRepresentativo;
    private String descripcionDetallada;
    private String parametroExacto;
    private String nivelSeveridad;
    private int ponderacion;

    public NuevaReglaDTO() {
    }

    public NuevaReglaDTO(String tipoMotor, String nombreRepresentativo, String descripcionDetallada,
                         String parametroExacto) {
        this.tipoMotor = tipoMotor;
        this.nombreRepresentativo = nombreRepresentativo;
        this.descripcionDetallada = descripcionDetallada;
        this.parametroExacto = parametroExacto;
    }

    public NuevaReglaDTO(Long idCurso, String tipoMotor, String nombreRepresentativo, String descripcionDetallada,
                         String parametroExacto) {
        this.idCurso = idCurso;
        this.tipoMotor = tipoMotor;
        this.nombreRepresentativo = nombreRepresentativo;
        this.descripcionDetallada = descripcionDetallada;
        this.parametroExacto = parametroExacto;
    }

    /**
     * Constructor sobrecargado para compatibilidad hacia atrás con suites de pruebas existentes.
     */
    public NuevaReglaDTO(String tipoMotor, String nombreRepresentativo, String descripcionDetallada,
                         String parametroExacto, String nivelSeveridad, int ponderacion) {
        this(tipoMotor, nombreRepresentativo, descripcionDetallada, parametroExacto);
        this.nivelSeveridad = nivelSeveridad;
        this.ponderacion = ponderacion;
    }

    public Long getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }

    public String getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(String tipoMotor) {
        this.tipoMotor = tipoMotor;
    }

    public String getNombreRepresentativo() {
        return nombreRepresentativo;
    }

    public void setNombreRepresentativo(String nombreRepresentativo) {
        this.nombreRepresentativo = nombreRepresentativo;
    }

    public String getDescripcionDetallada() {
        return descripcionDetallada;
    }

    public void setDescripcionDetallada(String descripcionDetallada) {
        this.descripcionDetallada = descripcionDetallada;
    }

    public String getParametroExacto() {
        return parametroExacto;
    }

    public void setParametroExacto(String parametroExacto) {
        this.parametroExacto = parametroExacto;
    }

    public String getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(String nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public int getPonderacion() {
        return ponderacion;
    }

    public void setPonderacion(int ponderacion) {
        this.ponderacion = ponderacion;
    }

    @Override
    public String toString() {
        return "NuevaReglaDTO{" +
                "idCurso=" + idCurso +
                ", tipoMotor='" + tipoMotor + '\'' +
                ", nombreRepresentativo='" + nombreRepresentativo + '\'' +
                ", descripcionDetallada='" + descripcionDetallada + '\'' +
                ", parametroExacto='" + parametroExacto + '\'' +
                '}';
    }
}
