package com.gr4.owlaudit.service;

import java.util.ArrayList;
import java.util.List;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.AuditoriaResumenDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;

public class AuditoriaServiceImpl implements AuditoriaService {

    private static final String MENSAJE_SIN_REGLAS = "No existen reglas de evaluación activas para auditar el proyecto";
    private static final int LIMITE_COMMITS = 30;

    private final ReglaDAO reglaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final GitHubClient gitHubClient;
    private final EvaluadorReglaFactory evaluadorFactory;
    private final ProyectoDAO proyectoDAO;
    private final ValidadorAccesoCurso validadorAccesoCurso;
    private AsignacionesService asignacionesService;

    public AuditoriaServiceImpl() {
        this(new ReglaDAOImpl(), new AuditoriaDAOImpl(), new GitHubClient(),
                new EvaluadorReglaFactory(), new ProyectoDAOImpl());
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory) {
        this(reglaDAO, auditoriaDAO, gitHubClient, evaluadorFactory, new ProyectoDAOImpl());
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory,
            ProyectoDAO proyectoDAO) {
        this.reglaDAO = reglaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.gitHubClient = gitHubClient;
        this.evaluadorFactory = evaluadorFactory;
        this.proyectoDAO = proyectoDAO;
        this.validadorAccesoCurso = new ValidadorAccesoCurso(null);
        this.asignacionesService = null;
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory,
            ProyectoDAO proyectoDAO, AsignacionesService asignacionesService) {
        this(reglaDAO, auditoriaDAO, gitHubClient, evaluadorFactory, proyectoDAO);
        this.asignacionesService = asignacionesService;
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory,
            ProyectoDAO proyectoDAO, AsignacionesService asignacionesService,
            ValidadorAccesoCurso validadorAccesoCurso) {
        this.reglaDAO = reglaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.gitHubClient = gitHubClient;
        this.evaluadorFactory = evaluadorFactory;
        this.proyectoDAO = proyectoDAO;
        this.asignacionesService = asignacionesService;
        this.validadorAccesoCurso = (validadorAccesoCurso != null) ? validadorAccesoCurso : new ValidadorAccesoCurso(null);
    }

    public void setAsignacionesService(AsignacionesService asignacionesService) {
        this.asignacionesService = asignacionesService;
    }

    @Override
    public ResumenDTO solicitarPreviaDeAuditoria(AuditoriaDTO dto) {
        List<Regla> reglas = obtenerReglasVigentes();
        int puntajeMaximo = reglas.stream()
                .mapToInt(Regla::getPonderacion)
                .sum();

        return new ResumenDTO(puntajeMaximo);
    }

    @Override
    public ResultadoDTO confirmarEjecucionDeAuditoria(AuditoriaDTO dto) {
        if (dto != null && dto.getIdAsignacion() != null) {
            return ejecutarAuditoriaDeAsignacion(dto);
        }

        if (dto != null && dto.getProyectoId() != null && proyectoDAO != null) {
            Proyecto proyecto = proyectoDAO.buscarPorId(dto.getProyectoId());
            if (proyecto != null && dto.getUsuarioId() != null && proyecto.getUsuario() != null) {
                if (!dto.getUsuarioId().equals(proyecto.getUsuario().getId())) {
                    throw new ExcepcionNegocio("No tiene permisos para auditar un proyecto que no le pertenece");
                }
            }
        }

        String urlRepositorio = dto == null ? null : dto.getUrl();
        List<String> rutas = gitHubClient.obtenerEstructuraDelRepositorio(urlRepositorio);
        List<Regla> reglas = obtenerReglasVigentes();

        Auditoria auditoria = crearAuditoria(dto);
        auditoria.setTipoAuditoria("GENERAL");
        if (dto != null && dto.getProyectoId() != null) {
            auditoria.setNumeroEnProyecto(auditoriaDAO.obtenerSiguienteNumeroGeneralEnProyecto(dto.getProyectoId()));
        }
        evaluarReglas(auditoria, reglas, rutas, urlRepositorio);

        calcularPuntajesDeLaAuditoria(auditoria);
        auditoria.setDictamen(auditoria.determinarDictamen());
        auditoriaDAO.registrarAuditoriaEnElHistorial(auditoria);

        ResultadoDTO resultadoDTO = crearResultadoDTO(auditoria);
        resultadoDTO.setUmbralAprobacion(Auditoria.UMBRAL_MINIMO_APROBACION);
        return resultadoDTO;
    }

    private List<Regla> obtenerReglasVigentes() {
        List<Regla> reglas = reglaDAO.consultarReglasDeEvaluacionVigentes();
        if (reglas.isEmpty()) {
            throw new ExcepcionNegocio(MENSAJE_SIN_REGLAS);
        }
        return reglas;
    }

