package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

/** Cumple si el repositorio contiene el archivo indicado (ruta desde la raíz). */
public class EvaluadorPresenciaArchivo implements EvaluadorReglaStrategy {

    @Override
    public boolean evaluar(String parametroExacto, List<String> rutas) {
        return rutas.contains(parametroExacto);
    }
}
