package com.gr4.owlaudit.service;

import com.gr4.owlaudit.client.GitHubClient;
import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.AuditoriaDTO;
import com.gr4.owlaudit.dto.ResultadoDTO;
import com.gr4.owlaudit.dto.ResumenDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import com.gr4.owlaudit.reglas.strategy.EvaluadorReglaFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de AuditoriaServiceImpl (secuencia3.puml) con dobles escritos a mano,
 * sin base de datos ni internet. Datos del caso de prueba CP-CU03-01.
 */
class AuditoriaServiceImplTest {

    private static final String URL_DEMO = "https://github.com/estudiante-demo/proyecto-demo";

    // ========================================================
    // Dobles de prueba
    // ========================================================

    static class ReglaDAOFalso implements ReglaDAO {
        private final List<Regla> reglas;

        ReglaDAOFalso(List<Regla> reglas) {
            this.reglas = reglas;
        }

        @Override
        public boolean existePorNombre(String nombre) {
            return false;
        }

        @Override
        public void guardar(Regla regla) {
        }

        @Override
        public List<Regla> consultarReglasDeEvaluacionVigentes() {
            return reglas;
        }
    }

    static class AuditoriaDAOFalso implements AuditoriaDAO {
        Auditoria auditoriaRegistrada;

        @Override
        public void registrarAuditoriaEnElHistorial(Auditoria auditoria) {
            this.auditoriaRegistrada = auditoria;
        }

        @Override
        public List<Auditoria> consultarHistorialDeAuditorias(Long proyectoId) {
            return auditoriaRegistrada != null ? List.of(auditoriaRegistrada) : List.of();
        }

        @Override
        public List<ResultadoRegla> consultarResultadosDeReglas(Long baseId, Long comparadaId) {
            return List.of();
        }
    }

    static class GitHubClientFalso extends GitHubClient {
        private final List<String> rutas;

        GitHubClientFalso(List<String> rutas) {
            this.rutas = rutas;
        }

        @Override
        public List<String> obtenerEstructuraDelRepositorio(String url) {
            return rutas;
        }
    }

    static class GitHubClientInaccesible extends GitHubClient {
        @Override
        public List<String> obtenerEstructuraDelRepositorio(String url) {
            throw new ExcepcionNegocio("No fue posible auditar el proyecto: el repositorio no existe o no es público");
        }
    }

    private static Regla regla(String nombre, TipoMotorEnum tipo, String parametro, int ponderacion,
                               NivelSeveridadEnum severidad) {
        Regla regla = new Regla();
        regla.setNombreRepresentativo(nombre);
        regla.setTipoMotor(tipo);
        regla.setParametroExacto(parametro);
        regla.setPonderacion(ponderacion);
        regla.setNivelSeveridad(severidad);
        regla.setActiva(true);
        return regla;
    }

    private static List<Regla> reglasCasoDePrueba() {
        List<Regla> reglas = new ArrayList<>();
        reglas.add(regla("Archivo gitignore", TipoMotorEnum.PRESENCIA_ARCHIVO, ".gitignore", 20, NivelSeveridadEnum.ALTA));
        reglas.add(regla("Archivo README", TipoMotorEnum.PRESENCIA_ARCHIVO, "README.md", 5, NivelSeveridadEnum.BAJA));
        reglas.add(regla("Carpeta src", TipoMotorEnum.ESTRUCTURA_CARPETAS, "src", 5, NivelSeveridadEnum.MEDIA));
        reglas.add(regla("Sin archivos env", TipoMotorEnum.RESTRICCION_ARCHIVOS, ".env", 5, NivelSeveridadEnum.ALTA));
        return reglas;
    }

    private static final List<String> ESTRUCTURA_CASO_DE_PRUEBA = List.of(".gitignore", "README.md", ".env", "docs/");

    // ========================================================
    // Pruebas
    // ========================================================

    @Test
    void previaInformaElPuntajeMaximo() {
        AuditoriaService servicio = new AuditoriaServiceImpl(new ReglaDAOFalso(reglasCasoDePrueba()),
            new AuditoriaDAOFalso(), new GitHubClientFalso(ESTRUCTURA_CASO_DE_PRUEBA), new EvaluadorReglaFactory());

        ResumenDTO resumen = servicio.solicitarPreviaDeAuditoria(new AuditoriaDTO(URL_DEMO));

        System.out.println("[Previa] puntaje máximo = " + resumen.getPuntajeMaximo());
        assertEquals(35, resumen.getPuntajeMaximo());
    }

