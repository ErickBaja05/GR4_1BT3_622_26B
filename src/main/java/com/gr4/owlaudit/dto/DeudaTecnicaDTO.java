package com.gr4.owlaudit.dto;

/**
 * DTO que encapsula el resultado del calculo de Deuda Tecnica Estimada.
 *
 * Contiene el total de minutos acumulados, los contadores por nivel de
 * severidad (ALTA, MEDIA, BAJA) y metodos de formato para la vista.
 *
 * Reglas de negocio aplicadas por CalculadoraDeudaTecnica:
 *   - ALTA  -> 120 minutos (2 horas)
 *   - MEDIA ->  60 minutos (1 hora)
 *   - BAJA  ->  30 minutos
 */
public class DeudaTecnicaDTO {

    private int totalMinutos;
    private int cantidadAlta;
    private int cantidadMedia;
    private int cantidadBaja;

    public DeudaTecnicaDTO() {
    }

    public DeudaTecnicaDTO(int totalMinutos, int cantidadAlta, int cantidadMedia, int cantidadBaja) {
        this.totalMinutos = totalMinutos;
        this.cantidadAlta = cantidadAlta;
        this.cantidadMedia = cantidadMedia;
        this.cantidadBaja = cantidadBaja;
    }

    // -------------------------------------------------------
    // Getters y setters
    // -------------------------------------------------------

    public int getTotalMinutos() {
        return totalMinutos;
    }

    public void setTotalMinutos(int totalMinutos) {
        this.totalMinutos = totalMinutos;
    }

    public int getCantidadAlta() {
        return cantidadAlta;
    }

    public void setCantidadAlta(int cantidadAlta) {
        this.cantidadAlta = cantidadAlta;
    }

    public int getCantidadMedia() {
        return cantidadMedia;
    }

    public void setCantidadMedia(int cantidadMedia) {
        this.cantidadMedia = cantidadMedia;
    }

    public int getCantidadBaja() {
        return cantidadBaja;
    }

    public void setCantidadBaja(int cantidadBaja) {
        this.cantidadBaja = cantidadBaja;
    }

    // -------------------------------------------------------
    // Metodos de formato para la vista
    // -------------------------------------------------------

    /**
     * Formatea el tiempo total como "Xh Ymin".
     * Ejemplos: 330 min -> "5h 30min" | 120 min -> "2h 0min" | 30 min -> "0h 30min"
     *
     * @return cadena legible con horas y minutos
     */
    public String getTiempoFormateado() {
        int horas = totalMinutos / 60;
        int minutos = totalMinutos % 60;
        return horas + "h " + minutos + "min";
    }

    /**
     * Genera un resumen textual descriptivo de la deuda tecnica, incluyendo
     * los contadores por nivel de severidad y el tiempo total formateado.
     *
     * @return texto de resumen para mostrar en la vista
     */
    public String getResumenTexto() {
        return "Deuda Tecnica Estimada: " + getTiempoFormateado()
                + " | Hallazgos ALTA: " + cantidadAlta + " (x120 min)"
                + " | Hallazgos MEDIA: " + cantidadMedia + " (x60 min)"
                + " | Hallazgos BAJA: " + cantidadBaja + " (x30 min)";
    }

    @Override
    public String toString() {
        return "DeudaTecnicaDTO{"
                + "totalMinutos=" + totalMinutos
                + ", cantidadAlta=" + cantidadAlta
                + ", cantidadMedia=" + cantidadMedia
                + ", cantidadBaja=" + cantidadBaja
                + ", tiempoFormateado='" + getTiempoFormateado() + '\''
                + '}';
    }
}
