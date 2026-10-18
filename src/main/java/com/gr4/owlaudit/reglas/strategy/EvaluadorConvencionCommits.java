package com.gr4.owlaudit.reglas.strategy;

import java.util.List;
import java.util.regex.Pattern;

public class EvaluadorConvencionCommits implements EvaluadorReglaStrategy {

    @Override
    public boolean evaluar(String parametroExacto, List<String> mensajesCommit) {
        if (parametroExacto == null || mensajesCommit == null || mensajesCommit.isEmpty()) {
            return false;
        }
        Pattern patron = Pattern.compile(parametroExacto);
        int validos = 0;
        for (String mensaje : mensajesCommit) {
            if (mensaje != null) {
                // Tomamos la primera línea del commit por si tiene salto de línea
                String primeraLinea = mensaje.split("\\R")[0].trim();
                if (patron.matcher(primeraLinea).matches()) {
                    validos++;
                }
            }
        }
        double porcentajeCumplimiento = (double) validos / mensajesCommit.size();
        return porcentajeCumplimiento >= 0.80;
    }
}
