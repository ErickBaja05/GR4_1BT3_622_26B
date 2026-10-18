package com.gr4.owlaudit.reglas.strategy;

import com.gr4.owlaudit.model.TipoMotorEnum;

public class EvaluadorReglaFactory {

    public EvaluadorReglaStrategy obtenerEvaluador(TipoMotorEnum tipo) {
        switch (tipo) {
            case PRESENCIA_ARCHIVO:
                return new EvaluadorPresenciaArchivo();
            case RESTRICCION_ARCHIVOS:
                return new EvaluadorRestriccionArchivos();
            case ESTRUCTURA_CARPETAS:
                return new EvaluadorEstructuraCarpetas();
            case CONVENCION_COMMITS:
                return new EvaluadorConvencionCommits();
            default:
                throw new IllegalArgumentException("Tipo de motor no soportado: " + tipo);
        }
    }
}
