package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

/**
 * Convención de rutas (la define GitHubClient): relativas a la raíz del
 * repositorio y las carpetas terminan en "/".
 * Ej: [".gitignore", "README.md", "src/", "src/Main.java"]
 */
public interface EvaluadorReglaStrategy {
    boolean evaluar(String parametroExacto, List<String> rutas);
}
