package com.gr4.owlaudit.dto;

// TODO DECISION D5: la especificación de CU03 también pide informar la lista de reglas activas
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
}
