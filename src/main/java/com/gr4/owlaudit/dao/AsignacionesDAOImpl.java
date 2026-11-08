package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.model.AsignacionRegla;
import com.gr4.owlaudit.model.Regla;

import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class AsignacionesDAOImpl implements AsignacionesDAO {

    @Override
    public Asignacion crearAsignacion(Asignacion asignacion) {
        boolean tieneDatosValidos = asignacion != null && asignacion.getNombre() != null
                && !asignacion.getNombre().trim().isEmpty();
        if (!tieneDatosValidos) {
            throw new IllegalArgumentException("La asignación debe contener un nombre válido para ser persistida.");
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (asignacion.getIdSecuencial() == null) {
                Long nextSeq = session.createQuery(
                    "SELECT COALESCE(MAX(a.idSecuencial), 0) + 1 FROM Asignacion a", Long.class)
                    .uniqueResult();
                asignacion.setIdSecuencial(nextSeq != null ? nextSeq : 1L);
            }
            session.persist(asignacion);
            tx.commit();
            return asignacion;
        } catch (Exception e) {
            revertirTransaccion(tx);
            throw new RuntimeException("Error al persistir la asignación: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Asignacion> listarAsignaciones() {
        return consultarAsignacionesDisponibles();
    }

    private void revertirTransaccion(Transaction tx) {
        if (tx != null && tx.isActive()) {
            try {
                tx.rollback();
            } catch (Exception ignored) {}
        }
    }

    @Override
    public List<Asignacion> consultarAsignacionesDisponibles() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Asignacion> lista = session.createQuery(
                "SELECT DISTINCT a FROM Asignacion a LEFT JOIN FETCH a.asignacionesReglas ar LEFT JOIN FETCH ar.regla ORDER BY a.id ASC", Asignacion.class
            ).list();

            long sec = 1L;
            boolean necesitaUpdate = false;
            for (Asignacion a : lista) {
                if (a.getIdSecuencial() == null) {
                    a.setIdSecuencial(sec);
                    session.merge(a);
                    necesitaUpdate = true;
                }
                sec++;
            }
            if (necesitaUpdate) {
                tx.commit();
            } else {
                tx.rollback();
            }
            return lista;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al consultar asignaciones disponibles: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Regla> consultarReglasDeAsignacion(Long idAsignacion) {
        if (idAsignacion == null) {
            return java.util.Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<AsignacionRegla> listaAR = session.createQuery(
                "SELECT ar FROM AsignacionRegla ar JOIN FETCH ar.regla WHERE ar.asignacionId = :idAsignacion", AsignacionRegla.class)
                .setParameter("idAsignacion", idAsignacion)
                .list();
            List<Regla> resultado = new ArrayList<>();
            for (AsignacionRegla ar : listaAR) {
                if (ar.getRegla() != null) {
                    Regla r = ar.getRegla();
                    r.setPonderacion(ar.getPonderacion());
                    r.setNivelSeveridad(ar.getNivelSeveridad());
                    resultado.add(r);
                }
            }
            return resultado;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar reglas de la asignación: " + e.getMessage(), e);
        }
    }

    @Override
    public double consultarUmbralAprobacion(Long idAsignacion) {
        if (idAsignacion == null) {
            return 0.0;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Double umbral = session.createQuery(
                "SELECT a.umbralAprobacion FROM Asignacion a WHERE a.id = :idAsignacion", Double.class)
                .setParameter("idAsignacion", idAsignacion)
                .uniqueResult();
            return umbral != null ? umbral : 0.0;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar umbral de la asignación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Asignacion> consultarAsignacionesDeCurso(Long idCurso) {
        if (idCurso == null) {
            return java.util.Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Asignacion a LEFT JOIN FETCH a.asignacionesReglas ar LEFT JOIN FETCH ar.regla LEFT JOIN FETCH a.curso WHERE a.curso.id = :idCurso ORDER BY a.id ASC", Asignacion.class)
                .setParameter("idCurso", idCurso)
                .list();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar asignaciones del curso: " + e.getMessage(), e);
        }
    }

    @Override
    public Asignacion buscarPorId(Long id) {
        if (id == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Asignacion a = session.createQuery(
                "SELECT DISTINCT a FROM Asignacion a LEFT JOIN FETCH a.asignacionesReglas ar LEFT JOIN FETCH ar.regla LEFT JOIN FETCH a.curso c WHERE a.id = :id", Asignacion.class)
                .setParameter("id", id)
                .uniqueResult();
            if (a != null && a.getCurso() != null) {
                org.hibernate.Hibernate.initialize(a.getCurso().getListadoEstudiantes());
                if (a.getCurso().getListadoEstudiantes() != null) {
                    for (var le : a.getCurso().getListadoEstudiantes()) {
                        if (le != null) {
                            org.hibernate.Hibernate.initialize(le.getEstudiante());
                        }
                    }
                }
            }
            return a;
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar asignación por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizar(Asignacion asignacion, List<Long> nuevasReglasIds) {
        if (asignacion == null || asignacion.getId() == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Asignacion entidad = session.get(Asignacion.class, asignacion.getId());
            if (entidad != null) {
                if (asignacion.getNombre() != null && !asignacion.getNombre().isBlank()) {
                    entidad.setNombre(asignacion.getNombre().trim());
                }
                if (asignacion.getUmbralAprobacion() != null) {
                    entidad.setUmbralAprobacion(asignacion.getUmbralAprobacion());
                }

                if (nuevasReglasIds != null) {
                    List<Regla> reglasNuevas = nuevasReglasIds.isEmpty()
                        ? new ArrayList<>()
                        : session.createQuery("SELECT r FROM Regla r WHERE r.id IN (:rids)", Regla.class)
                                 .setParameter("rids", nuevasReglasIds)
                                 .list();
                    entidad.setReglas(reglasNuevas);
                }
                session.merge(entidad);
            }
            tx.commit();
        } catch (Exception e) {
            revertirTransaccion(tx);
            throw new RuntimeException("Error al actualizar la asignación: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void eliminar(Long id) {
        if (id == null) {
            return;
        }
        Session session = null;
        Transaction tx = null;
        List<Long> proyectosAfectados = new ArrayList<>();
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            Asignacion asignacion = session.get(Asignacion.class, id);
            if (asignacion != null) {
                // Obtener proyectos afectados para resecuenciar auditorías luego
                proyectosAfectados = session.createQuery(
                    "SELECT DISTINCT a.proyecto.id FROM Auditoria a WHERE a.asignacion.id = :id AND a.proyecto IS NOT NULL",
                    Long.class
                ).setParameter("id", id).list();

                // 1. Limpieza en orden inverso de claves foráneas: solicitudes_retroalimentacion
                session.createNativeMutationQuery(
                    "DELETE FROM solicitudes_retroalimentacion WHERE hallazgo_id IN (" +
                    "  SELECT h.id FROM hallazgos h " +
                    "  JOIN resultados_regla rr ON h.resultado_regla_id = rr.id " +
                    "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                    "  WHERE a.asignacion_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                // 2. Limpieza de hallazgos
                session.createNativeMutationQuery(
                    "DELETE FROM hallazgos WHERE resultado_regla_id IN (" +
                    "  SELECT rr.id FROM resultados_regla rr " +
                    "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                    "  WHERE a.asignacion_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                // 3. Limpieza de resultados_regla
                session.createNativeMutationQuery(
                    "DELETE FROM resultados_regla WHERE auditoria_id IN (" +
                    "  SELECT a.id FROM auditorias a WHERE a.asignacion_id = :id" +
                    ")"
                ).setParameter("id", id).executeUpdate();

                // 4. Limpieza de auditorías vinculadas
                session.createNativeMutationQuery(
                    "DELETE FROM auditorias WHERE asignacion_id = :id"
                ).setParameter("id", id).executeUpdate();

                // 5. Limpieza de tabla asignacion_regla
                session.createNativeMutationQuery(
                    "DELETE FROM asignacion_regla WHERE asignacion_id = :id"
                ).setParameter("id", id).executeUpdate();

                // 6. Remover la asignación
                session.createNativeMutationQuery(
                    "DELETE FROM asignaciones WHERE id = :id"
                ).setParameter("id", id).executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            revertirTransaccion(tx);
            throw new RuntimeException("Error al eliminar la asignación: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        // 7. Resecuenciar asignaciones restantes
        resecuenciarIdsAsignaciones();

        // 8. Resecuenciar auditorías de los proyectos afectados
        if (!proyectosAfectados.isEmpty()) {
            AuditoriaDAO audDAO = new AuditoriaDAOImpl();
            for (Long pid : proyectosAfectados) {
                audDAO.resecuenciarAuditoriasDeProyecto(pid);
            }
        }
    }

    @Override
    public void resecuenciarIdsAsignaciones() {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Asignacion> lista = session.createQuery(
                "SELECT a FROM Asignacion a ORDER BY a.id ASC", Asignacion.class
            ).list();

            long seq = 1L;
            for (Asignacion a : lista) {
                a.setIdSecuencial(seq++);
                session.merge(a);
            }
            tx.commit();
        } catch (Exception e) {
            revertirTransaccion(tx);
            throw new RuntimeException("Error al resecuenciar asignaciones: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
