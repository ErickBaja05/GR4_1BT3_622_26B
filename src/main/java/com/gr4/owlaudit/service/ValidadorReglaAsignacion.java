package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.ReglaAsignacionDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;

import java.util.List;

/**
 * Validador de la asociación entre reglas y asignaciones (ponderación y severidad).
 */
public class ValidadorReglaAsignacion {

    public static final int PONDERACION_MINIMA = 1;
    public static final int PONDERACION_MAXIMA = 100;

    public void validar(List<ReglaAsignacionDTO> reglas) {
        if (reglas == null || reglas.isEmpty()) {
            throw new ExcepcionNegocio("Debe asociar al menos una regla de evaluación a la asignación.");
        }
        for (ReglaAsignacionDTO dto : reglas) {
            validarRegla(dto);
        }
    }

    public void validarRegla(ReglaAsignacionDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("La información de la regla asociada no puede ser nula.");
        }
        if (dto.getReglaId() == null) {
            throw new ExcepcionNegocio("El identificador de la regla es obligatorio.");
        }
        if (dto.getPonderacion() == null || dto.getPonderacion() < PONDERACION_MINIMA || dto.getPonderacion() > PONDERACION_MAXIMA) {
            throw new ExcepcionNegocio("La ponderación de la regla debe ser un número entero entre " 
                    + PONDERACION_MINIMA + " y " + PONDERACION_MAXIMA + ".");
        }
        if (dto.getSeveridad() == null) {
            throw new ExcepcionNegocio("El nivel de severidad de la regla es obligatorio (ALTA, MEDIA, BAJA).");
        }
    }
}
