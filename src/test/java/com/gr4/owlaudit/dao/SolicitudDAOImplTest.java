package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.EstadoRetroEnum;
import com.gr4.owlaudit.model.Hallazgo;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Proyecto;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.ResultadoRegla;
import com.gr4.owlaudit.model.SolicitudRetroalimentacion;
import com.gr4.owlaudit.model.TipoMotorEnum;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Prueba de integración para SolicitudDAOImpl con MariaDB.
 * Verifica estrictamente los 4 métodos del diagrama de clases:
 * - crearSolicitudDeRetroalimentacion
 * - buscarPorId
 * - buscarPorHallazgoId
 * - actualizarSolicitud
 */
@Tag("integracion")
class SolicitudDAOImplTest {

    @Test
    void testFlujoCompletoSolicitudDAO() {
        SolicitudDAO dao = new SolicitudDAOImpl();

        Proyecto proyecto = new Proyecto();
        proyecto.setNombre("Proyecto Prueba DAO CU06 " + System.nanoTime());
        proyecto.setUrlGithub("https://github.com/test/dao");
        proyecto.setFechaRegistro(LocalDateTime.now());
        new ProyectoDAOImpl().guardarProyectoAcademico(proyecto);

        Regla regla = new Regla();
        regla.setNombreRepresentativo("Regla prueba CU06 " + System.nanoTime());
        regla.setTipoMotor(TipoMotorEnum.PRESENCIA_ARCHIVO);
        regla.setParametroExacto(".gitignore");
        regla.setNivelSeveridad(NivelSeveridadEnum.ALTA);
        regla.setPonderacion(20);
        regla.setActiva(false);
        new ReglaDAOImpl().guardar(regla);

        Auditoria auditoria = new Auditoria();
        auditoria.setProyecto(proyecto);
        auditoria.setFechaHora(LocalDateTime.now());
        auditoria.setPuntajeObtenido(0);
        auditoria.setPuntajeMaximo(20);
        auditoria.setPorcentaje(0.0);
        auditoria.registrarHallazgoDeIncumplimiento(regla);

        new AuditoriaDAOImpl().registrarAuditoriaEnElHistorial(auditoria);

        Hallazgo hallazgo = auditoria.getResultados().get(0).getHallazgo();
        assertNotNull(hallazgo.getId());

        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion();
        solicitud.setHallazgo(hallazgo);
        solicitud.setJustificacion("Consulta de prueba para CU06");
        solicitud.setEstado(EstadoRetroEnum.PENDIENTE);

        try {
            // 1. crearSolicitudDeRetroalimentacion
            dao.crearSolicitudDeRetroalimentacion(solicitud);
            assertNotNull(solicitud.getId(), "La solicitud debe recibir un ID autogenerado");

            // 2. buscarPorId
            SolicitudRetroalimentacion encontrada = dao.buscarPorId(solicitud.getId());
            assertNotNull(encontrada);
            assertEquals("Consulta de prueba para CU06", encontrada.getJustificacion());
            assertEquals(EstadoRetroEnum.PENDIENTE, encontrada.getEstado());

            // 3. buscarPorHallazgoId
            SolicitudRetroalimentacion porHallazgo = dao.buscarPorHallazgoId(hallazgo.getId());
            assertNotNull(porHallazgo);
            assertEquals(solicitud.getId(), porHallazgo.getId());

            // 4. actualizarSolicitud
            encontrada.setOrientacionTecnica("Orientación técnica de respuesta válida con más de diez caracteres.");
            encontrada.marcarSolicitudComoAtendida();
            dao.actualizarSolicitud(encontrada);

            SolicitudRetroalimentacion atendida = dao.buscarPorId(solicitud.getId());
            assertEquals(EstadoRetroEnum.ATENDIDA, atendida.getEstado());
            assertEquals("Orientación técnica de respuesta válida con más de diez caracteres.", atendida.getOrientacionTecnica());

        } finally {
            limpiar(solicitud, auditoria, regla, proyecto);
        }
    }

    private void limpiar(SolicitudRetroalimentacion solicitud, Auditoria auditoria, Regla regla, Proyecto proyecto) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            if (solicitud.getId() != null) {
                SolicitudRetroalimentacion s = session.get(SolicitudRetroalimentacion.class, solicitud.getId());
                if (s != null) {
                    session.remove(s);
                }
            }
            if (auditoria.getId() != null) {
                Auditoria a = session.get(Auditoria.class, auditoria.getId());
                if (a != null) {
                    session.remove(a); // cascade borra resultados y hallazgo
                }
            }
            if (regla.getId() != null) {
                Regla r = session.get(Regla.class, regla.getId());
                if (r != null) {
                    session.remove(r);
                }
            }
            if (proyecto.getId() != null) {
                Proyecto p = session.get(Proyecto.class, proyecto.getId());
                if (p != null) {
                    session.remove(p);
                }
            }
            tx.commit();
        } catch (Exception e) {
            System.err.println("Error limpiando datos de prueba: " + e.getMessage());
        }
    }
}
