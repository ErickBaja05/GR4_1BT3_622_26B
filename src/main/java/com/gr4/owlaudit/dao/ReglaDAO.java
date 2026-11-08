package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import java.util.List;

public interface ReglaDAO {
    boolean existePorNombre(String nombre);

    void guardar(Regla regla);

    List<Regla> consultarReglasDeEvaluacionVigentes();

    default List<Regla> consultarReglasVigentes() {
        return consultarReglasDeEvaluacionVigentes();
    }

    default Regla obtenerPorNombre(String nombre) {
        return null;
    };

    default boolean actualizarEstadoVigencia(String nombre, EstadoVigencia nuevoEstado) {
        return false;
    };

    default List<Regla> consultarTodasLasReglas() {
        return consultarReglasDeEvaluacionVigentes();
    };

    default boolean existeNombreEnCurso(String nombre, Long idCurso) {
        return false;
    }

    default List<Regla> listarPorCurso(Long idCurso) {
        return List.of();
    }

    default List<Regla> listarVigentesPorCurso(Long idCurso) {
        return List.of();
    }

    default Regla buscarPorId(Long id) {
        return null;
    }

    default Regla obtenerPorIdYCurso(Long idRegla, Long idCurso) {
        return null;
    }

    default Regla obtenerPorNombreYCurso(String nombre, Long idCurso) {
        return null;
    }

    default boolean actualizarEstadoVigenciaPorIdYCurso(Long idRegla, Long idCurso, EstadoVigencia nuevoEstado) {
        return false;
    }
}
