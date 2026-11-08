package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.AsignacionesDAOImpl;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.CursoDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.dto.ResumenAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.ListadoEstudiantes;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsignacionesServiceImpl implements AsignacionesService {

    private final AsignacionesDAO asignacionesDAO;
    private final ReglaDAO reglaDAO;
    private final ProyectoDAO proyectoDAO;
    private final CursoDAO cursoDAO;
    private final ValidadorAccesoCurso validadorAccesoCurso;
    private AuditoriaService auditoriaService;

    public AsignacionesServiceImpl() {
        this(new AsignacionesDAOImpl(), new ReglaDAOImpl(), new ProyectoDAOImpl(), new CursoDAOImpl());
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO) {
        this(asignacionesDAO, reglaDAO, new ProyectoDAOImpl(), new CursoDAOImpl());
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO,
            ProyectoDAO proyectoDAO) {
        this(asignacionesDAO, reglaDAO, proyectoDAO, (CursoDAO) null);
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO,
            ProyectoDAO proyectoDAO,
            CursoDAO cursoDAO) {
        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
        this.proyectoDAO = proyectoDAO;
        this.cursoDAO = cursoDAO;
        this.validadorAccesoCurso = new ValidadorAccesoCurso(cursoDAO);
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO,
            ProyectoDAO proyectoDAO,
            AuditoriaService auditoriaService) {
        this(asignacionesDAO, reglaDAO, proyectoDAO, new CursoDAOImpl());
        this.auditoriaService = auditoriaService;
    }

    public AsignacionesServiceImpl(
            AsignacionesDAO asignacionesDAO,
            ReglaDAO reglaDAO,
            ProyectoDAO proyectoDAO,
            CursoDAO cursoDAO,
            AuditoriaService auditoriaService) {
        this(asignacionesDAO, reglaDAO, proyectoDAO, cursoDAO);
        this.auditoriaService = auditoriaService;
    }

    public void setAuditoriaService(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    private AuditoriaService getAuditoriaService() {
        if (this.auditoriaService == null) {
            this.auditoriaService = new AuditoriaServiceImpl();
        }
        return this.auditoriaService;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        validarNombreAsignacion(dto);
        double valorUmbral = validarUmbralAprobacion(dto.getUmbralAprobacion());

        if (dto.getReglasIds() == null || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException("Se debe asociar al menos una regla de evaluación a la asignación.");
        }

        List<Regla> reglasParaAsociar = obtenerReglasVigentes(dto.getReglasIds());

        return asignacionesDAO.crearAsignacion(
                new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar)
        );
    }

    @Override
    public Asignacion programarAsignacion(NuevaAsignacionDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la asignación son obligatorios.");
        }
        validarNombreAsignacion(dto);
        validarFechaLimite(dto.getFechaLimite());
        validarIntentosPermitidos(dto.getIntentosPermitidos());
        validarUmbralAprobacion(dto.getUmbralAprobacion());

        Curso curso = null;
        if (dto.getIdCurso() != null && cursoDAO != null) {
            curso = cursoDAO.buscarPorId(dto.getIdCurso());
        }

        List<Regla> reglasParaAsociar = Collections.emptyList();
        if (dto.getReglasIds() != null && !dto.getReglasIds().isEmpty()) {
            reglasParaAsociar = obtenerReglasVigentes(dto.getReglasIds());
        }

        Asignacion nueva = new Asignacion(
                curso,
                dto.getNombre().trim(),
                dto.getFechaLimite(),
                dto.getIntentosPermitidos(),
                dto.getUmbralAprobacion()
        );
        if (!reglasParaAsociar.isEmpty()) {
            nueva.setReglas(reglasParaAsociar);
        }

        return asignacionesDAO.crearAsignacion(nueva);
    }

    private void validarFechaLimite(java.time.LocalDateTime fechaLimite) {
        if (fechaLimite == null || !fechaLimite.isAfter(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha límite debe ser posterior a la fecha actual");
        }
    }

    private void validarIntentosPermitidos(Integer intentosPermitidos) {
        if (intentosPermitidos == null || intentosPermitidos <= 0) {
            throw new IllegalArgumentException("El número de intentos permitidos debe ser al menos 1");
        }
    }

    @Override
    public List<Asignacion> listarAsignaciones() {
        return asignacionesDAO.listarAsignaciones();
    }

    @Override
    public Asignacion buscarPorId(Long id) {
        if (id == null) return null;
        return asignacionesDAO.buscarPorId(id);
    }

    @Override
    public void actualizarAsignacion(Long idAsignacion, NuevaAsignacionDTO dto) {
        if (idAsignacion == null) {
            throw new ExcepcionNegocio("Identificador de asignación no válido.");
        }
        validarNombreAsignacion(dto);
        double valorUmbral = validarUmbralAprobacion(dto.getUmbralAprobacion());

        if (dto.getReglasIds() == null || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException("Se debe asociar al menos una regla de evaluación a la asignación.");
        }

        List<Regla> reglasParaAsociar = obtenerReglasVigentes(dto.getReglasIds());

        Asignacion asig = new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar);
        asig.setId(idAsignacion);

        asignacionesDAO.actualizar(asig, dto.getReglasIds());

        // Revalorización automática de todas las auditorías realizadas en esta asignación
        try {
            getAuditoriaService().revalorizarAuditoriasDeAsignacion(idAsignacion);
        } catch (Exception e) {
            System.err.println("[AsignacionesServiceImpl] Advertencia en revalorización: " + e.getMessage());
        }
    }

    @Override
    public void eliminarAsignacion(Long idAsignacion) {
        if (idAsignacion == null) {
            throw new ExcepcionNegocio("Identificador de asignación no válido.");
        }
        Asignacion existente = asignacionesDAO.buscarPorId(idAsignacion);
        if (existente == null) {
            throw new ExcepcionNegocio("La asignación no existe.");
        }
        asignacionesDAO.eliminar(idAsignacion);
    }

    private void validarNombreAsignacion(NuevaAsignacionDTO dto) {
        if (dto == null || dto.getNombre() == null || dto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la asignación es obligatorio.");
        }
    }

    private double validarUmbralAprobacion(Double umbralAprobacion) {
        if (umbralAprobacion == null || !Double.isFinite(umbralAprobacion)) {
            throw new IllegalArgumentException("El umbral de aprobación es obligatorio y debe ser un valor numérico válido.");
        }

        if (umbralAprobacion < 50.0 || umbralAprobacion > 100.0) {
            throw new IllegalArgumentException("El umbral de aprobación debe estar comprendido entre 50% y 100%");
        }

        return umbralAprobacion;
    }

    private List<Regla> obtenerReglasVigentes(List<Long> reglasIds) {
        List<Regla> reglasVigentesEnCatalogo = reglaDAO.consultarReglasDeEvaluacionVigentes();

        Map<Long, Regla> mapaReglasVigentes = reglasVigentesEnCatalogo.stream()
                .collect(Collectors.toMap(Regla::getId, Function.identity()));

        return reglasIds.stream()
                .distinct()
                .map(id -> {
                    Regla regla = mapaReglasVigentes.get(id);
                    if (regla == null || !regla.esVigente()) {
                        throw new IllegalArgumentException("La regla con ID " + id + " no se encuentra en estado vigente.");
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
    public List<Asignacion> consultarAsignacionesDeCurso(Long idCurso, Long idEstudiante) {
        if (idCurso == null) {
            return Collections.emptyList();
        }
        if (idEstudiante != null) {
            validadorAccesoCurso.validarAccesoEstudianteACurso(idCurso, idEstudiante);
        }
        return asignacionesDAO.consultarAsignacionesDeCurso(idCurso);
    }

    @Override
    public List<Regla> consultarReglasDeAsignacion(Long idAsignacion) {
        return asignacionesDAO.consultarReglasDeAsignacion(idAsignacion);
    }

    @Override
    public double consultarUmbralAprobacion(Long idAsignacion) {
        return asignacionesDAO.consultarUmbralAprobacion(idAsignacion);
    }

    @Override
    public ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion) {
        return generarResumenAsignacion(idAsignacion, null);
    }

    @Override
    public ResumenAsignacionDTO generarResumenAsignacion(Long idAsignacion, Long usuarioId) {
        validarAccesoAsignacion(idAsignacion, usuarioId);

        List<Proyecto> proyectos = (usuarioId != null)
                ? proyectoDAO.listarPorUsuarioId(usuarioId)
                : proyectoDAO.listarTodos();

        return armarResumenAsignacionDTO(
                idAsignacion,
                consultarUmbralAprobacion(idAsignacion),
                consultarReglasDeAsignacion(idAsignacion),
                proyectos,
                obtenerNombreAsignacion(idAsignacion));
    }

    /**
     * Refactoring: Extract Method (Martin Fowler)
     * Extrae y centraliza la validación de acceso de la asignación consultada.
     */
    private void validarAccesoAsignacion(Long idAsignacion, Long usuarioId) {
        if (idAsignacion == null || usuarioId == null) {
            return;
        }
        Asignacion asignacion = asignacionesDAO.buscarPorId(idAsignacion);
        if (asignacion != null && asignacion.getCurso() != null) {
            validadorAccesoCurso.validarAccesoEstudianteACurso(asignacion.getCurso(), usuarioId);
        }
    }

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
