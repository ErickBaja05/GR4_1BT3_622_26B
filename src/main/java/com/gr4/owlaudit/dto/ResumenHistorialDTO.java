package com.gr4.owlaudit.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * CU04 - Historial de auditorías registradas de un proyecto (salida "historialAuditorias").
 */
public class ResumenHistorialDTO {
    private Long proyectoId;
    private List<AuditoriaHistorial> auditorias = new ArrayList<>();

    public ResumenHistorialDTO() {
    }

    public ResumenHistorialDTO(Long proyectoId, List<AuditoriaHistorial> auditorias) {
        this.proyectoId = proyectoId;
        this.auditorias = auditorias;
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public List<AuditoriaHistorial> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriaHistorial> auditorias) {
        this.auditorias = auditorias;
    }

    @Override
    public String toString() {
        return "ResumenHistorialDTO{" +
                "proyectoId=" + proyectoId +
                ", auditorias=" + auditorias +
                '}';
    }

    /** Fila del historial: datos de una auditoría registrada. */
    public static class AuditoriaHistorial {
        private Long auditoriaId;
        private LocalDateTime fechaHora;
        private int puntajeObtenido;
        private int puntajeMaximo;
        private double porcentaje;

        public AuditoriaHistorial() {
        }

        public AuditoriaHistorial(Long auditoriaId, LocalDateTime fechaHora, int puntajeObtenido,
                                  int puntajeMaximo, double porcentaje) {
            this.auditoriaId = auditoriaId;
            this.fechaHora = fechaHora;
            this.puntajeObtenido = puntajeObtenido;
            this.puntajeMaximo = puntajeMaximo;
            this.porcentaje = porcentaje;
        }

        public Long getAuditoriaId() {
            return auditoriaId;
        }

        public void setAuditoriaId(Long auditoriaId) {
            this.auditoriaId = auditoriaId;
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

        @Override
        public String toString() {
            return "AuditoriaHistorial{" +
                    "auditoriaId=" + auditoriaId +
                    ", fechaHora=" + fechaHora +
                    ", puntajeObtenido=" + puntajeObtenido +
                    ", puntajeMaximo=" + puntajeMaximo +
                    ", porcentaje=" + porcentaje +
                    '}';
        }
    }
}