    private Auditoria crearAuditoria(AuditoriaDTO dto) {
        Auditoria auditoria = new Auditoria();

        if (dto == null || dto.getProyectoId() == null || proyectoDAO == null) {
            return auditoria;
        }

        Proyecto proyecto = proyectoDAO.buscarPorId(dto.getProyectoId());
        if (proyecto == null) {
            proyecto = new Proyecto();
            proyecto.setId(dto.getProyectoId());
        }

        auditoria.setProyecto(proyecto);
        if (proyecto.getEstudiante() != null) {
            auditoria.setEstudiante(proyecto.getEstudiante());
        } else if (dto.getUsuarioId() != null) {
            com.gr4.owlaudit.model.Estudiante e = new com.gr4.owlaudit.model.Estudiante();
            e.setId(dto.getUsuarioId());
            auditoria.setEstudiante(e);
        }

        return auditoria;
    }

    private void evaluarReglas(Auditoria auditoria, List<Regla> reglas,
            List<String> rutas, String urlRepositorio) {
        List<String> mensajesCommits = null;

        for (Regla regla : reglas) {
            List<String> elementosAEvaluar = rutas;

            if (regla.getTipoMotor() == TipoMotorEnum.CONVENCION_COMMITS) {
                if (mensajesCommits == null) {
                    mensajesCommits = gitHubClient.obtenerMensajesDeCommits(
                            urlRepositorio, LIMITE_COMMITS);
                }
                elementosAEvaluar = mensajesCommits;
            }

            registrarResultadoDeRegla(auditoria, regla, elementosAEvaluar);
        }
    }

    private void registrarResultadoDeRegla(Auditoria auditoria, Regla regla,
            List<String> elementosAEvaluar) {
        if (evaluarCumplimientoDeRegla(regla, elementosAEvaluar)) {
            auditoria.asignarPonderacionCompleta(regla);
        } else {
            auditoria.registrarHallazgoDeIncumplimiento(regla);
        }
    }

    private boolean evaluarCumplimientoDeRegla(Regla regla, List<String> elementosAEvaluar) {
        return evaluadorFactory.obtenerEvaluador(regla.getTipoMotor())
                .evaluar(regla.getParametroExacto(), elementosAEvaluar);
    }

    private ResultadoDTO crearResultadoDTO(Auditoria auditoria) {
        List<ResultadoDTO.DetalleResultadoDTO> detalles = crearDetallesResultado(auditoria);

        ResultadoDTO resultadoDTO = new ResultadoDTO(
                auditoria.getPuntajeObtenido(),
                auditoria.getPuntajeMaximo(),
                auditoria.getPorcentaje(),
                detalles);
        resultadoDTO.setIdAuditoria(auditoria.getId());
        resultadoDTO.setNumeroEnProyecto(auditoria.getNumeroEnProyecto());
        resultadoDTO.setNumeroEnAsignacion(auditoria.getNumeroEnAsignacion());
        resultadoDTO.setTipoAuditoria(auditoria.getTipoAuditoria());
        resultadoDTO.setCodigoVisible(auditoria.getCodigoVisible());
        resultadoDTO.setEtiquetaVisible(auditoria.getEtiquetaVisible());
        resultadoDTO.setDictamen(auditoria.getDictamen() != null ? auditoria.getDictamen() : auditoria.determinarDictamen());

        return resultadoDTO;
    }

    private List<ResultadoDTO.DetalleResultadoDTO> crearDetallesResultado(Auditoria auditoria) {
        List<ResultadoDTO.DetalleResultadoDTO> detalles = new ArrayList<>();

        for (ResultadoRegla resultado : auditoria.getResultados()) {
            detalles.add(new ResultadoDTO.DetalleResultadoDTO(
                    resultado.getRegla().getNombreRepresentativo(),
                    resultado.isCumple(),
                    resultado.getPuntosObtenidos(),
                    resultado.getRegla().getPonderacion(),
                    resultado.getRegla().getNivelSeveridad(),
                    resultado.getHallazgo() != null ? resultado.getHallazgo().getId() : null,
                    resultado.getHallazgo() != null ? resultado.getHallazgo().getEvidencia() : null,
                    resultado.getHallazgo() != null ? resultado.getHallazgo().getRecomendacion() : null));
        }

        return detalles;
    }

