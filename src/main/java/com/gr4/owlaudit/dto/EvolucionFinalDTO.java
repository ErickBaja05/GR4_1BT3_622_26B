package com.gr4.owlaudit.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EvolucionFinalDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Auditoría Base (más antigua)
    private Long baseId;
    private LocalDateTime fechaHoraBase;
    private int puntajeObtenidoBase;
    private int puntajeMaximoBase;
    private double porcentajeBase;

    // Auditoría Comparada (más reciente)
    private Long comparadaId;
    private LocalDateTime fechaHoraComparada;
    private int puntajeObtenidoComparada;
    private int puntajeMaximoComparada;
    private double porcentajeComparada;

    // Variaciones
    private int variacionPuntaje;
    private double variacionPorcentaje;

    // Lista de comparaciones por regla
    private List<ComparacionReglaDTO> comparaciones;

    // Conteo de hallazgos por estado
    private int totalNuevos;
    private int totalPersistentes;
    private int totalCorregidos;

    // Mensaje para escenarios alternativos (p. ej. "No existen hallazgos en las auditorías comparadas")
    private String mensaje;

    public EvolucionFinalDTO() {
        this.comparaciones = new ArrayList<>();
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public LocalDateTime getFechaHoraBase() {
        return fechaHoraBase;
    }

    public void setFechaHoraBase(LocalDateTime fechaHoraBase) {
        this.fechaHoraBase = fechaHoraBase;
    }

    public int getPuntajeObtenidoBase() {
        return puntajeObtenidoBase;
    }

    public void setPuntajeObtenidoBase(int puntajeObtenidoBase) {
        this.puntajeObtenidoBase = puntajeObtenidoBase;
    }

    public int getPuntajeMaximoBase() {
        return puntajeMaximoBase;
    }

    public void setPuntajeMaximoBase(int puntajeMaximoBase) {
        this.puntajeMaximoBase = puntajeMaximoBase;
    }

    public double getPorcentajeBase() {
        return porcentajeBase;
    }

    public void setPorcentajeBase(double porcentajeBase) {
        this.porcentajeBase = porcentajeBase;
    }

    public Long getComparadaId() {
        return comparadaId;
    }

    public void setComparadaId(Long comparadaId) {
        this.comparadaId = comparadaId;
    }

    public LocalDateTime getFechaHoraComparada() {
        return fechaHoraComparada;
    }

    public void setFechaHoraComparada(LocalDateTime fechaHoraComparada) {
        this.fechaHoraComparada = fechaHoraComparada;
    }

    public int getPuntajeObtenidoComparada() {
        return puntajeObtenidoComparada;
    }

    public void setPuntajeObtenidoComparada(int puntajeObtenidoComparada) {
        this.puntajeObtenidoComparada = puntajeObtenidoComparada;
    }

    public int getPuntajeMaximoComparada() {
        return puntajeMaximoComparada;
    }

    public void setPuntajeMaximoComparada(int puntajeMaximoComparada) {
        this.puntajeMaximoComparada = puntajeMaximoComparada;
    }

    public double getPorcentajeComparada() {
        return porcentajeComparada;
    }

    public void setPorcentajeComparada(double porcentajeComparada) {
        this.porcentajeComparada = porcentajeComparada;
    }

    public int getVariacionPuntaje() {
        return variacionPuntaje;
    }

    public void setVariacionPuntaje(int variacionPuntaje) {
        this.variacionPuntaje = variacionPuntaje;
    }

    public double getVariacionPorcentaje() {
        return variacionPorcentaje;
    }

    public void setVariacionPorcentaje(double variacionPorcentaje) {
        this.variacionPorcentaje = variacionPorcentaje;
    }

    public List<ComparacionReglaDTO> getComparaciones() {
        return comparaciones;
    }

    public void setComparaciones(List<ComparacionReglaDTO> comparaciones) {
        this.comparaciones = comparaciones;
    }

    public int getTotalNuevos() {
        return totalNuevos;
    }

    public void setTotalNuevos(int totalNuevos) {
        this.totalNuevos = totalNuevos;
    }

    public int getTotalPersistentes() {
        return totalPersistentes;
    }

    public void setTotalPersistentes(int totalPersistentes) {
        this.totalPersistentes = totalPersistentes;
    }

    public int getTotalCorregidos() {
        return totalCorregidos;
    }

    public void setTotalCorregidos(int totalCorregidos) {
        this.totalCorregidos = totalCorregidos;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFechaBaseFormateada() {
        return fechaHoraBase != null ? fechaHoraBase.format(FORMATTER) : "";
    }

    public String getFechaComparadaFormateada() {
        return fechaHoraComparada != null ? fechaHoraComparada.format(FORMATTER) : "";
    }
}
