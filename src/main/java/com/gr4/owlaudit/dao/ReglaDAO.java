package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.Regla;
import java.util.List;

public interface ReglaDAO {
    boolean existePorNombre(String nombre);
    void guardar(Regla regla);
    List<Regla> consultarReglasDeEvaluacionVigentes();
}
