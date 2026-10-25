package com.gr4.owlaudit.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de una nueva asignación de auditoría.
 * Encapsula:
 * - nombre (String): Nombre descriptivo de la asignación.
 * - umbral (String): Umbral recibido desde la interfaz para permitir validación de formatos en el servicio.
 * - reglasIds (List<Long>): Colección de identificadores de las reglas vigentes seleccionadas.
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

    public List<Long> getReglasIds() {
        return reglasIds;
    }

    public void setReglasIds(List<Long> reglasIds) {
        this.reglasIds = (reglasIds != null) ? new ArrayList<>(reglasIds) : new ArrayList<>();
    }
}
