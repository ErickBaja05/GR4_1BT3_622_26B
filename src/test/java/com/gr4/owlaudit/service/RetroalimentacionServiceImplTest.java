package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.SolicitudDAO;
import com.gr4.owlaudit.dto.SolicitudDTO;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.EstadoRetroEnum;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias para CU06: Atender retroalimentación de hallazgo.
 * Basado en secuencia6.puml y el caso de prueba CP-CU06-01.
 */
class RetroalimentacionServiceImplTest {

    static class MockSolicitudDAO implements SolicitudDAO {
        private final Map<Long, SolicitudRetroalimentacion> solicitudesPorId = new HashMap<>();
        private final Map<Long, SolicitudRetroalimentacion> solicitudesPorHallazgoId = new HashMap<>();
        private boolean crearLlamado = false;
        private boolean actualizarLlamado = false;

        void agregarSolicitud(SolicitudRetroalimentacion solicitud) {
            solicitudesPorId.put(solicitud.getId(), solicitud);
            if (solicitud.getHallazgo() != null && solicitud.getHallazgo().getId() != null) {
                solicitudesPorHallazgoId.put(solicitud.getHallazgo().getId(), solicitud);
            }
        }

        @Override
        public SolicitudRetroalimentacion buscarPorHallazgoId(Long hallazgoId) {
            return solicitudesPorHallazgoId.get(hallazgoId);
        }

        @Override
        public void crearSolicitudDeRetroalimentacion(SolicitudRetroalimentacion solicitud) {
            this.crearLlamado = true;
            solicitud.setId((long) (solicitudesPorId.size() + 1));
            agregarSolicitud(solicitud);
        }

        @Override
        public SolicitudRetroalimentacion buscarPorId(Long solicitudId) {
            return solicitudesPorId.get(solicitudId);
        }

        @Override
        public void actualizarSolicitud(SolicitudRetroalimentacion solicitud) {
            this.actualizarLlamado = true;
            agregarSolicitud(solicitud);
        }

        @Override
        public java.util.List<SolicitudRetroalimentacion> consultarSolicitudesPendientes() {
            return solicitudesPorId.values().stream()
                    .filter(s -> s.getEstado() == EstadoRetroEnum.PENDIENTE)
                    .toList();
        }

        @Override
        public java.util.List<SolicitudRetroalimentacion> buscarPendientes() {
            return consultarSolicitudesPendientes();
        }

        public boolean fueLlamadoCrear() {
            return crearLlamado;
        }

        public boolean fueLlamadoActualizar() {
            return actualizarLlamado;
        }
    }

    private MockSolicitudDAO mockDao;
    private RetroalimentacionService retroService;

    private SolicitudRetroalimentacion solicitudR4;
    private Hallazgo hallazgoR4;

    @BeforeEach
    void setUp() {
        mockDao = new MockSolicitudDAO();
        retroService = new RetroalimentacionServiceImpl(mockDao);

        // Configuración de entidades para CP-CU06-01
        Proyecto proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setNombre("Proyecto Demo");

        Auditoria auditoria = new Auditoria();
        auditoria.setId(102L);
        auditoria.setProyecto(proyecto);
        auditoria.setFechaHora(LocalDateTime.of(2026, 10, 12, 11, 0));

        Regla reglaEnv = new Regla();
        reglaEnv.setId(4L);
        reglaEnv.setNombreRepresentativo("Sin archivos env");
        reglaEnv.setTipoMotor(TipoMotorEnum.RESTRICCION_ARCHIVOS);
        reglaEnv.setParametroExacto(".env");
        reglaEnv.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        reglaEnv.setPonderacion(5);

        ResultadoRegla resultadoEnv = new ResultadoRegla(false, 0, reglaEnv, auditoria);

        hallazgoR4 = new Hallazgo(NivelSeveridadEnum.ALTA, "Contiene archivos prohibidos: .env", "Elimine los archivos .env", resultadoEnv);
        hallazgoR4.setId(401L);
        resultadoEnv.setHallazgo(hallazgoR4);

        solicitudR4 = new SolicitudRetroalimentacion();
        solicitudR4.setId(10L);
        solicitudR4.setHallazgo(hallazgoR4);
        solicitudR4.setJustificacion("¿Cómo podemos compartir las variables necesarias para producción sin subir el archivo .env?");
        solicitudR4.setEstado(EstadoRetroEnum.PENDIENTE);

        mockDao.agregarSolicitud(solicitudR4);
    }

    @Test
    @DisplayName("CP-CU06-01: Atención exitosa de una retroalimentación solicitada por un Estudiante")
    void testAtencionExitosaCP_CU06_01() {
        // Datos de entrada según CP-CU06-01
        String respuestaTecnica = "Se debe eliminar inmediatamente el archivo .env del repositorio remoto y agregarlo al .gitignore. " +
                "Para permitir que otros desarrolladores configuren su entorno, cree un archivo plantilla llamado .env.example en la raíz " +
                "con los nombres de las variables y valores vacíos, documentando su uso en el README.md.";

        SolicitudDTO dto = new SolicitudDTO(null, 10L, null, respuestaTecnica);

        // Ejecutar servicio
        retroService.registrarOrientacionTecnicaEnLaSolicitud(dto);

        // Verificaciones
        assertTrue(mockDao.fueLlamadoActualizar(), "Se debió persistir la actualización en el DAO");

        SolicitudRetroalimentacion actualizada = mockDao.buscarPorId(10L);
        assertNotNull(actualizada);
        assertEquals(EstadoRetroEnum.ATENDIDA, actualizada.getEstado(), "El estado debe cambiar a ATENDIDA");
        assertEquals(respuestaTecnica, actualizada.getOrientacionTecnica(), "La orientación técnica debe ser la ingresada por el Docente");
    }

