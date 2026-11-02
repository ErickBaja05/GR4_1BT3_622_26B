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
    default List<Auditoria> consultarHistorialDeAuditoriasPorUsuario(Long usuarioId) {
        return java.util.Collections.emptyList();
    }
    default Long obtenerSiguienteNumeroEnProyecto(Long proyectoId) {
        return 1L;
    }
    default Long obtenerSiguienteNumeroGeneralEnProyecto(Long proyectoId) {
        return obtenerSiguienteNumeroEnProyecto(proyectoId);
    }
    default Long obtenerSiguienteNumeroEnAsignacion(Long proyectoId, Long asignacionId) {
        return 1L;
    }
    default List<Auditoria> consultarHistorialAuditoriasGenerales(Long proyectoId) {
        return consultarHistorialDeAuditorias(proyectoId);
    }
    default List<Auditoria> consultarHistorialAuditoriasAsignacion(Long proyectoId, Long asignacionId) {
        return java.util.Collections.emptyList();
    }
    default List<Auditoria> consultarAuditoriasDeAsignacionPorUsuario(Long asignacionId, Long usuarioId) {
        return java.util.Collections.emptyList();
    }
    default void resecuenciarAuditoriasDeProyecto(Long proyectoId) {}
    default List<Auditoria> buscarPorAsignacionId(Long asignacionId) {
        return java.util.Collections.emptyList();
    }
    default void actualizarAuditoria(Auditoria auditoria) {}
    default void eliminarAuditoriasDeAsignacion(Long asignacionId) {}
}
