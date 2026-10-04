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

    private static final String MENSAJE_SIN_REGLAS =
        "No existen reglas de evaluación activas para auditar el proyecto";

    private final ReglaDAO reglaDAO;
    private final AuditoriaDAO auditoriaDAO;
    private final GitHubClient gitHubClient;
    private final EvaluadorReglaFactory evaluadorFactory;

    public AuditoriaServiceImpl() {
        this(new ReglaDAOImpl(), new AuditoriaDAOImpl(), new GitHubClient(), new EvaluadorReglaFactory());
    }

    public AuditoriaServiceImpl(ReglaDAO reglaDAO, AuditoriaDAO auditoriaDAO,
                                GitHubClient gitHubClient, EvaluadorReglaFactory evaluadorFactory) {
        this.reglaDAO = reglaDAO;
        this.auditoriaDAO = auditoriaDAO;
        this.gitHubClient = gitHubClient;
        this.evaluadorFactory = evaluadorFactory;
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
        // 1. Obtener la estructura del repositorio (escenario alternativo 3 si no es accesible)
        List<String> rutas = gitHubClient.obtenerEstructuraDelRepositorio(dto == null ? null : dto.getUrl());

        // 2. Volver a consultar las reglas vigentes al momento de ejecutar
        // TODO DECISION D6: falta este mensaje en la fase 2 de secuencia3.puml
        List<Regla> reglas = reglaDAO.consultarReglasDeEvaluacionVigentes();
        if (reglas.isEmpty()) {
            throw new ExcepcionNegocio(MENSAJE_SIN_REGLAS);
        }

        // 3. Evaluar cada regla y registrar su resultado (y hallazgo si no cumple)
        Auditoria auditoria = new Auditoria();
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

        return new ResultadoDTO(auditoria.getPuntajeObtenido(), auditoria.getPuntajeMaximo(), auditoria.getPorcentaje());
    }

    // TODO DECISION D7: en clases.puml retorna void, pero el alt de secuencia3 necesita el boolean
    private boolean evaluarCumplimientoDeRegla(Regla regla, List<String> rutas) {
        return evaluadorFactory.obtenerEvaluador(regla.getTipoMotor())
            .evaluar(regla.getParametroExacto(), rutas);
    }

    // TODO DECISION D8: recibe la Auditoria por parámetro; como atributo se pisarían
    // dos auditorías simultáneas (Tomcat comparte una sola instancia del Servlet y su Service)
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
