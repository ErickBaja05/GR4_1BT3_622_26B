package com.gr4.owlaudit.common.exception;

public class ExcepcionNegocio extends RuntimeException {
    public ExcepcionNegocio(String mensaje) {
        super(mensaje);
    }

    public ExcepcionNegocio(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
