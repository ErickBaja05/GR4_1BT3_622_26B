package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaAsignacionDTO;

/**
 * Interfaz de la capa de servicio para la gestión de asignaciones de auditoría.
 */
public interface AsignacionesService {

    /**
     * Procesa la creación y persistencia de una nueva asignación a partir del DTO.
     *
     * @param dto Datos de la asignación a crear.
     * @return true si la asignación fue creada exitosamente, false en caso contrario.
     */
    boolean crearAsignacion(NuevaAsignacionDTO dto);
}
