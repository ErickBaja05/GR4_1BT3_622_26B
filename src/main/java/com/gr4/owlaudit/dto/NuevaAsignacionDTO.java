package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva asignación de auditoría.
 * Encapsula:
 * - nombre (String): Nombre descriptivo de la asignación.
 * - umbral (String): Umbral recibido desde la interfaz para permitir validación de formatos en el servicio.
 * - reglasIds (List<Long>): Colección de identificadores de las reglas vigentes seleccionadas.
 *
 * Refactorizaciones Martin Fowler aplicadas:
 * - Encapsulate Collection sobre la lista de identificadores de reglas.
 * - Replace Temp with Query (tieneReglas, obtenerCantidadReglas).
 */
public class NuevaAsignacionDTO {

    private String nombre;
    private String umbral;
    private List<Long> reglasIds;

    public NuevaAsignacionDTO() {
        this.reglasIds = new ArrayList<>();
    }

    public NuevaAsignacionDTO(String nombre, String umbral, List<Long> reglasIds) {
        this.nombre = nombre;
        this.umbral = umbral;
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }

    public NuevaAsignacionDTO(String nombre, Double umbralAprobacion, List<Long> idsReglas) {
        this.nombre = nombre;
        this.umbral = (umbralAprobacion != null) ? String.valueOf(umbralAprobacion) : null;
        this.reglasIds = (idsReglas != null) ? new ArrayList<>(idsReglas) : new ArrayList<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUmbral() {
        return umbral;
    }

    public void setUmbral(String umbral) {
        this.umbral = umbral;
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

    // Métodos alias para compatibilidad y conveniencia adicional
    public List<Long> getIdsReglas() {
        return getReglasIds();
    }

    public void setIdsReglas(List<Long> idsReglas) {
        setReglasIds(idsReglas);
    }

    public Double getUmbralAprobacion() {
        if (this.umbral == null || this.umbral.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(this.umbral.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void setUmbralAprobacion(Double umbralAprobacion) {
        this.umbral = (umbralAprobacion != null) ? String.valueOf(umbralAprobacion) : null;
    }
}
