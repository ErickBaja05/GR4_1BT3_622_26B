package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.DeudaTecnicaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;

import java.util.ArrayList;
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

    private static final int TIEMPO_HALLAZGO_ALTA = 120;
    private static final int TIEMPO_HALLAZGO_MEDIA = 60;
    private static final int TIEMPO_HALLAZGO_BAJA = 30;

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
        if (hallazgos == null || hallazgos.isEmpty()) {
            return new DeudaTecnicaDTO(0, 0, 0, 0);
        }

        int cantidadAlta = contarPorSeveridad(hallazgos, NivelSeveridadEnum.ALTA);
        int cantidadMedia = contarPorSeveridad(hallazgos, NivelSeveridadEnum.MEDIA);
        int cantidadBaja = contarPorSeveridad(hallazgos, NivelSeveridadEnum.BAJA);

        int totalMinutos = (cantidadAlta * TIEMPO_HALLAZGO_ALTA)
                + (cantidadMedia * TIEMPO_HALLAZGO_MEDIA)
                + (cantidadBaja * TIEMPO_HALLAZGO_BAJA);

        return new DeudaTecnicaDTO(totalMinutos, cantidadAlta, cantidadMedia, cantidadBaja);
    }

    /**
     * Calcula la deuda tecnica a partir del resultado de una auditoria,
     * considerando unicamente las reglas incumplidas (hallazgos reales).
     * Lo usan tanto la auditoria general como la auditoria de asignacion.
     *
     * @param resultado resultado de la auditoria; puede ser null.
     * @return {@link DeudaTecnicaDTO} con el total de minutos y contadores por nivel.
     */
    public DeudaTecnicaDTO calcularDesdeResultado(ResultadoDTO resultado) {
        List<NivelSeveridadEnum> severidades = new ArrayList<>();
        if (resultado != null && resultado.getDetalles() != null) {
            for (ResultadoDTO.DetalleResultadoDTO detalle : resultado.getDetalles()) {
                if (!detalle.isCumple() && detalle.getNivelSeveridad() != null) {
                    severidades.add(detalle.getNivelSeveridad());
                }
            }
        }
        return calcular(severidades);
    }

    /**
     * Cuenta la cantidad de hallazgos que coinciden con el nivel de severidad especificado
     * utilizando Java Streams de forma segura ante elementos nulos.
     */
    private int contarPorSeveridad(List<NivelSeveridadEnum> hallazgos, NivelSeveridadEnum severidad) {
        return (int) hallazgos.stream()
                .filter(nivel -> nivel == severidad)
                .count();
    }
}
