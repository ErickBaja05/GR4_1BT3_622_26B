package com.gr4.owlaudit.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuditoriaResumenDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private Long numeroEnProyecto;
    private Long numeroEnAsignacion;
    private String tipoAuditoria;
    private String codigoVisible;
    private String etiquetaVisible;
    private String nombreAsignacion;
    private String dictamen;
    private Double umbral;
    private LocalDateTime fechaHora;
    private int puntajeObtenido;
    private int puntajeMaximo;
    private double porcentaje;

    public AuditoriaResumenDTO() {
    }

    public AuditoriaResumenDTO(Long id, LocalDateTime fechaHora, int puntajeObtenido,
                               int puntajeMaximo, double porcentaje) {
        this(id, null, fechaHora, puntajeObtenido, puntajeMaximo, porcentaje);
    }

    public AuditoriaResumenDTO(Long id, Long numeroEnProyecto, LocalDateTime fechaHora,
                               int puntajeObtenido, int puntajeMaximo, double porcentaje) {
        this.id = id;
        this.numeroEnProyecto = numeroEnProyecto;
        this.fechaHora = fechaHora;
        this.puntajeObtenido = puntajeObtenido;
        this.puntajeMaximo = puntajeMaximo;
        this.porcentaje = porcentaje;
    }

    public String getTipoAuditoria() {
        return tipoAuditoria;
    }

    public void setTipoAuditoria(String tipoAuditoria) {
        this.tipoAuditoria = tipoAuditoria;
    }

    public String getCodigoVisible() {
        return codigoVisible;
    }

    public void setCodigoVisible(String codigoVisible) {
        this.codigoVisible = codigoVisible;
    }

    public String getEtiquetaVisible() {
        return etiquetaVisible;
    }

    public void setEtiquetaVisible(String etiquetaVisible) {
        this.etiquetaVisible = etiquetaVisible;
    }

    public String getNombreAsignacion() {
        return nombreAsignacion;
    }

    public void setNombreAsignacion(String nombreAsignacion) {
        this.nombreAsignacion = nombreAsignacion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNumeroEnProyecto() {
        return numeroEnProyecto;
    }

    public void setNumeroEnProyecto(Long numeroEnProyecto) {
        this.numeroEnProyecto = numeroEnProyecto;
    }

    public Long getNumeroEnAsignacion() {
        return numeroEnAsignacion;
    }

    public void setNumeroEnAsignacion(Long numeroEnAsignacion) {
        this.numeroEnAsignacion = numeroEnAsignacion;
    }

    public String getDictamen() {
        return dictamen;
    }

    public void setDictamen(String dictamen) {
        this.dictamen = dictamen;
    }

    public Double getUmbral() {
        return umbral;
    }

    public void setUmbral(Double umbral) {
        this.umbral = umbral;
    }

    public Long getIdVisible() {
        if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria)) {
            return numeroEnAsignacion != null ? numeroEnAsignacion : (numeroEnProyecto != null ? numeroEnProyecto : id);
        }
        return numeroEnProyecto != null ? numeroEnProyecto : id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public void setPuntajeObtenido(int puntajeObtenido) {
        this.puntajeObtenido = puntajeObtenido;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setPuntajeMaximo(int puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public String getFechaFormateada() {
        return fechaHora != null ? fechaHora.format(FORMATTER) : "";
    }
}
