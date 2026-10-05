package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.AuditoriaDAO;
import com.gr4.owlaudit.dto.EvolucionDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO;
import com.gr4.owlaudit.dto.EvolucionFinalDTO.ComparacionRegla;
import com.gr4.owlaudit.dto.ResumenHistorialDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para CU04: Consultar Evolución de Calidad.
 * Basado en secuencia4.puml, cu04-consultarEvolucionCalidad.md y el caso de prueba CP-CU04-01.
 */
class EvolucionServiceImplTest {

    static class MockAuditoriaDAO implements AuditoriaDAO {
        private final Map<Long, List<Auditoria>> historialPorProyecto = new HashMap<>();
        private final List<ResultadoRegla> todosLosResultados = new ArrayList<>();
        private boolean registrarLlamado = false;

        void agregarAuditoria(Long proyectoId, Auditoria auditoria) {
            historialPorProyecto.computeIfAbsent(proyectoId, k -> new ArrayList<>()).add(auditoria);
        }

        void agregarResultadoRegla(ResultadoRegla resultado) {
            todosLosResultados.add(resultado);
        }

        @Override
        public void registrarAuditoriaEnElHistorial(Auditoria auditoria) {
            this.registrarLlamado = true;
        }

        @Override
        public List<Auditoria> consultarHistorialDeAuditorias(Long proyectoId) {
            return new ArrayList<>(historialPorProyecto.getOrDefault(proyectoId, List.of()));
        }

        @Override
        public List<ResultadoRegla> consultarResultadosDeReglas(Long baseId, Long comparadaId) {
            List<ResultadoRegla> filtrados = new ArrayList<>();
            for (ResultadoRegla r : todosLosResultados) {
                if (r.getAuditoria() != null && 
                    (r.getAuditoria().getId().equals(baseId) || r.getAuditoria().getId().equals(comparadaId))) {
                    filtrados.add(r);
                }
            }
            return filtrados;
        }

        public boolean fueLlamadoRegistrar() {
            return registrarLlamado;
        }
    }

    private MockAuditoriaDAO mockDao;
    private EvolucionService evolucionService;

    private Regla reglaGitignore;
    private Regla reglaReadme;
    private Regla reglaSrc;
    private Regla reglaEnv;

    private Auditoria auditoria1;
    private Auditoria auditoria2;

