package com.gr4.owlaudit.dto;

public class ResumenDTO {
    private int puntajeMaximo;

    public ResumenDTO() {
    }

    public ResumenDTO(int puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setPuntajeMaximo(int puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    @Override
    public String toString() {
        return "ResumenDTO{" +
                "puntajeMaximo=" + puntajeMaximo +
                '}';
    }
}
