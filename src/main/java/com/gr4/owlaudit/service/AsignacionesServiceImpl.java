package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.AsignacionesDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsignacionesServiceImpl implements AsignacionesService {

    private final AsignacionesDAO asignacionesDAO;
    private final ReglaDAO reglaDAO;
    private final ProyectoDAO proyectoDAO;

    public AsignacionesServiceImpl() {
        this(new AsignacionesDAOImpl(), new ReglaDAOImpl(), new ProyectoDAOImpl());
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO) {
        this(asignacionesDAO, reglaDAO, new ProyectoDAOImpl());
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO,
            ProyectoDAO proyectoDAO) {

        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
        this.proyectoDAO = proyectoDAO;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        // 1. Validar los datos básicos.
        validarNombreAsignacion(dto);

        // 2. Validar y convertir el umbral.
        double valorUmbral = validarUmbralAprobacion(dto.getUmbralAprobacion());

        // 3. Validar las reglas seleccionadas.
        if (dto.getReglasIds() == null
                || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException(
                    "Se debe asociar al menos una regla de evaluación a la asignación.");
        }

        List<Regla> reglasParaAsociar = obtenerReglasVigentes(dto.getReglasIds());

        // Inline Temp: eliminar variable temporal 'nuevaAsignacion' de único uso
        return asignacionesDAO.crearAsignacion(
                new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar)
        );

    }

    @Override
    public List<Asignacion> listarAsignaciones() {
        return asignacionesDAO.listarAsignaciones();
    }

    private void validarNombreAsignacion(NuevaAsignacionDTO dto) {
        if (dto == null
                || dto.getNombre() == null
                || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre de la asignación es obligatorio.");
        }
    }

    private double validarUmbralAprobacion(Double umbralAprobacion) {
        if (umbralAprobacion == null || !Double.isFinite(umbralAprobacion)) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación es obligatorio y debe ser un valor numérico válido.");
        }

        if (umbralAprobacion < 50.0 || umbralAprobacion > 100.0) {
            throw new IllegalArgumentException(
                    "El umbral de aprobación debe encontrarse en el rango válido entre 50% y 100%.");
        }

        return umbralAprobacion;
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

    @Override
    public List<Asignacion> consultarAsignacionesDisponibles() {
        return asignacionesDAO.consultarAsignacionesDisponibles();
    }

    @Override
    public List<Regla> consultarReglasDeAsignacion(Long idAsignacion) {
        return asignacionesDAO.consultarReglasDeAsignacion(idAsignacion);
    }

    @Override
    public double consultarUmbralAprobacion(Long idAsignacion) {
        return asignacionesDAO.consultarUmbralAprobacion(idAsignacion);
    }

    /**
     * Genera el resumen consolidado de la asignación.
     * Refactorización Martin Fowler:
     * - Extract Method (armarResumenAsignacionDTO)
     * - Replace Temp with Query (obtenerNombreAsignacion)
     */
    @Override
    public ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion) {
        return armarResumenAsignacionDTO(
                idAsignacion,
                consultarUmbralAprobacion(idAsignacion),
                consultarReglasDeAsignacion(idAsignacion),
                proyectoDAO.listarTodos(),
                obtenerNombreAsignacion(idAsignacion));
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods).
     * Consulta el nombre de la asignación a partir del catálogo de asignaciones disponibles.
     */
    private String obtenerNombreAsignacion(Long idAsignacion) {
        List<Asignacion> asignaciones = consultarAsignacionesDisponibles();
        if (asignaciones == null || idAsignacion == null) {
            return null;
        }
        return asignaciones.stream()
                .filter(a -> a != null && idAsignacion.equals(a.getId()))
                .map(Asignacion::getNombre)
                .findFirst()
                .orElse(null);
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     * Construye y mapea el objeto ResumenAsignacionDTO desacoplando su estructura.
     */
    private ResumenAsignacionDTO armarResumenAsignacionDTO(
            Long idAsignacion,
            double umbral,
            List<Regla> reglas,
            List<Proyecto> proyectos,
            String nombre) {

        ResumenAsignacionDTO resumen = new ResumenAsignacionDTO();
        resumen.setIdAsignacion(idAsignacion);
        resumen.setUmbralAprobacion(umbral);
        resumen.setReglasAsociadas(reglas);
        resumen.setProyectosEstudiante(proyectos);
        resumen.setNombreAsignacion(nombre);
        return resumen;
    }
}