    @BeforeEach
    void setUp() {
        mockDao = new MockAuditoriaDAO();
        evolucionService = new EvolucionServiceImpl(mockDao);

        // Reglas de CP-CU04-01
        reglaGitignore = new Regla();
        reglaGitignore.setId(1L);
        reglaGitignore.setNombreRepresentativo("Archivo gitignore");
        reglaGitignore.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaGitignore.setParametroExacto(".gitignore");
        reglaGitignore.setPonderacion(20);
        reglaGitignore.setNivelSeveridad(NivelSeveridadEnum.ALTA);

        reglaReadme = new Regla();
        reglaReadme.setId(2L);
        reglaReadme.setNombreRepresentativo("Archivo README");
        reglaReadme.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        reglaReadme.setParametroExacto("README.md");
        reglaReadme.setPonderacion(5);
        reglaReadme.setNivelSeveridad(NivelSeveridadEnum.BAJA);

        reglaSrc = new Regla();
        reglaSrc.setId(3L);
        reglaSrc.setNombreRepresentativo("Carpeta src");
        reglaSrc.setTipoMotor(TipoMotorEnum.ESTRUCTURA_CARPETAS);
        reglaSrc.setParametroExacto("src");
        reglaSrc.setPonderacion(5);
        reglaSrc.setNivelSeveridad(NivelSeveridadEnum.MEDIA);

        reglaEnv = new Regla();
        reglaEnv.setId(4L);
        reglaEnv.setNombreRepresentativo("Sin archivos env");
        reglaEnv.setTipoMotor(TipoMotorEnum.RESTRICCION_ARCHIVOS);
        reglaEnv.setParametroExacto(".env");
        reglaEnv.setPonderacion(5);
        reglaEnv.setNivelSeveridad(NivelSeveridadEnum.ALTA);

        Proyecto proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setNombre("Proyecto Demo");

        // A1: 05/10/2026 - Puntaje: 25 de 35, 71.4%
        // Cumplen: Archivo gitignore (20) y Archivo README (5)
        // No cumplen: Carpeta src y Sin archivos env
        auditoria1 = new Auditoria();
        auditoria1.setId(101L);
        auditoria1.setProyecto(proyecto);
        auditoria1.setFechaHora(LocalDateTime.of(2026, 10, 5, 10, 0));
        auditoria1.setPuntajeObtenido(25);
        auditoria1.setPuntajeMaximo(35);
        auditoria1.setPorcentaje(71.4);

        ResultadoRegla resA1Git = new ResultadoRegla(true, 20, reglaGitignore, auditoria1);
        ResultadoRegla resA1Readme = new ResultadoRegla(true, 5, reglaReadme, auditoria1);
        ResultadoRegla resA1Src = new ResultadoRegla(false, 0, reglaSrc, auditoria1);
        resA1Src.setHallazgo(new Hallazgo(NivelSeveridadEnum.MEDIA, "No se encontró la carpeta 'src'", "Cree la carpeta 'src'", resA1Src));
        ResultadoRegla resA1Env = new ResultadoRegla(false, 0, reglaEnv, auditoria1);
        resA1Env.setHallazgo(new Hallazgo(NivelSeveridadEnum.ALTA, "Contiene archivos prohibidos: .env", "Elimine los archivos .env", resA1Env));

        mockDao.agregarResultadoRegla(resA1Git);
        mockDao.agregarResultadoRegla(resA1Readme);
        mockDao.agregarResultadoRegla(resA1Src);
        mockDao.agregarResultadoRegla(resA1Env);

        // A2: 12/10/2026 - Puntaje: 25 de 35, 71.4%
        // Cumplen: Archivo gitignore (20) y Carpeta src (5)
        // No cumplen: Archivo README y Sin archivos env
        auditoria2 = new Auditoria();
        auditoria2.setId(102L);
        auditoria2.setProyecto(proyecto);
        auditoria2.setFechaHora(LocalDateTime.of(2026, 10, 12, 11, 0));
        auditoria2.setPuntajeObtenido(25);
        auditoria2.setPuntajeMaximo(35);
        auditoria2.setPorcentaje(71.4);

        ResultadoRegla resA2Git = new ResultadoRegla(true, 20, reglaGitignore, auditoria2);
        ResultadoRegla resA2Readme = new ResultadoRegla(false, 0, reglaReadme, auditoria2);
        resA2Readme.setHallazgo(new Hallazgo(NivelSeveridadEnum.BAJA, "No se encontró el archivo README.md", "Agregue el archivo README.md", resA2Readme));
        ResultadoRegla resA2Src = new ResultadoRegla(true, 5, reglaSrc, auditoria2);
        ResultadoRegla resA2Env = new ResultadoRegla(false, 0, reglaEnv, auditoria2);
        resA2Env.setHallazgo(new Hallazgo(NivelSeveridadEnum.ALTA, "Contiene archivos prohibidos: .env", "Elimine los archivos .env", resA2Env));

        mockDao.agregarResultadoRegla(resA2Git);
        mockDao.agregarResultadoRegla(resA2Readme);
        mockDao.agregarResultadoRegla(resA2Src);
        mockDao.agregarResultadoRegla(resA2Env);

        mockDao.agregarAuditoria(1L, auditoria1);
        mockDao.agregarAuditoria(1L, auditoria2);
    }

