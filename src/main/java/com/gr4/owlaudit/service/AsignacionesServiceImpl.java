package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AsignacionReglaDAO;
import com.gr4.owlaudit.dao.AsignacionReglaDAOImpl;
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
import com.gr4.owlaudit.dto.ReglaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.AsignacionRegla;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;

import java.util.ArrayList;
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
    private AsignacionReglaDAO asignacionReglaDAO;
    private ValidadorReglaAsignacion validadorReglaAsignacion = new ValidadorReglaAsignacion();
    private AuditoriaService auditoriaService;
    private static final double UMBRAL_MINIMO_APROBACION = 50.0;
    private static final double UMBRAL_MAXIMO_APROBACION = 100.0;

    public AsignacionesServiceImpl() {
        this(new AsignacionesDAOImpl(), new ReglaDAOImpl(), new ProyectoDAOImpl(), new CursoDAOImpl());
        this.asignacionReglaDAO = new AsignacionReglaDAOImpl();
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

    public void setAsignacionReglaDAO(AsignacionReglaDAO asignacionReglaDAO) {
        this.asignacionReglaDAO = asignacionReglaDAO;
    }

    public AsignacionReglaDAO getAsignacionReglaDAO() {
        if (this.asignacionReglaDAO == null) {
            this.asignacionReglaDAO = new AsignacionReglaDAOImpl();
        }
        return this.asignacionReglaDAO;
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

        List<Regla> reglasParaAsociar = validarYObtenerReglas(dto);

        Asignacion asigCreada = asignacionesDAO.crearAsignacion(new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar));
        guardarConfiguracionReglasAsignacion(asigCreada, dto, reglasParaAsociar);
        return asigCreada;
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

        Asignacion asigCreada = asignacionesDAO.crearAsignacion(nueva);
        guardarConfiguracionReglasAsignacion(asigCreada, dto, reglasParaAsociar);
        return asigCreada;
    }

    private void guardarConfiguracionReglasAsignacion(Asignacion asignacion, NuevaAsignacionDTO dto, List<Regla> reglasAsociadas) {
        if (asignacion == null || asignacion.getId() == null) {
            return;
        }
        AsignacionReglaDAO dao = getAsignacionReglaDAO();
        if (dao == null) {
            return;
        }
        // Build listaAR using functional streams (Substitute Algorithm)
        List<AsignacionRegla> listaAR = java.util.stream.Stream.of(
                java.util.Optional.ofNullable(dto)
                        .map(NuevaAsignacionDTO::getReglasConfiguradas)
                        .filter(rcs -> rcs != null && !rcs.isEmpty())
                        .orElse(null),
                java.util.Optional.ofNullable(reglasAsociadas)
                        .filter(rgs -> rgs != null && !rgs.isEmpty())
                        .orElse(null))
            .filter(java.util.Objects::nonNull)
            .flatMap(java.util.Collection::stream)
            .map(item -> {
                if (item instanceof ReglaAsignacionDTO) {
                    ReglaAsignacionDTO rc = (ReglaAsignacionDTO) item;
                    // Validate rule configuration
                    validadorReglaAsignacion.validarRegla(rc);
                    return new AsignacionRegla(
                            asignacion.getId(),
                            rc.getReglaId(),
                            rc.getSeveridad() != null ? rc.getSeveridad() : NivelSeveridadEnum.MEDIA,
                            rc.getPonderacion() != null && rc.getPonderacion() > 0 ? rc.getPonderacion() : 20);
                } else {
                    Regla r = (Regla) item;
                    NivelSeveridadEnum sev = r.getNivelSeveridad() != null ? r.getNivelSeveridad() : NivelSeveridadEnum.MEDIA;
                    int pond = r.getPonderacion() > 0 ? r.getPonderacion() : 20;
                    return new AsignacionRegla(asignacion.getId(), r.getId(), sev, pond);
                }
            })
            .collect(java.util.stream.Collectors.toList());

        if (!listaAR.isEmpty()) {
            dao.guardar(listaAR);
        }
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

        List<Regla> reglasParaAsociar = validarYObtenerReglas(dto);

        Asignacion asig = new Asignacion(dto.getNombre().trim(), valorUmbral, reglasParaAsociar);
        asig.setId(idAsignacion);

        asignacionesDAO.actualizar(asig, dto.getReglasIds());

        // Actualizar asociaciones en asignacion_regla eliminando las anteriores y guardando la nueva selección
        if (getAsignacionReglaDAO() != null) {
            getAsignacionReglaDAO().eliminarPorAsignacion(idAsignacion);
        }
        guardarConfiguracionReglasAsignacion(asig, dto, reglasParaAsociar);

        // Revalorización automática de todas las auditorías realizadas en esta asignación
        try {
            getAuditoriaService().revalorizarAuditoriasDeAsignacion(idAsignacion);
        } catch (Exception e) {
            System.err.println("[AsignacionesServiceImpl] Advertencia en revalorización: " + e.getMessage());
        }
    }

    @Override
    public void asociarReglasAsignacion(Long idAsignacion, List<ReglaAsignacionDTO> reglasParam) {
        if (idAsignacion == null) {
            throw new IllegalArgumentException("El identificador de asignación no puede ser nulo.");
        }

        Asignacion asignacion = asignacionesDAO.buscarPorId(idAsignacion);
        if (asignacion == null) {
            asignacion = new Asignacion();
            asignacion.setId(idAsignacion);
        }

        if (reglasParam == null || reglasParam.isEmpty()) {
            throw new IllegalArgumentException("Debe proporcionar al menos una regla para asociar.");
        }

        reglasParam.forEach(dto -> {
            if (dto == null) {
                throw new IllegalArgumentException("DTO de regla no puede ser nulo.");
            }
            if (dto.getPonderacion() == null || dto.getPonderacion() <= 0) {
                throw new IllegalArgumentException("La ponderación debe ser mayor a 0.");
            }
        });

        List<Long> idsReglas = reglasParam.stream()
                .map(ReglaAsignacionDTO::getReglaId)
                .collect(Collectors.toList());

        asignacionesDAO.actualizar(asignacion, idsReglas);

        if (getAsignacionReglaDAO() != null) {
            List<AsignacionRegla> listaAR = new ArrayList<>();
            for (ReglaAsignacionDTO rDto : reglasParam) {
                NivelSeveridadEnum sev = (rDto.getSeveridad() != null) ? rDto.getSeveridad() : NivelSeveridadEnum.MEDIA;
                listaAR.add(new AsignacionRegla(idAsignacion, rDto.getReglaId(), sev, rDto.getPonderacion()));
            }
            try {
                getAsignacionReglaDAO().guardar(listaAR);
            } catch (Exception e) {
                System.err.println("[AsignacionesServiceImpl] Advertencia al persistir asociarReglasAsignacion: " + e.getMessage());
            }
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

        if (umbralAprobacion < UMBRAL_MINIMO_APROBACION || umbralAprobacion > UMBRAL_MAXIMO_APROBACION) {
            throw new IllegalArgumentException("El umbral de aprobación debe estar comprendido entre 50% y 100%");
        }

        return umbralAprobacion;
    }

    /**
     * Refactoring: Extract Method (Martin Fowler)
     * Exige al menos una regla y devuelve las reglas vigentes seleccionadas.
     */
    private List<Regla> validarYObtenerReglas(NuevaAsignacionDTO dto) {
        if (dto.getReglasIds() == null || dto.getReglasIds().isEmpty()) {
            throw new IllegalArgumentException("Se debe asociar al menos una regla de evaluación a la asignación.");
        }
        return obtenerReglasVigentes(dto.getReglasIds());
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
            if (validadorAccesoCurso != null && !validadorAccesoCurso.esDocenteTitular(idCurso, idEstudiante)) {
                validadorAccesoCurso.validarAccesoEstudianteACurso(idCurso, idEstudiante);
            }
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
        if (idAsignacion == null) {
            return null;
        }
        return consultarAsignacionesDisponibles().stream()
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
