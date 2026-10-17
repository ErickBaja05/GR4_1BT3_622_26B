package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.DeudaTecnicaDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;

import java.util.List;

/**
 * Servicio que calcula la Deuda Tecnica Estimada en minutos a partir de
 * una lista de severidades de hallazgos de auditoria.
 *
 * Reglas de negocio:
 *   - NivelSeveridadEnum.ALTA  -> 120 minutos (2 horas)
 *   - NivelSeveridadEnum.MEDIA ->  60 minutos (1 hora)
 *   - NivelSeveridadEnum.BAJA  ->  30 minutos
 */
public class CalculadoraDeudaTecnica {

    private static final int MINUTOS_ALTA  = 120;
    private static final int MINUTOS_MEDIA =  60;
    private static final int MINUTOS_BAJA  =  30;

    /**
     * Calcula la deuda tecnica estimada acumulando el tiempo de correccion
     * por cada hallazgo segun su nivel de severidad.
     *
     * @param hallazgos lista de niveles de severidad de los hallazgos detectados.
     *                  Puede ser null o vacia; en ese caso la deuda es cero.
     * @return {@link DeudaTecnicaDTO} con el total de minutos, contadores
     *         por nivel y metodos de formato para la vista.
     */
    public DeudaTecnicaDTO calcular(List<NivelSeveridadEnum> hallazgos) {
        int totalMinutos  = 0;
        int cantidadAlta  = 0;
        int cantidadMedia = 0;
        int cantidadBaja  = 0;

        if (hallazgos != null) {
            for (NivelSeveridadEnum nivel : hallazgos) {
                if (nivel == null) {
                    continue;
                }
                switch (nivel) {
                    case ALTA -> {
                        totalMinutos += MINUTOS_ALTA;
                        cantidadAlta++;
                    }
                    case MEDIA -> {
                        totalMinutos += MINUTOS_MEDIA;
                        cantidadMedia++;
                    }
                    case BAJA -> {
                        totalMinutos += MINUTOS_BAJA;
                        cantidadBaja++;
                    }
                }
            }
        }

        return new DeudaTecnicaDTO(totalMinutos, cantidadAlta, cantidadMedia, cantidadBaja);
    }
}
