package com.gr4.owlaudit.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva asignación de auditoría.
 * Encapsula:
 * - nombre (String): Nombre descriptivo de la asignación.
 * - umbralAprobacion (Double): Umbral requerido para la aprobación de la auditoría.
 * - reglasIds (List<Long>): Colección de identificadores de las reglas vigentes seleccionadas.
 *
 * Refactorizaciones Martin Fowler aplicadas:
 * - Encapsulate Collection sobre la lista de identificadores de reglas.
 * - Replace Temp with Query (tieneReglas, obtenerCantidadReglas).
 */
public class NuevaAsignacionDTO {

    private Long idCurso;
    private String nombre;
    private LocalDateTime fechaLimite;
    private Integer intentosPermitidos;
    private Double umbralAprobacion;
    private List<Long> reglasIds;

    public NuevaAsignacionDTO() {
        this.reglasIds = new ArrayList<>();
    }

    public NuevaAsignacionDTO(String nombre, Double umbralAprobacion, List<Long> reglasIds) {
        this.nombre = nombre;
        this.umbralAprobacion = umbralAprobacion;
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }

    public NuevaAsignacionDTO(Long idCurso, String nombre, LocalDateTime fechaLimite, Integer intentosPermitidos, Double umbralAprobacion) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.fechaLimite = fechaLimite;
        this.intentosPermitidos = intentosPermitidos;
        this.umbralAprobacion = umbralAprobacion;
        this.reglasIds = new ArrayList<>();
    }

    public NuevaAsignacionDTO(Long idCurso, String nombre, LocalDateTime fechaLimite, Integer intentosPermitidos, Double umbralAprobacion, List<Long> reglasIds) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.fechaLimite = fechaLimite;
        this.intentosPermitidos = intentosPermitidos;
        this.umbralAprobacion = umbralAprobacion;
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }

    public NuevaAsignacionDTO(String nombre, String umbralAprobacion, List<Long> reglasIds) {
        this.nombre = nombre;
        this.umbralAprobacion = parsearUmbralAprobacion(umbralAprobacion);
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }

    private static Double parsearUmbralAprobacion(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(valor.trim());
            return Double.isFinite(parsed) ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Long getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public Integer getIntentosPermitidos() {
        return intentosPermitidos;
    }

    public void setIntentosPermitidos(Integer intentosPermitidos) {
        this.intentosPermitidos = intentosPermitidos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getUmbralAprobacion() {
        return umbralAprobacion;
    }

    public void setUmbralAprobacion(Double umbralAprobacion) {
        this.umbralAprobacion = umbralAprobacion;
    }

    /**
     * Refactorización Martin Fowler: Encapsulate Collection.
     * Retorna una vista no modificable de los identificadores de reglas seleccionadas.
     */
    public List<Long> getReglasIds() {
        return Collections.unmodifiableList(reglasIds);
    }

    public void setReglasIds(List<Long> reglasIds) {
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }

    public void agregarReglaId(Long reglaId) {
        if (reglaId != null && !this.reglasIds.contains(reglaId)) {
            this.reglasIds.add(reglaId);
        }
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods)
     * Reemplaza variables temporales de verificación consultando dinámicamente si existen reglas.
     */
    public boolean tieneReglas() {
        return obtenerCantidadReglas() > 0;
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods)
     * Reemplaza variables temporales de conteo consultando directamente el tamaño de la lista.
     */
    public int obtenerCantidadReglas() {
        return this.reglasIds.size();
    }
}