    private void calcularPuntajesDeLaAuditoria(Auditoria auditoria) {
        int puntajeObtenido = 0;
        int puntajeMaximo = 0;

        for (ResultadoRegla resultado : auditoria.getResultados()) {
            puntajeObtenido += resultado.getPuntosObtenidos();
            puntajeMaximo += resultado.getRegla().getPonderacion();
        }

        double porcentaje = puntajeMaximo == 0
                ? 0.0
                : Math.round(puntajeObtenido * 1000.0 / puntajeMaximo) / 10.0;

        auditoria.setPuntajeObtenido(puntajeObtenido);
        auditoria.setPuntajeMaximo(puntajeMaximo);
        auditoria.setPorcentaje(porcentaje);
    }

    @Override
    public ResultadoDTO ejecutarAuditoriaDeAsignacion(AuditoriaDTO auditoriaDTO) {
        if (auditoriaDTO == null || auditoriaDTO.getIdAsignacion() == null) {
            throw new ExcepcionNegocio("No se permiten auditorías libres. Debe seleccionar una asignación válida dentro de sus cursos matriculados.");
        }

        final boolean esProyectoNoEspecificado = auditoriaDTO.getProyectoId() == null;
        if (esProyectoNoEspecificado) {
            throw new ExcepcionNegocio("Debe seleccionar un proyecto válido");
        }

        Proyecto proyecto = proyectoDAO.buscarPorId(auditoriaDTO.getProyectoId());
        if (proyecto == null) {
            throw new ExcepcionNegocio("Debe seleccionar un proyecto válido");
        }

        verificarPertenenciaProyecto(auditoriaDTO, proyecto);
        validarAccesoEstudianteACursoDeAsignacion(auditoriaDTO);

        Asignacion asignacion = (asignacionesService != null)
                ? asignacionesService.buscarPorId(auditoriaDTO.getIdAsignacion())
                : null;

        if (asignacion != null) {
            // CA-2: Rechazo de entregas con fecha límite vencida (Move Method en Asignacion)
            if (asignacion.estaVencida()) {
                throw new ExcepcionNegocio("La fecha límite para entregar esta asignación ha expirado");
            }

            // CA-1.2: Bloqueo por agotar intentos permitidos (Move Method en Asignacion y Replace Temp with Query)
            Long usuarioId = resolverUsuarioIdDeEntrega(auditoriaDTO, proyecto);
            if (usuarioId != null && auditoriaDAO != null) {
                List<Auditoria> previas = auditoriaDAO.consultarAuditoriasDeAsignacionPorUsuario(
                        auditoriaDTO.getIdAsignacion(), usuarioId);
                int intentosConsumidos = (previas != null) ? previas.size() : 0;
                if (asignacion.haAlcanzadoLimiteIntentos(intentosConsumidos)) {
                    throw new ExcepcionNegocio("Ha alcanzado el límite máximo de intentos permitidos para esta asignación");
                }
            }
        }

        String urlRepositorio = (proyecto.getUrlGithub() != null && !proyecto.getUrlGithub().isBlank())
                ? proyecto.getUrlGithub()
                : auditoriaDTO.getUrl();

        List<String> rutas = gitHubClient.obtenerEstructuraDelRepositorio(urlRepositorio);

        List<Regla> reglasAsignacion = asignacionesService.consultarReglasDeAsignacion(auditoriaDTO.getIdAsignacion());
        double umbral = asignacionesService.consultarUmbralAprobacion(auditoriaDTO.getIdAsignacion());

        Auditoria auditoria = crearAuditoria(auditoriaDTO);
        auditoria.setTipoAuditoria("ASIGNACION");
        if (auditoriaDTO.getIdAsignacion() != null) {
            Asignacion asigRef = new Asignacion();
            asigRef.setId(auditoriaDTO.getIdAsignacion());
            auditoria.setAsignacion(asigRef);
            auditoria.setNumeroEnAsignacion(auditoriaDAO.obtenerSiguienteNumeroEnAsignacion(auditoriaDTO.getProyectoId(), auditoriaDTO.getIdAsignacion()));
        }

        evaluarReglas(auditoria, reglasAsignacion, rutas, urlRepositorio);
        calcularPuntajesDeLaAuditoria(auditoria);

        String dictamen = auditoria.determinarDictamenPorUmbral(umbral);
        auditoria.setDictamen(dictamen);

        auditoriaDAO.registrarAuditoriaEnElHistorial(auditoria);

        ResultadoDTO resultadoDTO = crearResultadoDTO(auditoria);
        resultadoDTO.setDictamen(dictamen);
        resultadoDTO.setUmbralAprobacion(umbral);

        return resultadoDTO;
    }

    /**
     * Refactoring: Extract Method (Martin Fowler)
     * Verifica que el proyecto a auditar pertenezca al usuario en sesión.
     */
    private void verificarPertenenciaProyecto(AuditoriaDTO auditoriaDTO, Proyecto proyecto) {
        if (auditoriaDTO.getUsuarioId() != null && proyecto.getUsuario() != null) {
            if (!auditoriaDTO.getUsuarioId().equals(proyecto.getUsuario().getId())) {
                throw new ExcepcionNegocio("No tiene permisos para auditar un proyecto que no le pertenece");
            }
        }
    }