    @Test
    void cpCu03_01_auditoriaConCumplimientoParcial() {
        AuditoriaDAOFalso auditoriaDAO = new AuditoriaDAOFalso();
        AuditoriaService servicio = new AuditoriaServiceImpl(new ReglaDAOFalso(reglasCasoDePrueba()),
            auditoriaDAO, new GitHubClientFalso(ESTRUCTURA_CASO_DE_PRUEBA), new EvaluadorReglaFactory());

        ResultadoDTO resultado = servicio.confirmarEjecucionDeAuditoria(new AuditoriaDTO(URL_DEMO));

        System.out.println("[CP-CU03-01] " + resultado.getPuntajeObtenido() + " de " + resultado.getPuntajeMaximo()
            + " (" + resultado.getPorcentaje() + " %)");
        assertEquals(25, resultado.getPuntajeObtenido());
        assertEquals(35, resultado.getPuntajeMaximo());
        assertEquals(71.4, resultado.getPorcentaje());

        // La auditoría se registró con un resultado por regla y un hallazgo por cada incumplimiento
        Auditoria registrada = auditoriaDAO.auditoriaRegistrada;
        assertEquals(4, registrada.getResultados().size());
        List<Hallazgo> hallazgos = new ArrayList<>();
        for (ResultadoRegla r : registrada.getResultados()) {
            System.out.println("  " + r.getRegla().getNombreRepresentativo() + " -> cumple=" + r.isCumple()
                + ", puntos=" + r.getPuntosObtenidos());
            if (r.getHallazgo() != null) {
                hallazgos.add(r.getHallazgo());
                System.out.println("    Hallazgo " + r.getHallazgo().getNivelSeveridad() + ": "
                    + r.getHallazgo().getEvidencia() + " | " + r.getHallazgo().getRecomendacion());
            }
        }
        assertEquals(2, hallazgos.size());
        assertEquals(NivelSeveridadEnum.MEDIA, hallazgos.get(0).getNivelSeveridad()); // Carpeta src
        assertEquals(NivelSeveridadEnum.ALTA, hallazgos.get(1).getNivelSeveridad());  // Sin archivos env
        assertTrue(hallazgos.get(0).getEvidencia().contains("src"));
    }

    @Test
    void alterno1_sinReglasActivasNoSePuedeAuditar() {
        AuditoriaService servicio = new AuditoriaServiceImpl(new ReglaDAOFalso(new ArrayList<>()),
            new AuditoriaDAOFalso(), new GitHubClientFalso(ESTRUCTURA_CASO_DE_PRUEBA), new EvaluadorReglaFactory());

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class,
            () -> servicio.solicitarPreviaDeAuditoria(new AuditoriaDTO(URL_DEMO)));

        System.out.println("[Alterno 1] " + error.getMessage());
        assertEquals("No existen reglas de evaluación activas para auditar el proyecto", error.getMessage());
    }

    @Test
    void alterno3_repositorioNoAccesibleNoRegistraLaAuditoria() {
        AuditoriaDAOFalso auditoriaDAO = new AuditoriaDAOFalso();
        AuditoriaService servicio = new AuditoriaServiceImpl(new ReglaDAOFalso(reglasCasoDePrueba()),
            auditoriaDAO, new GitHubClientInaccesible(), new EvaluadorReglaFactory());

        ExcepcionNegocio error = assertThrows(ExcepcionNegocio.class,
            () -> servicio.confirmarEjecucionDeAuditoria(new AuditoriaDTO(URL_DEMO)));

        System.out.println("[Alterno 3] " + error.getMessage());
        assertEquals("No fue posible auditar el proyecto: el repositorio no existe o no es público", error.getMessage());
        assertNull(auditoriaDAO.auditoriaRegistrada);
    }

    @Test
    void alterno4_todasLasReglasSeCumplenSinHallazgos() {
        AuditoriaDAOFalso auditoriaDAO = new AuditoriaDAOFalso();
        List<String> estructuraCompleta = List.of(".gitignore", "README.md", "src/", "src/Main.java");
        AuditoriaService servicio = new AuditoriaServiceImpl(new ReglaDAOFalso(reglasCasoDePrueba()),
            auditoriaDAO, new GitHubClientFalso(estructuraCompleta), new EvaluadorReglaFactory());

        ResultadoDTO resultado = servicio.confirmarEjecucionDeAuditoria(new AuditoriaDTO(URL_DEMO));

        System.out.println("[Alterno 4] " + resultado.getPuntajeObtenido() + " de " + resultado.getPuntajeMaximo()
            + " (" + resultado.getPorcentaje() + " %)");
        assertEquals(35, resultado.getPuntajeObtenido());
        assertEquals(35, resultado.getPuntajeMaximo());
        assertEquals(100.0, resultado.getPorcentaje());
        for (ResultadoRegla r : auditoriaDAO.auditoriaRegistrada.getResultados()) {
            assertNull(r.getHallazgo());
        }
    }
}