    @Test
    @DisplayName("CP-CU04-01: Comparación exitosa tomando la más antigua como base sin importar el orden")
    void testCasoDePruebaPrincipalCP_CU04_01() {
        // El estudiante indica las dos auditorías a comparar: primero A2 (102L) y luego A1 (101L)
        EvolucionDTO dto = new EvolucionDTO(1L, 102L, 101L);

        EvolucionFinalDTO evolucion = evolucionService.indicarDosAuditoriasAComparar(dto);

        assertNotNull(evolucion, "El DTO resultante no debe ser nulo");

        // El sistema toma A1 como base por ser más antigua y A2 como comparada
        assertEquals(101L, evolucion.getBaseId(), "La auditoría base debe ser A1 (101L)");
        assertEquals(102L, evolucion.getComparadaId(), "La auditoría comparada debe ser A2 (102L)");

        // Variación de puntaje y porcentaje
        assertEquals(0, evolucion.getVariacionPuntaje(), "La variación de puntaje debe ser 0");
        assertEquals(0.0, evolucion.getVariacionPorcentaje(), "La variación de porcentaje debe ser 0.0 %");

        // Clasificación de hallazgos
        // - Archivo README es Nuevo (BAJA)
        // - Carpeta src es Corregido (MEDIA)
        // - Sin archivos env es Persistente (ALTA)
        // - Archivo gitignore no tiene hallazgo
        List<ComparacionRegla> comparaciones = evolucion.getComparaciones();
        assertEquals(3, comparaciones.size(), "Deben existir 3 comparaciones de hallazgos clasificados");

        ComparacionRegla compReadme = comparaciones.stream()
                .filter(c -> "Archivo README".equals(c.getNombreRepresentativo())).findFirst().orElse(null);
        assertNotNull(compReadme);
        assertEquals("Nuevo", compReadme.getEstado());
        assertEquals(NivelSeveridadEnum.BAJA, compReadme.getNivelSeveridad());

        ComparacionRegla compSrc = comparaciones.stream()
                .filter(c -> "Carpeta src".equals(c.getNombreRepresentativo())).findFirst().orElse(null);
        assertNotNull(compSrc);
        assertEquals("Corregido", compSrc.getEstado());
        assertEquals(NivelSeveridadEnum.MEDIA, compSrc.getNivelSeveridad());

        ComparacionRegla compEnv = comparaciones.stream()
                .filter(c -> "Sin archivos env".equals(c.getNombreRepresentativo())).findFirst().orElse(null);
        assertNotNull(compEnv);
        assertEquals("Persistente", compEnv.getEstado());
        assertEquals(NivelSeveridadEnum.ALTA, compEnv.getNivelSeveridad());

        // Totales por estado
        assertEquals(1, evolucion.getTotalNuevos(), "Debe haber 1 hallazgo Nuevo");
        assertEquals(1, evolucion.getTotalPersistentes(), "Debe haber 1 hallazgo Persistente");
        assertEquals(1, evolucion.getTotalCorregidos(), "Debe haber 1 hallazgo Corregido");

        // Postcondición: No se modifica ni se registra ningún dato
        assertTrue(!mockDao.fueLlamadoRegistrar(), "No se debe registrar ninguna auditoría ni modificar la base de datos");
    }

    @Test
    @DisplayName("Fase 1: Consulta del historial de auditorías registradas")
    void testSolicitarEvolucionDeCalidadExitosa() {
        EvolucionDTO dto = new EvolucionDTO(1L);
        ResumenHistorialDTO resumen = evolucionService.solicitarEvolucionDeCalidad(dto);

        assertNotNull(resumen);
        assertEquals(1L, resumen.getProyectoId());
        assertEquals(2, resumen.getHistorialAuditorias().size());
        assertEquals(101L, resumen.getHistorialAuditorias().get(0).getAuditoriaId());
        assertEquals(102L, resumen.getHistorialAuditorias().get(1).getAuditoriaId());
    }

    @Test
    @DisplayName("Escenario Alternativo 1: Proyecto con menos de dos auditorías registradas")
    void testAlternativo1_MenosDeDosAuditorias() {
        MockAuditoriaDAO daoVacio = new MockAuditoriaDAO();
        EvolucionService servicioVacio = new EvolucionServiceImpl(daoVacio);

        // Sin auditorías
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                servicioVacio.solicitarEvolucionDeCalidad(new EvolucionDTO(99L)));
        assertEquals("El proyecto no tiene suficientes auditorías para comparar", ex1.getMessage());

