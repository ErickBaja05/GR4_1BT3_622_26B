package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.NivelSeveridadEnum;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EvolucionFinalDTO {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Datos de auditoría base
    private Long baseId;
    private LocalDateTime fechaHoraBase;
    private int puntajeObtenidoBase;
    private int puntajeMaximoBase;
    private double porcentajeBase;

    // Datos de auditoría comparada
    private Long comparadaId;
    private LocalDateTime fechaHoraComparada;
    private int puntajeObtenidoComparada;
    private int puntajeMaximoComparada;
    private double porcentajeComparada;

    // Variaciones
    private int variacionPuntaje;
    private double variacionPorcentaje;

    // Comparaciones por regla
    private List<ComparacionRegla> comparaciones;

    // Totales por estado
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

    public List<ComparacionRegla> getComparaciones() {
        return comparaciones;
    }

    public void setComparaciones(List<ComparacionRegla> comparaciones) {
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

    public static class ComparacionRegla {
        private String nombreRepresentativo;
        private NivelSeveridadEnum nivelSeveridad;
        private String estado; // "Nuevo", "Persistente", "Corregido"
        private String evidencia;
        private String recomendacion;
        private Long hallazgoId;

        public ComparacionRegla() {
        }

        public ComparacionRegla(String nombreRepresentativo, NivelSeveridadEnum nivelSeveridad,
                                String estado, String evidencia, String recomendacion) {
            this(nombreRepresentativo, nivelSeveridad, estado, evidencia, recomendacion, null);
        }

        public ComparacionRegla(String nombreRepresentativo, NivelSeveridadEnum nivelSeveridad,
                                String estado, String evidencia, String recomendacion, Long hallazgoId) {
            this.nombreRepresentativo = nombreRepresentativo;
            this.nivelSeveridad = nivelSeveridad;
            this.estado = estado;
            this.evidencia = evidencia;
            this.recomendacion = recomendacion;
            this.hallazgoId = hallazgoId;
        }

        public Long getHallazgoId() {
            return hallazgoId;
        }

        public void setHallazgoId(Long hallazgoId) {
            this.hallazgoId = hallazgoId;
        }

        public String getNombreRepresentativo() {
            return nombreRepresentativo;
        }

        public void setNombreRepresentativo(String nombreRepresentativo) {
            this.nombreRepresentativo = nombreRepresentativo;
        }

        public NivelSeveridadEnum getNivelSeveridad() {
            return nivelSeveridad;
        }

        public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) {
            this.nivelSeveridad = nivelSeveridad;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getEvidencia() {
            return evidencia;
        }

        public void setEvidencia(String evidencia) {
            this.evidencia = evidencia;
        }

        public String getRecomendacion() {
            return recomendacion;
        }

        public void setRecomendacion(String recomendacion) {
            this.recomendacion = recomendacion;
        }
    }
}
