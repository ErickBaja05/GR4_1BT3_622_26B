package com.gr4.owlaudit.reglas.strategy;

import java.util.List;

public class EvaluadorConvencionCommits implements EvaluadorReglaStrategy {
    @Override
    public boolean evaluar(String parametroExacto, List<String> mensajesCommit) {
        return false; // Stub vacío para Fase Roja
    }
}
