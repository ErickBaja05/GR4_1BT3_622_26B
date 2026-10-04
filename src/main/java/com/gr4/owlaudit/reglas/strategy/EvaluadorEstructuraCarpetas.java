package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

/** Cumple si el repositorio contiene la carpeta indicada (ruta desde la raíz). */
public class EvaluadorEstructuraCarpetas implements EvaluadorReglaStrategy {

    @Override
    public boolean evaluar(String parametroExacto, List<String> rutas) {
        String carpeta = parametroExacto.endsWith("/")
            ? parametroExacto.substring(0, parametroExacto.length() - 1)
            : parametroExacto;
        return rutas.contains(carpeta + "/");
    }
}
