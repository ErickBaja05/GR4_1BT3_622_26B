package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.AsignacionRegla;
import java.util.List;

/**
 * DAO para la entidad de asociación {@link AsignacionRegla}.
 */
public interface AsignacionReglaDAO {
    /**
     * Persiste una lista de asociaciones entre asignación y regla.
     */
    void guardar(List<AsignacionRegla> asignacionesReglas);
}