        // Con solo una auditoría
        daoVacio.agregarAuditoria(99L, auditoria1);
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                servicioVacio.solicitarEvolucionDeCalidad(new EvolucionDTO(99L)));
        assertEquals("El proyecto no tiene suficientes auditorías para comparar", ex2.getMessage());
    }

    @Test
    @DisplayName("Escenario Alternativo 2: No se indican las dos auditorías a comparar")
    void testAlternativo2_NoIndicaDosAuditorias() {
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                evolucionService.indicarDosAuditoriasAComparar(new EvolucionDTO(1L, 101L, null)));
        assertEquals("Consulta cancelada", ex1.getMessage());

        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                evolucionService.indicarDosAuditoriasAComparar(new EvolucionDTO(1L, null, 102L)));
        assertEquals("Consulta cancelada", ex2.getMessage());
    }

    @Test
    @DisplayName("Escenario Alternativo 3: Se indican dos auditorías iguales")
    void testAlternativo3_AuditoriasIguales() {
        EvolucionDTO dto = new EvolucionDTO(1L, 101L, 101L);
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                evolucionService.indicarDosAuditoriasAComparar(dto));
        assertEquals("Debe indicar dos auditorías distintas", ex.getMessage());
    }

    @Test
    @DisplayName("Escenario Alternativo 4: Todas las reglas se cumplen en ambas auditorías")
    void testAlternativo4_TodasReglasCumplenSinHallazgos() {
        MockAuditoriaDAO daoPerfecto = new MockAuditoriaDAO();
        EvolucionService servicioPerfecto = new EvolucionServiceImpl(daoPerfecto);

        Auditoria aBase = new Auditoria();
        aBase.setId(201L);
        aBase.setFechaHora(LocalDateTime.of(2026, 9, 1, 10, 0));
        aBase.setPuntajeObtenido(35);
        aBase.setPuntajeMaximo(35);
        aBase.setPorcentaje(100.0);

        Auditoria aComp = new Auditoria();
        aComp.setId(202L);
        aComp.setFechaHora(LocalDateTime.of(2026, 9, 15, 10, 0));
        aComp.setPuntajeObtenido(35);
        aComp.setPuntajeMaximo(35);
        aComp.setPorcentaje(100.0);

        ResultadoRegla r1B = new ResultadoRegla(true, 20, reglaGitignore, aBase);
        ResultadoRegla r1C = new ResultadoRegla(true, 20, reglaGitignore, aComp);
        ResultadoRegla r2B = new ResultadoRegla(true, 5, reglaReadme, aBase);
        ResultadoRegla r2C = new ResultadoRegla(true, 5, reglaReadme, aComp);

        daoPerfecto.agregarResultadoRegla(r1B);
        daoPerfecto.agregarResultadoRegla(r1C);
        daoPerfecto.agregarResultadoRegla(r2B);
        daoPerfecto.agregarResultadoRegla(r2C);

        daoPerfecto.agregarAuditoria(2L, aBase);
        daoPerfecto.agregarAuditoria(2L, aComp);

        EvolucionDTO dto = new EvolucionDTO(2L, 201L, 202L);
        EvolucionFinalDTO evolucion = servicioPerfecto.indicarDosAuditoriasAComparar(dto);

        assertNotNull(evolucion);
        assertEquals(0, evolucion.getVariacionPuntaje());
        assertEquals(0.0, evolucion.getVariacionPorcentaje());
        assertTrue(evolucion.getComparaciones().isEmpty(), "No debe haber hallazgos");
        assertEquals(0, evolucion.getTotalNuevos());
        assertEquals(0, evolucion.getTotalPersistentes());
        assertEquals(0, evolucion.getTotalCorregidos());
        assertEquals("No existen hallazgos en las auditorías comparadas", evolucion.getMensaje());
    }
}
