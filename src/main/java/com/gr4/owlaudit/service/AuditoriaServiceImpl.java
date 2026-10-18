package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.ProyectoDAO;
import com.gr4.owlaudit.dao.ProyectoDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;

import java.util.ArrayList;
import java.util.List;

public class AuditoriaServiceImpl implements AuditoriaService {

    private static final String MENSAJE_SIN_REGLAS = "No existen reglas de evaluación activas para auditar el proyecto";
    private static final int LIMITE_COMMITS = 10;

    private final ReglaDAO reglaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final GitHubClient gitHubClient;
    private final EvaluadorReglaFactory evaluadorFactory;
    private final ProyectoDAO proyectoDAO;

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
        String urlRepositorio = dto == null ? null : dto.getUrl();
        List<String> rutas = gitHubClient.obtenerEstructuraDelRepositorio(urlRepositorio);
        List<Regla> reglas = obtenerReglasVigentes();

        Auditoria auditoria = crearAuditoria(dto);
        evaluarReglas(auditoria, reglas, rutas, urlRepositorio);

        calcularPuntajesDeLaAuditoria(auditoria);
        auditoriaDAO.registrarAuditoriaEnElHistorial(auditoria);

        return crearResultadoDTO(auditoria);
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
        resultadoDTO.setDictamen(auditoria.determinarDictamen());

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
}
