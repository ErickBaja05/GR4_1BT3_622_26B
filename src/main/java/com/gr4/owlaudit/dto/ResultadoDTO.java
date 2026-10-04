package com.gr4.owlaudit.dto;

public class ResultadoDTO {
    private int puntajeObtenido;
    private int puntajeMaximo;
    private double porcentaje;

    public ResultadoDTO() {
    }

    public ResultadoDTO(int puntajeObtenido, int puntajeMaximo, double porcentaje) {
        this.puntajeObtenido = puntajeObtenido;
        this.puntajeMaximo = puntajeMaximo;
        this.porcentaje = porcentaje;
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
        return "ResultadoDTO{" +
                "puntajeObtenido=" + puntajeObtenido +
                ", puntajeMaximo=" + puntajeMaximo +
                ", porcentaje=" + porcentaje +
                '}';
    }
}
