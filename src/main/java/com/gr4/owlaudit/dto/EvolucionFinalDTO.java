package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que contiene el resultado consolidado de la evolución de calidad entre dos auditorías (CU04).
 * Basado en diagramaClasesIncremento2.puml, secuencia4.puml y el caso de uso consultarEvolucionCalidad.md.
 */
public class EvolucionFinalDTO {

    private Long proyectoId;
    private String nombreProyecto;
    private Long baseId;
    private Long comparadaId;
    private String fechaBase;
    private String fechaComparada;

    private int puntajeBase;
    private int puntajeComparada;
    private int variacionPuntaje;

    private double porcentajeBase;
    private double porcentajeComparada;
    private double variacionPorcentaje;

    private int totalNuevos;
    private int totalCorregidos;
    private int totalPersistentes;

    private List<ReglaComparadaDTO> detallesReglas;

    public EvolucionFinalDTO() {
        this.detallesReglas = new ArrayList<>();
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public Long getComparadaId() {
        return comparadaId;
    }

    public void setComparadaId(Long comparadaId) {
        this.comparadaId = comparadaId;
    }

    public String getFechaBase() {
        return fechaBase;
    }

    public void setFechaBase(String fechaBase) {
        this.fechaBase = fechaBase;
    }

    public String getFechaComparada() {
        return fechaComparada;
    }

    public void setFechaComparada(String fechaComparada) {
        this.fechaComparada = fechaComparada;
    }

    public int getPuntajeBase() {
        return puntajeBase;
    }

    public void setPuntajeBase(int puntajeBase) {
        this.puntajeBase = puntajeBase;
    }

    public int getPuntajeComparada() {
        return puntajeComparada;
    }

    public void setPuntajeComparada(int puntajeComparada) {
        this.puntajeComparada = puntajeComparada;
    }

    public int getVariacionPuntaje() {
        return variacionPuntaje;
    }

    public void setVariacionPuntaje(int variacionPuntaje) {
        this.variacionPuntaje = variacionPuntaje;
    }

    public double getPorcentajeBase() {
        return porcentajeBase;
    }

    public void setPorcentajeBase(double porcentajeBase) {
        this.porcentajeBase = porcentajeBase;
    }

    public double getPorcentajeComparada() {
        return porcentajeComparada;
    }

    public void setPorcentajeComparada(double porcentajeComparada) {
        this.porcentajeComparada = porcentajeComparada;
    }

    public double getVariacionPorcentaje() {
        return variacionPorcentaje;
    }

    public void setVariacionPorcentaje(double variacionPorcentaje) {
        this.variacionPorcentaje = variacionPorcentaje;
    }

    public int getTotalNuevos() {
        return totalNuevos;
    }

    public void setTotalNuevos(int totalNuevos) {
        this.totalNuevos = totalNuevos;
    }

    public int getTotalCorregidos() {
        return totalCorregidos;
    }

    public void setTotalCorregidos(int totalCorregidos) {
        this.totalCorregidos = totalCorregidos;
    }

    public int getTotalPersistentes() {
        return totalPersistentes;
    }

    public void setTotalPersistentes(int totalPersistentes) {
        this.totalPersistentes = totalPersistentes;
    }

    public List<ReglaComparadaDTO> getDetallesReglas() {
        return detallesReglas;
    }

    public void setDetallesReglas(List<ReglaComparadaDTO> detallesReglas) {
        this.detallesReglas = detallesReglas;
    }

    public void agregarDetalle(ReglaComparadaDTO detalle) {
        if (this.detallesReglas == null) {
            this.detallesReglas = new ArrayList<>();
        }
        this.detallesReglas.add(detalle);
    }

    @Override
    public String toString() {
        return "EvolucionFinalDTO{" +
                "baseId=" + baseId +
                ", comparadaId=" + comparadaId +
                ", variacionPuntaje=" + variacionPuntaje +
                ", variacionPorcentaje=" + variacionPorcentaje +
                ", totalNuevos=" + totalNuevos +
                ", totalCorregidos=" + totalCorregidos +
                ", totalPersistentes=" + totalPersistentes +
                '}';
    }

    /**
     * DTO que representa la comparación del cumplimiento de una regla entre ambas auditorías.
     */
    public static class ReglaComparadaDTO {
        private Long hallazgoId;
        private String nombreRegla;
        private String severidad;
        private boolean cumpleBase;
        private boolean cumpleComparada;
        private int puntosBase;
        private int puntosComparada;
        private String estado; // "Nuevo", "Corregido", "Persistente", "Sin Hallazgo"

        public ReglaComparadaDTO() {
        }

        public ReglaComparadaDTO(Long hallazgoId, String nombreRegla, String severidad,
                                 boolean cumpleBase, boolean cumpleComparada,
                                 int puntosBase, int puntosComparada, String estado) {
            this.hallazgoId = hallazgoId;
            this.nombreRegla = nombreRegla;
            this.severidad = severidad;
            this.cumpleBase = cumpleBase;
            this.cumpleComparada = cumpleComparada;
            this.puntosBase = puntosBase;
            this.puntosComparada = puntosComparada;
            this.estado = estado;
        }

        public Long getHallazgoId() {
            return hallazgoId;
        }

        public void setHallazgoId(Long hallazgoId) {
            this.hallazgoId = hallazgoId;
        }

        public String getNombreRegla() {
            return nombreRegla;
        }

        public void setNombreRegla(String nombreRegla) {
            this.nombreRegla = nombreRegla;
        }

        public String getSeveridad() {
            return severidad;
        }

        public void setSeveridad(String severidad) {
            this.severidad = severidad;
        }

        public boolean isCumpleBase() {
            return cumpleBase;
        }

        public void setCumpleBase(boolean cumpleBase) {
            this.cumpleBase = cumpleBase;
        }

        public boolean isCumpleComparada() {
            return cumpleComparada;
        }

        public void setCumpleComparada(boolean cumpleComparada) {
            this.cumpleComparada = cumpleComparada;
        }

        public int getPuntosBase() {
            return puntosBase;
        }

        public void setPuntosBase(int puntosBase) {
            this.puntosBase = puntosBase;
        }

        public int getPuntosComparada() {
            return puntosComparada;
        }

        public void setPuntosComparada(int puntosComparada) {
            this.puntosComparada = puntosComparada;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        @Override
        public String toString() {
            return "ReglaComparadaDTO{" +
                    "nombreRegla='" + nombreRegla + '\'' +
                    ", severidad='" + severidad + '\'' +
                    ", estado='" + estado + '\'' +
                    ", cumpleBase=" + cumpleBase +
                    ", cumpleComparada=" + cumpleComparada +
                    '}';
        }
    }
}
