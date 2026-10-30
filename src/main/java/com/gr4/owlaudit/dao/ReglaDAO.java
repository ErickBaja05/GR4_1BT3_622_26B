package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import java.util.List;

public interface ReglaDAO {
    boolean existePorNombre(String nombre);

    void guardar(Regla regla);

    List<Regla> consultarReglasDeEvaluacionVigentes();

    List<Regla> consultarReglasVigentes();

    Regla obtenerPorNombre(String nombre);

    boolean actualizarEstadoVigencia(String nombre, EstadoVigencia nuevoEstado);

    List<Regla> consultarTodasLasReglas();

    void eliminar(Long id);

    void actualizar(Regla regla);

    Regla buscarPorId(Long id);
}
