package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.AuditoriaDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;

import java.util.List;

public class AuditoriaServiceImpl implements AuditoriaService {

    private static final String MENSAJE_SIN_REGLAS = "No existen reglas de evaluación activas para auditar el proyecto";

    private final ReglaDAO reglaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final GitHubClient gitHubClient;
    private final EvaluadorReglaFactory evaluadorFactory;
    private final com.gr4.owlaudit.dao.ProyectoDAO proyectoDAO;

    public AuditoriaServiceImpl() {
        this(new ReglaDAOImpl(), new AuditoriaDAOImpl(), new GitHubClient(), new EvaluadorReglaFactory(), new com.gr4.owlaudit.dao.ProyectoDAOImpl());
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory) {
        this(reglaDAO, auditoriaDAO, gitHubClient, evaluadorFactory, new com.gr4.owlaudit.dao.ProyectoDAOImpl());
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
            GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory,
            com.gr4.owlaudit.dao.ProyectoDAO proyectoDAO) {
        this.reglaDAO = reglaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.gitHubClient = gitHubClient;
        this.evaluadorFactory = evaluadorFactory;
        this.proyectoDAO = proyectoDAO;
    }

    @Override
    public ResumenDTO solicitarPreviaDeAuditoria(AuditoriaDTO dto) {
        // 1. Verificar que existan reglas activas (escenario alternativo 1)
        List<Regla> reglas = reglaDAO.consultarReglasDeEvaluacionVigentes();
        if (reglas.isEmpty()) {
            throw new ExcepcionNegocio(MENSAJE_SIN_REGLAS);
        }

        // 2. Calcular el puntaje máximo como la suma de las ponderaciones
        int puntajeMaximo = 0;
        for (Regla regla : reglas) {
            puntajeMaximo += regla.getPonderacion();
        }
        return new ResumenDTO(puntajeMaximo);
    }

    @Override
    public ResultadoDTO confirmarEjecucionDeAuditoria(AuditoriaDTO dto) {
        // 1. Obtener la estructura del repositorio (escenario alternativo 3 si no es
        // accesible)
        List<String> rutas = gitHubClient.obtenerEstructuraDelRepositorio(dto == null ? null : dto.getUrl());

        // 2. Volver a consultar las reglas vigentes al momento de ejecutar
        // TODO DECISION D6: falta este mensaje en la fase 2 de secuencia3.puml
        List<Regla> reglas = reglaDAO.consultarReglasDeEvaluacionVigentes();
        if (reglas.isEmpty()) {
            throw new ExcepcionNegocio(MENSAJE_SIN_REGLAS);
        }

        // 3. Evaluar cada regla y registrar su resultado (y hallazgo si no cumple)
        Auditoria auditoria = new Auditoria();
        if (dto != null && dto.getProyectoId() != null && proyectoDAO != null) {
            com.gr4.owlaudit.model.Proyecto p = proyectoDAO.buscarPorId(dto.getProyectoId());
            if (p == null) {
                p = new com.gr4.owlaudit.model.Proyecto();
                p.setId(dto.getProyectoId());
            }
            auditoria.setProyecto(p);
        }

        for (Regla regla : reglas) {
            if (evaluarCumplimientoDeRegla(regla, rutas)) {
                auditoria.asignarPonderacionCompleta(regla);
            } else {
                auditoria.registrarHallazgoDeIncumplimiento(regla);
            }
        }

        // 4. Calcular puntajes y registrar la auditoría en el historial
        calcularPuntajesDeLaAuditoria(auditoria);
        auditoriaDAO.registrarAuditoriaEnElHistorial(auditoria);

        List<ResultadoDTO.DetalleResultadoDTO> detalles = new java.util.ArrayList<>();
        for (ResultadoRegla r : auditoria.getResultados()) {
            detalles.add(new ResultadoDTO.DetalleResultadoDTO(
                r.getRegla().getNombreRepresentativo(),
                r.isCumple(),
                r.getPuntosObtenidos(),
                r.getRegla().getPonderacion(),
                r.getRegla().getNivelSeveridad(),
                r.getHallazgo() != null ? r.getHallazgo().getId() : null,
                r.getHallazgo() != null ? r.getHallazgo().getEvidencia() : null,
                r.getHallazgo() != null ? r.getHallazgo().getRecomendacion() : null
            ));
        }

        return new ResultadoDTO(auditoria.getPuntajeObtenido(), auditoria.getPuntajeMaximo(),
                auditoria.getPorcentaje(), detalles);
    }

    private boolean evaluarCumplimientoDeRegla(Regla regla, List<String> rutas) {
        return evaluadorFactory.obtenerEvaluador(regla.getTipoMotor())
                .evaluar(regla.getParametroExacto(), rutas);
    }

    private void calcularPuntajesDeLaAuditoria(Auditoria auditoria) {
        int puntajeObtenido = 0;
        int puntajeMaximo = 0;
        for (ResultadoRegla resultado : auditoria.getResultados()) {
            puntajeObtenido += resultado.getPuntosObtenidos();
            puntajeMaximo += resultado.getRegla().getPonderacion();
        }
        double porcentaje = puntajeMaximo == 0 ? 0.0 : Math.round(puntajeObtenido * 1000.0 / puntajeMaximo) / 10.0;

        auditoria.setPuntajeObtenido(puntajeObtenido);
        auditoria.setPuntajeMaximo(puntajeMaximo);
        auditoria.setPorcentaje(porcentaje);
    }
}
