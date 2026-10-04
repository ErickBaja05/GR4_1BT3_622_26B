package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

/**
 * Cumple si NINGÚN archivo del repositorio está prohibido. Se compara solo el
 * nombre del archivo (lo que va después de la última "/") y se ignoran las carpetas:
 *   "*.jar"  -> nombre termina en ".jar"
 *   ".env"   -> nombre es ".env" o termina en ".env"
 *   "x.txt"  -> nombre igual a "x.txt"
 */
public class EvaluadorRestriccionArchivos implements EvaluadorReglaStrategy {

    @Override
    public boolean evaluar(String parametroExacto, List<String> rutas) {
        for (String ruta : rutas) {
            if (ruta.endsWith("/")) {
                continue;
            }
            String nombre = ruta.substring(ruta.lastIndexOf('/') + 1);
            if (esProhibido(nombre, parametroExacto)) {
                return false;
            }
        }
        return true;
    }

    private boolean esProhibido(String nombre, String parametroExacto) {
        if (parametroExacto.startsWith("*.")) {
            return nombre.endsWith(parametroExacto.substring(1));
        }
        if (parametroExacto.startsWith(".")) {
            return nombre.endsWith(parametroExacto);
        }
        return nombre.equals(parametroExacto);
    }
}
