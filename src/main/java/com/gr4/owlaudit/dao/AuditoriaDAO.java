package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.ResultadoRegla;
import java.util.List;

public interface AuditoriaDAO {
    void registrarAuditoriaEnElHistorial(Auditoria auditoria);
    List<Auditoria> consultarHistorialDeAuditorias(Long proyectoId);
    List<ResultadoRegla> consultarResultadosDeReglas(Long baseId, Long comparadaId);
    default List<Auditoria> consultarTodas() {
        return java.util.Collections.emptyList();
    }
    default Auditoria buscarPorId(Long id) {
        return null;
    }
}
