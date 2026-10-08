package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.NivelSeveridadEnum;
import java.util.ArrayList;
import java.util.List;

public class ResultadoDTO {
    private int puntajeObtenido;
    private int puntajeMaximo;
    private double porcentaje;
    private List<DetalleResultadoDTO> detalles = new ArrayList<>();

    public ResultadoDTO() {
    }

    public ResultadoDTO(int puntajeObtenido, int puntajeMaximo, double porcentaje) {
        this.puntajeObtenido = puntajeObtenido;
        this.puntajeMaximo = puntajeMaximo;
        this.porcentaje = porcentaje;
        this.detalles = new ArrayList<>();
    }

    public ResultadoDTO(int puntajeObtenido, int puntajeMaximo, double porcentaje, List<DetalleResultadoDTO> detalles) {
        this.puntajeObtenido = puntajeObtenido;
        this.puntajeMaximo = puntajeMaximo;
        this.porcentaje = porcentaje;
        this.detalles = detalles != null ? detalles : new ArrayList<>();
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

    public List<DetalleResultadoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleResultadoDTO> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleResultadoDTO {
        private String nombreRegla;
        private boolean cumple;
        private int puntosObtenidos;
        private int ponderacion;
        private NivelSeveridadEnum nivelSeveridad;
        private Long hallazgoId;
        private String evidencia;
        private String recomendacion;

        public DetalleResultadoDTO() {
        }

        public DetalleResultadoDTO(String nombreRegla, boolean cumple, int puntosObtenidos, int ponderacion,
                                   NivelSeveridadEnum nivelSeveridad, Long hallazgoId, String evidencia, String recomendacion) {
            this.nombreRegla = nombreRegla;
            this.cumple = cumple;
            this.puntosObtenidos = puntosObtenidos;
            this.ponderacion = ponderacion;
            this.nivelSeveridad = nivelSeveridad;
            this.hallazgoId = hallazgoId;
            this.evidencia = evidencia;
            this.recomendacion = recomendacion;
        }

        public String getNombreRegla() {
            return nombreRegla;
        }

        public void setNombreRegla(String nombreRegla) {
            this.nombreRegla = nombreRegla;
        }

        public boolean isCumple() {
            return cumple;
        }

        public void setCumple(boolean cumple) {
            this.cumple = cumple;
        }

        public int getPuntosObtenidos() {
            return puntosObtenidos;
        }

        public void setPuntosObtenidos(int puntosObtenidos) {
            this.puntosObtenidos = puntosObtenidos;
        }

        public int getPonderacion() {
            return ponderacion;
        }

        public void setPonderacion(int ponderacion) {
            this.ponderacion = ponderacion;
        }

        public NivelSeveridadEnum getNivelSeveridad() {
            return nivelSeveridad;
        }

        public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) {
            this.nivelSeveridad = nivelSeveridad;
        }

        public Long getHallazgoId() {
            return hallazgoId;
        }

        public void setHallazgoId(Long hallazgoId) {
            this.hallazgoId = hallazgoId;
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

    @Override
    public String toString() {
        return "ResultadoDTO{" +
                "puntajeObtenido=" + puntajeObtenido +
                ", puntajeMaximo=" + puntajeMaximo +
                ", porcentaje=" + porcentaje +
                ", detalles=" + detalles +
                '}';
    }
}

