package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * CU04 - Resultado de comparar dos auditorías (salida "evolucionCalidad").
 * La base es la auditoría más antigua y la comparada la más reciente.
 */
public class EvolucionFinalDTO {
    private ResumenHistorialDTO.AuditoriaHistorial base;
    private ResumenHistorialDTO.AuditoriaHistorial comparada;
    private int variacionPuntaje;
    private double variacionPorcentaje;
    private List<ComparacionRegla> comparaciones = new ArrayList<>();
    private int totalNuevos;
    private int totalPersistentes;
    private int totalCorregidos;

    public EvolucionFinalDTO() {
    }

    public ResumenHistorialDTO.AuditoriaHistorial getBase() {
        return base;
    }

    public void setBase(ResumenHistorialDTO.AuditoriaHistorial base) {
        this.base = base;
    }

    public ResumenHistorialDTO.AuditoriaHistorial getComparada() {
        return comparada;
    }

    public void setComparada(ResumenHistorialDTO.AuditoriaHistorial comparada) {
        this.comparada = comparada;
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

    @Override
    public String toString() {
        return "EvolucionFinalDTO{" +
                "base=" + base +
                ", comparada=" + comparada +
                ", variacionPuntaje=" + variacionPuntaje +
                ", variacionPorcentaje=" + variacionPorcentaje +
                ", comparaciones=" + comparaciones +
                ", totalNuevos=" + totalNuevos +
                ", totalPersistentes=" + totalPersistentes +
                ", totalCorregidos=" + totalCorregidos +
                '}';
    }

    /** Estado de evolución de una regla entre la auditoría base y la comparada. */
    public static class ComparacionRegla {
        private String nombreRepresentativo;
        private String nivelSeveridad;
        private String estado; // "Nuevo", "Persistente" o "Corregido"

        public ComparacionRegla() {
        }

        public ComparacionRegla(String nombreRepresentativo, String nivelSeveridad, String estado) {
            this.nombreRepresentativo = nombreRepresentativo;
            this.nivelSeveridad = nivelSeveridad;
            this.estado = estado;
        }

        public String getNombreRepresentativo() {
            return nombreRepresentativo;
        }

        public void setNombreRepresentativo(String nombreRepresentativo) {
            this.nombreRepresentativo = nombreRepresentativo;
        }

        public String getNivelSeveridad() {
            return nivelSeveridad;
        }

        public void setNivelSeveridad(String nivelSeveridad) {
            this.nivelSeveridad = nivelSeveridad;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        @Override
        public String toString() {
            return "ComparacionRegla{" +
                    "nombreRepresentativo='" + nombreRepresentativo + '\'' +
                    ", nivelSeveridad='" + nivelSeveridad + '\'' +
                    ", estado='" + estado + '\'' +
                    '}';
        }
    }
}
