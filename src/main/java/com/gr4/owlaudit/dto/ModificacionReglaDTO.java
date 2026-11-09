package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.EstadoVigencia;

/**
 * Datos para modificar una regla de un curso (HU-02).
 * Refactorización Martin Fowler: Introduce Parameter Object.
 * Solo transporta lo editable: nombre y tipo de motor son inmutables.
 */
public record ModificacionReglaDTO(
        Long idRegla,
        Long idCurso,
        Long idDocente,
        String descripcion,
        EstadoVigencia vigencia) {
}
