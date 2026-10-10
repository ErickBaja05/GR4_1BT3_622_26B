package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

public class EvaluadorRestriccionArchivos implements EvaluadorReglaStrategy {

    @Override
    public boolean evaluar(String parametroExacto, List<String> rutas) {
        if (rutas == null || parametroExacto == null) {
            return true;
        }

        return rutas.stream()
                .filter(ruta -> !ruta.endsWith("/"))
                .map(this::extraerNombreArchivo)
                .noneMatch(nombre -> esProhibido(nombre, parametroExacto));
    }

    private String extraerNombreArchivo(String ruta) {
        return ruta.substring(ruta.lastIndexOf('/') + 1);
    }

    private boolean esProhibido(String nombre, String parametro) {
        if (parametro.startsWith("*.")) {
            return nombre.endsWith(parametro.substring(1));
        }
        if (parametro.startsWith(".")) {
            return nombre.endsWith(parametro);
        }
        return nombre.equals(parametro);
    }
}