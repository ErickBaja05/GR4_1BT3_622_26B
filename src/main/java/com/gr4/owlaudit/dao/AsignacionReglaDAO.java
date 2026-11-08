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

    /**
     * Persiste o actualiza una única asociación.
     */
    void guardar(AsignacionRegla asignacionRegla);

    /**
     * Consulta las reglas asociadas a una asignación con su severidad y ponderación.
     */
    List<AsignacionRegla> listarPorAsignacion(Long asignacionId);

    /**
     * Elimina todas las asociaciones de una asignación dada.
     */
    void eliminarPorAsignacion(Long asignacionId);
}