    @Test
    @DisplayName("Escenario Alternativo 3: Orientación en blanco o menor a 10 caracteres")
    void testAlternativo3_OrientacionInvalidaCorta() {
        SolicitudDTO dtoVacio = new SolicitudDTO(null, 10L, null, "");
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoVacio));
        assertEquals("Orientación en blanco o inválida", ex1.getMessage());

        SolicitudDTO dtoEspacios = new SolicitudDTO(null, 10L, null, "   ");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoEspacios));
        assertEquals("Orientación en blanco o inválida", ex2.getMessage());

        SolicitudDTO dtoCorta = new SolicitudDTO(null, 10L, null, "Borre eso");
        ExcepcionNegocio ex3 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoCorta));
        assertEquals("Orientación en blanco o inválida", ex3.getMessage());

        SolicitudDTO dtoNull = new SolicitudDTO(null, 10L, null, null);
        ExcepcionNegocio ex4 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoNull));
        assertEquals("Orientación en blanco o inválida", ex4.getMessage());
    }

    @Test
    @DisplayName("Escenario Alternativo 3: Orientación mayor a 1000 caracteres")
    void testAlternativo3_OrientacionInvalidaLarga() {
        String textoLargo = "a".repeat(1001);
        SolicitudDTO dto = new SolicitudDTO(null, 10L, null, textoLargo);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dto));
        assertEquals("Orientación en blanco o inválida", ex.getMessage());
    }

    @Test
    @DisplayName("Validación: Solicitud inexistente o ya atendida")
    void testSolicitudInexistenteOAtendida() {
        SolicitudDTO dtoInexistente = new SolicitudDTO(null, 999L, null, "Texto de orientación técnica válido con suficiente longitud.");
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoInexistente));
        assertEquals("La solicitud de retroalimentación no existe.", ex1.getMessage());

        // Marcar la solicitud como ATENDIDA y reintentar
        solicitudR4.marcarSolicitudComoAtendida();
        SolicitudDTO dtoAtendida = new SolicitudDTO(null, 10L, null, "Texto de orientación técnica válido con suficiente longitud.");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.registrarOrientacionTecnicaEnLaSolicitud(dtoAtendida));
        assertEquals("La solicitud ya ha sido atendida.", ex2.getMessage());
    }

    @Test
    @DisplayName("CU05: Solicitar retroalimentación de hallazgo")
    void testSolicitarRetroalimentacionDeHallazgo() {
        // Hallazgo sin solicitud previa
        SolicitudDTO nuevoDto = new SolicitudDTO(501L, "Tengo dudas sobre cómo organizar las carpetas en src");
        retroService.solicitarRetroalimentacionDeHallazgo(nuevoDto);
        assertTrue(mockDao.fueLlamadoCrear(), "Se debió persistir la nueva solicitud");

        // Intentar solicitar nuevamente sobre el mismo hallazgo R4 que ya tiene solicitud
        SolicitudDTO dtoDuplicado = new SolicitudDTO(401L, "Otra consulta sobre el mismo hallazgo");
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoDuplicado));
        assertEquals("Hallazgo en revisión", ex.getMessage());
    }

    @Test
    @DisplayName("CU05 - CP-CU05-01: Solicitud exitosa para el hallazgo de una auditoría")
    void testSolicitudExitosaCP_CU05_01() {
        SolicitudDTO dto = new SolicitudDTO(801L, "La función requiere múltiples estructuras condicionales para el control de errores de entrada");
        retroService.solicitarRetroalimentacionDeHallazgo(dto);

        SolicitudRetroalimentacion creada = mockDao.buscarPorHallazgoId(801L);
        assertNotNull(creada, "La solicitud debe haber sido creada y persistida");
        assertEquals(EstadoRetroEnum.PENDIENTE, creada.getEstado(), "El estado inicial debe ser PENDIENTE");
        assertEquals(dto.getJustificacion(), creada.getJustificacion());
    }

    @Test
    @DisplayName("CU05 - Escenario Alternativo 1: Hallazgo ya en proceso de revisión")
    void testSolicitudHallazgoYaEnRevision() {
        SolicitudDTO dtoDuplicado = new SolicitudDTO(401L, "Otra consulta sobre el mismo hallazgo R4");
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoDuplicado));
        assertEquals("Hallazgo en revisión", ex.getMessage());
    }

    @Test
    @DisplayName("CU05: Validación de hallazgoId inválido")
    void testSolicitudHallazgoIdInvalido() {
        SolicitudDTO dtoNull = new SolicitudDTO(null, "Justificación con longitud válida");
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoNull));
        assertEquals("El identificador del hallazgo no es válido.", ex1.getMessage());

        SolicitudDTO dtoCero = new SolicitudDTO(0L, "Justificación con longitud válida");
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                retroService.solicitarRetroalimentacionDeHallazgo(dtoCero));
        assertEquals("El identificador del hallazgo no es válido.", ex2.getMessage());
    }
}
