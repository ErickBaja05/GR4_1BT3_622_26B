package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva asignación de auditoría.
 * Encapsula el nombre de la asignación, el umbral de aprobación y la colección de identificadores
 * de las reglas vigentes seleccionadas para la asignación.
 *
 * Refactorización Martin Fowler: Encapsulate Collection sobre la lista de idsReglas.
 */
public class NuevaAsignacionDTO {

    private String nombre;
    private Double umbralAprobacion;
    private List<Long> idsReglas;

    public NuevaAsignacionDTO() {
        this.idsReglas = new ArrayList<>();
    }

    public NuevaAsignacionDTO(String nombre, Double umbralAprobacion, List<Long> idsReglas) {
        this.nombre = nombre;
        this.umbralAprobacion = umbralAprobacion;
        this.idsReglas = (idsReglas != null) ? new ArrayList<>(idsReglas) : new ArrayList<>();
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
     * Refactorización: Encapsulate Collection.
     * Retorna una vista no modificable de los identificadores de reglas seleccionadas.
     */
    public List<Long> getIdsReglas() {
        return Collections.unmodifiableList(idsReglas);
    }

    public void setIdsReglas(List<Long> idsReglas) {
        this.idsReglas = (idsReglas != null) ? new ArrayList<>(idsReglas) : new ArrayList<>();
    }

    public void agregarReglaId(Long reglaId) {
        if (reglaId != null && !this.idsReglas.contains(reglaId)) {
            this.idsReglas.add(reglaId);
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
        return this.idsReglas.size();
    }

    // Alias de conveniencia para compatibilidad con llamadas alternativas
    public List<Long> getReglasIds() {
        return getIdsReglas();
    }

    public void setReglasIds(List<Long> reglasIds) {
        setIdsReglas(reglasIds);
    }
}
