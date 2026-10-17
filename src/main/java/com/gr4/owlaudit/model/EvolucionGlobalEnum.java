package com.gr4.owlaudit.model;

public enum EvolucionGlobalEnum {
    MEJORA("badge-corregido"),
    REGRESION("badge-alta"),
    ESTABLE("badge-media");

    private final String claseBadge;

    EvolucionGlobalEnum(String claseBadge) {
        this.claseBadge = claseBadge;
    }

    public String getClaseBadge() {
        return claseBadge;
    }
}
