package com.gr4.owlaudit.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResumenHistorialDTO {

    private Long proyectoId;
    private List<AuditoriaItemDTO> historialAuditorias;

    public ResumenHistorialDTO() {
        this.historialAuditorias = new ArrayList<>();
    }

    public ResumenHistorialDTO(Long proyectoId, List<AuditoriaItemDTO> historialAuditorias) {
        this.proyectoId = proyectoId;
        this.historialAuditorias = historialAuditorias != null ? historialAuditorias : new ArrayList<>();
    }

    public Long getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Long proyectoId) {
        this.proyectoId = proyectoId;
    }

    public List<AuditoriaItemDTO> getHistorialAuditorias() {
        return historialAuditorias;
    }

    public void setHistorialAuditorias(List<AuditoriaItemDTO> historialAuditorias) {
        this.historialAuditorias = historialAuditorias;
    }

    public static class AuditoriaItemDTO {
        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        private Long auditoriaId;
        private LocalDateTime fechaHora;
        private int puntajeObtenido;
        private int puntajeMaximo;
        private double porcentaje;

        public AuditoriaItemDTO() {
        }

        public AuditoriaItemDTO(Long auditoriaId, LocalDateTime fechaHora, int puntajeObtenido,
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

        public String getFechaFormateada() {
            return fechaHora != null ? fechaHora.format(FORMATTER) : "";
        }
    }
}
