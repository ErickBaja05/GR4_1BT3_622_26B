package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que contiene el historial de auditorías de un proyecto para la previa de comparación (CU04).
 * Basado en diagramaClasesIncremento2.puml y secuencia4.puml.
 */
public class ResumenHistorialDTO {

    private Long proyectoId;
    private String nombreProyecto;
    private List<AuditoriaItemDTO> auditorias;

    public ResumenHistorialDTO() {
        this.auditorias = new ArrayList<>();
    }

    public ResumenHistorialDTO(Long proyectoId, String nombreProyecto) {
        this.proyectoId = proyectoId;
        this.nombreProyecto = nombreProyecto;
        this.auditorias = new ArrayList<>();
    }

    public ResumenHistorialDTO(Long proyectoId, String nombreProyecto, List<AuditoriaItemDTO> auditorias) {
        this.proyectoId = proyectoId;
        this.nombreProyecto = nombreProyecto;
        this.auditorias = (auditorias != null) ? auditorias : new ArrayList<>();
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

    public List<AuditoriaItemDTO> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<AuditoriaItemDTO> auditorias) {
        this.auditorias = auditorias;
    }

    public void agregarAuditoria(AuditoriaItemDTO item) {
        if (this.auditorias == null) {
            this.auditorias = new ArrayList<>();
        }
        this.auditorias.add(item);
    }

    public int getTotalAuditorias() {
        return this.auditorias != null ? this.auditorias.size() : 0;
    }

    @Override
    public String toString() {
        return "ResumenHistorialDTO{" +
                "proyectoId=" + proyectoId +
                ", nombreProyecto='" + nombreProyecto + '\'' +
                ", totalAuditorias=" + getTotalAuditorias() +
                '}';
    }

    /**
     * Elemento individual de una auditoría registrada en el historial.
     */
    public static class AuditoriaItemDTO {
        private Long auditoriaId;
        private String fechaHora;
        private int puntajeObtenido;
        private int puntajeMaximo;
        private double porcentaje;

        public AuditoriaItemDTO() {
        }

        public AuditoriaItemDTO(Long auditoriaId, String fechaHora, int puntajeObtenido, int puntajeMaximo, double porcentaje) {
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

        public String getFechaHora() {
            return fechaHora;
        }

        public void setFechaHora(String fechaHora) {
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
            return "AuditoriaItemDTO{" +
                    "auditoriaId=" + auditoriaId +
                    ", fechaHora='" + fechaHora + '\'' +
                    ", puntajeObtenido=" + puntajeObtenido +
                    ", puntajeMaximo=" + puntajeMaximo +
                    ", porcentaje=" + porcentaje +
                    '}';
        }
    }
}