    /**
     * Refactorización Martin Fowler: Replace Temp with Query (Composing Methods).
     * Resuelve el identificador del usuario que realiza la entrega a partir del DTO o del proyecto asociado.
     */
    private Long resolverUsuarioIdDeEntrega(AuditoriaDTO dto, Proyecto proyecto) {
        if (dto != null && dto.getUsuarioId() != null) {
            return dto.getUsuarioId();
        }
        return (proyecto != null && proyecto.getUsuario() != null) ? proyecto.getUsuario().getId() : null;
    }

    /**
     * Refactoring: Extract Method (Martin Fowler)
     * Valida mediante el ValidadorAccesoCurso que el estudiante esté matriculado en el curso de la asignación.
     */
    private void validarAccesoEstudianteACursoDeAsignacion(AuditoriaDTO auditoriaDTO) {
        Asignacion asignacion = (asignacionesService != null)
                ? asignacionesService.buscarPorId(auditoriaDTO.getIdAsignacion())
                : null;
        if (asignacion != null && asignacion.getCurso() != null && auditoriaDTO.getUsuarioId() != null) {
            validadorAccesoCurso.validarAccesoEstudianteACurso(asignacion.getCurso(), auditoriaDTO.getUsuarioId());
        }
    }

    @Override
    public ResultadoDTO obtenerDetalleAuditoria(Long auditoriaId) {
        if (auditoriaId == null) return null;
        Auditoria aud = auditoriaDAO.buscarPorId(auditoriaId);
        if (aud == null) return null;
        return convertirAuditoriaAResultadoDTO(aud);
    }

    @Override
    public ResultadoDTO convertirAuditoriaAResultadoDTO(Auditoria auditoria) {
        if (auditoria == null) return null;
        ResultadoDTO dto = crearResultadoDTO(auditoria);
        if (auditoria.getAsignacion() != null && auditoria.getAsignacion().getUmbralAprobacion() != null) {
            dto.setUmbralAprobacion(auditoria.getAsignacion().getUmbralAprobacion());
        }
        return dto;
    }

    @Override
    public void revalorizarAuditoriasDeAsignacion(Long asignacionId) {
        if (asignacionId == null) return;
        List<Auditoria> auditorias = auditoriaDAO.buscarPorAsignacionId(asignacionId);
        if (auditorias == null || auditorias.isEmpty()) return;

        List<Regla> nuevasReglas = (asignacionesService != null)
                ? asignacionesService.consultarReglasDeAsignacion(asignacionId)
                : java.util.Collections.emptyList();
        if ((nuevasReglas == null || nuevasReglas.isEmpty()) && auditorias.get(0).getAsignacion() != null) {
            nuevasReglas = auditorias.get(0).getAsignacion().getReglas();
        }

        double nuevoUmbral = (asignacionesService != null)
                ? asignacionesService.consultarUmbralAprobacion(asignacionId)
                : 70.0;
        if (nuevoUmbral <= 0.0 && auditorias.get(0).getAsignacion() != null && auditorias.get(0).getAsignacion().getUmbralAprobacion() != null) {
            nuevoUmbral = auditorias.get(0).getAsignacion().getUmbralAprobacion();
        }

        for (Auditoria aud : auditorias) {
            Proyecto p = aud.getProyecto();
            String urlRepo = (p != null && p.getUrlGithub() != null && !p.getUrlGithub().isBlank())
                    ? p.getUrlGithub()
                    : "";

            List<String> rutas = java.util.Collections.emptyList();
            try {
                if (urlRepo != null && !urlRepo.isBlank()) {
                    rutas = gitHubClient.obtenerEstructuraDelRepositorio(urlRepo);
                }
            } catch (Exception ignored) {}

            if (aud.getResultados() != null) {
                aud.getResultados().clear();
            } else {
                aud.setResultados(new ArrayList<>());
            }

            evaluarReglas(aud, nuevasReglas, rutas, urlRepo);
            calcularPuntajesDeLaAuditoria(aud);
            String nuevoDictamen = aud.determinarDictamenPorUmbral(nuevoUmbral);
            aud.setDictamen(nuevoDictamen);

            auditoriaDAO.actualizarAuditoria(aud);
        }
    }

    @Override
    public List<AuditoriaResumenDTO> consultarEntregasDeAsignacionParaDocente(Long idAsignacion, Long docenteId) {
        return java.util.Collections.emptyList();
    }
}
