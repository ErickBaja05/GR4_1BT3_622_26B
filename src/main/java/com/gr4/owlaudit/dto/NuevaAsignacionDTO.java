package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva asignación de auditoría.
 * Encapsula el nombre de la asignación, el umbral de aprobación y la colección de identificadores
 * de las reglas vigentes seleccionadas para la asignación.
 */
public class NuevaAsignacionDTO {

    private String nombre;
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

    public List<Long> getReglasIds() {
        return reglasIds;
    }

    public void setReglasIds(List<Long> reglasIds) {
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }
}
