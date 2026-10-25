package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.service.AsignacionesService;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsignacionesServiceImpl implements AsignacionesService {

    private final AsignacionesDAO asignacionesDAO;
    private final ReglaDAO reglaDAO;

    public AsignacionesServiceImpl() {
        this.asignacionesDAO = new AsignacionesDAO();
        this.reglaDAO = new ReglaDAOImpl();
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO) {

        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        // 1. Validar los datos básicos.
        validarNombreAsignacion(dto);

        // 2. Validar y convertir el umbral.
        double valorUmbral = validarYConvertirUmbral(dto.getUmbral());

        // 3. Validar las reglas seleccionadas.
        if (dto.getReglasIds() == null
                || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "Se debe asociar al menos una regla de evaluación a la asignación.");
        }

        List<Regla> reglasParaAsociar = obtenerReglasVigentes(dto.getReglasIds());

        // 4. Crear y persistir la asignación.
        Asignacion nuevaAsignacion = new Asignacion(
                dto.getNombre().trim(),
                valorUmbral,
                reglasParaAsociar);

        return asignacionesDAO.crearAsignacion(nuevaAsignacion);
    }

    private void validarNombreAsignacion(NuevaAsignacionDTO dto) {
        if (dto == null
                || dto.getNombre() == null
                || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la asignación es obligatorio.");
        }
    }

    private double validarYConvertirUmbral(String umbral) {
        if (umbral == null || umbral.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación es obligatorio y debe ser un valor numérico válido.");
        }

        double valorUmbral;

        try {
            valorUmbral = Double.parseDouble(umbral.trim());

            if (!Double.isFinite(valorUmbral)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El umbral ingresado debe ser un valor numérico válido.");
        }

        if (valorUmbral < 50.0 || valorUmbral > 100.0) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación debe encontrarse en el rango válido entre 50% y 100%.");
        }

        return valorUmbral;
    }

    private List<Regla> obtenerReglasVigentes(
            List<Long> reglasIds) {

        List<Regla> reglasVigentesEnCatalogo = reglaDAO.consultarReglasDeEvaluacionVigentes();

        Map<Long, Regla> mapaReglasVigentes = reglasVigentesEnCatalogo.stream()
                .collect(Collectors.toMap(
                        Regla::getId,
                        Function.identity()));

        return reglasIds.stream()
                .distinct()
                .map(id -> {
                    Regla regla = mapaReglasVigentes.get(id);

                    if (regla == null || !regla.esVigente()) {
                        throw new IllegalArgumentException(
                                "La regla con ID " + id
                                        + " no se encuentra en estado vigente.");
                    }

                    return regla;
                })
                .collect(Collectors.toList());
    }
}
