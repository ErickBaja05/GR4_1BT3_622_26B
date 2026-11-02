package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.NivelSeveridadEnum;
import java.util.ArrayList;
import java.util.List;

public class ResultadoDTO {
    private Long idAuditoria;
    private Long numeroEnProyecto;
    private Long numeroEnAsignacion;
    private String tipoAuditoria;
    private String codigoVisible;
    private String etiquetaVisible;
    private int puntajeObtenido;
    private int puntajeMaximo;
    private double porcentaje;
    private String dictamen;
    private List<DetalleResultadoDTO> detalles = new ArrayList<>();
    private double umbralAprobacion;

    public Long getIdAuditoria() {
        return idAuditoria;
    }

    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }

    public Long getNumeroEnProyecto() {
        return numeroEnProyecto;
    }

    public void setNumeroEnProyecto(Long numeroEnProyecto) {
        this.numeroEnProyecto = numeroEnProyecto;
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

    public Long getNumeroEnAsignacion() {
        return numeroEnAsignacion;
    }

    public void setNumeroEnAsignacion(Long numeroEnAsignacion) {
        this.numeroEnAsignacion = numeroEnAsignacion;
    }

    public Long getIdVisible() {
        if ("ASIGNACION".equalsIgnoreCase(tipoAuditoria)) {
            return numeroEnAsignacion != null ? numeroEnAsignacion : (numeroEnProyecto != null ? numeroEnProyecto : idAuditoria);
        }
        return numeroEnProyecto != null ? numeroEnProyecto : idAuditoria;
    }

    public double getUmbralAprobacion() {
        return umbralAprobacion;
    }

    public void setUmbralAprobacion(double umbralAprobacion) {
        this.umbralAprobacion = umbralAprobacion;
    }

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

    public String getDictamen() {
        return dictamen;
    }

    public void setDictamen(String dictamen) {
        this.dictamen = dictamen;
    }

    public List<DetalleResultadoDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleResultadoDTO> detalles) {
        this.detalles = detalles;
    }

    public static class DetalleResultadoDTO {
        private String regla;
        private boolean cumple;
        private int puntosObtenidos;
        private int ponderacion;
        private NivelSeveridadEnum nivelSeveridad;
        private Long hallazgoId;
        private String evidencia;
        private String recomendacion;

        public DetalleResultadoDTO() {
        }

        public DetalleResultadoDTO(String regla, boolean cumple, int puntosObtenidos, int ponderacion) {
            this.regla = regla;
            this.cumple = cumple;
            this.puntosObtenidos = puntosObtenidos;
            this.ponderacion = ponderacion;
        }

        public DetalleResultadoDTO(String regla, boolean cumple, int puntosObtenidos,
                                  int ponderacion, NivelSeveridadEnum nivelSeveridad) {
            this.regla = regla;
            this.cumple = cumple;
            this.puntosObtenidos = puntosObtenidos;
            this.ponderacion = ponderacion;
            this.nivelSeveridad = nivelSeveridad;
        }

        public DetalleResultadoDTO(String regla, boolean cumple, int puntosObtenidos,
                                  int ponderacion, NivelSeveridadEnum nivelSeveridad,
                                  Long hallazgoId, String evidencia, String recomendacion) {
            this.regla = regla;
            this.cumple = cumple;
            this.puntosObtenidos = puntosObtenidos;
            this.ponderacion = ponderacion;
            this.nivelSeveridad = nivelSeveridad;
            this.hallazgoId = hallazgoId;
            this.evidencia = evidencia;
            this.recomendacion = recomendacion;
        }

        public String getRegla() {
            return regla;
        }

        public String getNombreRegla() {
            return regla;
        }

        public void setRegla(String regla) {
            this.regla = regla;
        }

        public Long getIdHallazgo() {
            return hallazgoId;
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
}
