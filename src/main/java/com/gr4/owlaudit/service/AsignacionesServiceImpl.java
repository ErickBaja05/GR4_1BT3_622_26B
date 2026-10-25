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

    public AsignacionesServiceImpl(AsignacionesDAO asignacionesDAO, ReglaDAO reglaDAO) {
        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        if (dto == null || dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la asignación es obligatorio.");
        }

        // 1. Escenario 1.2: Validar que exista al menos una regla seleccionada
        if (dto.getReglasIds() == null || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException("Se debe asociar al menos una regla de evaluación a la asignación.");
        }

        // 2. Escenario 2.3: Validar que el umbral no esté vacío y sea numérico válido
        if (dto.getUmbral() == null || dto.getUmbral().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación es obligatorio y debe ser un valor numérico válido.");
        }

        double valorUmbral;
        try {
            valorUmbral = Double.parseDouble(dto.getUmbral().trim());
            if (Double.isNaN(valorUmbral) || Double.isInfinite(valorUmbral)) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El umbral ingresado debe ser un valor numérico válido.");
        }

        // 3. Escenario 2.1 y 2.2: Validar que el umbral se encuentre entre 50% y 100%
        if (valorUmbral < 50.0 || valorUmbral > 100.0) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación debe encontrarse en el rango válido entre 50% y 100%.");
        }

        // 4. Escenario 1.3: Validar que todas las reglas seleccionadas estén vigentes
        List<Regla> reglasVigentesEnCatalogo = reglaDAO.consultarReglasDeEvaluacionVigentes();
        Map<Long, Regla> mapaReglasVigentes = reglasVigentesEnCatalogo.stream()
                .collect(Collectors.toMap(Regla::getId, Function.identity()));

        List<Regla> reglasParaAsociar = dto.getReglasIds().stream()
                .distinct()
                .map(id -> {
                    Regla regla = mapaReglasVigentes.get(id);
                    if (regla == null || !regla.esVigente()) {
                        throw new IllegalArgumentException(
                                "La regla con ID " + id + " no se encuentra en estado vigente.");
                    }
                    return regla;
                })
                .collect(Collectors.toList());

        // 5. Escenario 1.1: Instanciar el modelo ORM Asignacion, invocar a
        // AsignacionesDAO y retornar confirmación
        Asignacion nuevaAsignacion = new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar);
        return asignacionesDAO.crearAsignacion(nuevaAsignacion);
    }
}
