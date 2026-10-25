package com.gr4.owlaudit.model;

/**
 * Enumerado que define los estados de vigencia de una regla técnica de evaluación.
 * Restringe los valores permitidos únicamente a VIGENTE y NO_VIGENTE.
 */
public enum EstadoVigencia {
    VIGENTE,
    NO_VIGENTE;

    public String construirMensajeExito() {
        return (this == NO_VIGENTE)
                ? "La regla fue desactivada para nuevas evaluaciones."
                : "La regla fue reactivada y vuelve a estar disponible para su aplicación.";
    }
}
