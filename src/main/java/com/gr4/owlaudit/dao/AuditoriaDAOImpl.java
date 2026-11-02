package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Auditoria;
import com.gr4.owlaudit.model.ResultadoRegla;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class AuditoriaDAOImpl implements AuditoriaDAO {

    @Override
    public void registrarAuditoriaEnElHistorial(Auditoria auditoria) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            if (auditoria.getProyecto() != null && auditoria.getProyecto().getId() != null) {
                Long pid = auditoria.getProyecto().getId();
                if (auditoria.esAuditoriaAsignacion()) {
                    auditoria.setTipoAuditoria("ASIGNACION");
                    if (auditoria.getNumeroEnAsignacion() == null && auditoria.getAsignacion() != null && auditoria.getAsignacion().getId() != null) {
                        Long nextNum = session.createQuery(
                            "SELECT COALESCE(MAX(a.numeroEnAsignacion), 0) + 1 FROM Auditoria a WHERE a.proyecto.id = :pid AND a.asignacion.id = :asigId", Long.class)
                            .setParameter("pid", pid)
                            .setParameter("asigId", auditoria.getAsignacion().getId())
                            .uniqueResult();
                        auditoria.setNumeroEnAsignacion(nextNum != null ? nextNum : 1L);
                    }
                } else {
                    auditoria.setTipoAuditoria("GENERAL");
                    if (auditoria.getNumeroEnProyecto() == null) {
                        Long nextNum = session.createQuery(
                            "SELECT COALESCE(MAX(a.numeroEnProyecto), 0) + 1 FROM Auditoria a WHERE a.proyecto.id = :pid AND a.asignacion IS NULL", Long.class)
                            .setParameter("pid", pid)
                            .uniqueResult();
                        auditoria.setNumeroEnProyecto(nextNum != null ? nextNum : 1L);
                    }
                }
            }
            // Por cascade se guardan también sus ResultadoRegla y Hallazgo
            session.persist(auditoria);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al registrar la auditoría en el historial: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Auditoria> consultarHistorialDeAuditorias(Long proyectoId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Auditoria> lista = session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.proyecto.id = :proyectoId " +
                "ORDER BY a.fechaHora ASC, a.id ASC",
                Auditoria.class
            )
            .setParameter("proyectoId", proyectoId)
            .getResultList();

            long sec = 1L;
            boolean necesitaUpdate = false;
            for (Auditoria a : lista) {
                if (a.getNumeroEnProyecto() == null) {
                    a.setNumeroEnProyecto(sec);
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
                try {
                    tx.rollback();
                } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al consultar el historial de auditorías: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public Long obtenerSiguienteNumeroEnProyecto(Long proyectoId) {
        if (proyectoId == null) return 1L;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "SELECT COALESCE(MAX(a.numeroEnProyecto), 0) + 1 FROM Auditoria a WHERE a.proyecto.id = :pid", Long.class)
                .setParameter("pid", proyectoId)
                .uniqueResult();
            return count != null ? count : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Override
    public Long obtenerSiguienteNumeroGeneralEnProyecto(Long proyectoId) {
        if (proyectoId == null) return 1L;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "SELECT COALESCE(MAX(a.numeroEnProyecto), 0) + 1 FROM Auditoria a WHERE a.proyecto.id = :pid AND a.asignacion IS NULL", Long.class)
                .setParameter("pid", proyectoId)
                .uniqueResult();
            return count != null ? count : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Override
    public Long obtenerSiguienteNumeroEnAsignacion(Long proyectoId, Long asignacionId) {
        if (proyectoId == null) return 1L;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                "SELECT COALESCE(MAX(a.numeroEnAsignacion), 0) + 1 FROM Auditoria a WHERE a.proyecto.id = :pid AND a.asignacion.id = :asigId", Long.class)
                .setParameter("pid", proyectoId)
                .setParameter("asigId", asignacionId)
                .uniqueResult();
            return count != null ? count : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }

    @Override
    public List<Auditoria> consultarHistorialAuditoriasGenerales(Long proyectoId) {
        if (proyectoId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Auditoria> list = session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.proyecto.id = :proyectoId AND a.asignacion IS NULL " +
                "ORDER BY a.fechaHora ASC, a.id ASC",
                Auditoria.class
            ).setParameter("proyectoId", proyectoId).getResultList();

            boolean hayNulos = list.stream().anyMatch(a -> a.getNumeroEnProyecto() == null || a.getTipoAuditoria() == null);
            if (hayNulos) {
                resecuenciarAuditoriasDeProyecto(proyectoId);
                return session.createQuery(
                    "SELECT DISTINCT a FROM Auditoria a " +
                    "LEFT JOIN FETCH a.proyecto " +
                    "LEFT JOIN FETCH a.resultados r " +
                    "LEFT JOIN FETCH r.regla " +
                    "LEFT JOIN FETCH r.hallazgo " +
                    "WHERE a.proyecto.id = :proyectoId AND a.asignacion IS NULL " +
                    "ORDER BY a.fechaHora ASC, a.id ASC",
                    Auditoria.class
                ).setParameter("proyectoId", proyectoId).getResultList();
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar historial de auditorías generales: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Auditoria> consultarHistorialAuditoriasAsignacion(Long proyectoId, Long asignacionId) {
        if (proyectoId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Auditoria> list = session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.proyecto.id = :proyectoId AND a.asignacion.id = :asigId " +
                "ORDER BY a.fechaHora ASC, a.id ASC",
                Auditoria.class
            )
            .setParameter("proyectoId", proyectoId)
            .setParameter("asigId", asignacionId)
            .getResultList();

            boolean hayNulos = list.stream().anyMatch(a -> a.getNumeroEnAsignacion() == null || a.getTipoAuditoria() == null);
            if (hayNulos) {
                resecuenciarAuditoriasDeProyecto(proyectoId);
                return session.createQuery(
                    "SELECT DISTINCT a FROM Auditoria a " +
                    "LEFT JOIN FETCH a.proyecto " +
                    "LEFT JOIN FETCH a.asignacion " +
                    "LEFT JOIN FETCH a.resultados r " +
                    "LEFT JOIN FETCH r.regla " +
                    "LEFT JOIN FETCH r.hallazgo " +
                    "WHERE a.proyecto.id = :proyectoId AND a.asignacion.id = :asigId " +
                    "ORDER BY a.fechaHora ASC, a.id ASC",
                    Auditoria.class
                )
                .setParameter("proyectoId", proyectoId)
                .setParameter("asigId", asignacionId)
                .getResultList();
            }
            return list;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar historial de auditorías de asignación: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Auditoria> consultarAuditoriasDeAsignacionPorUsuario(Long asignacionId, Long usuarioId) {
        if (asignacionId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto p " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.asignacion.id = :asigId ";
            if (usuarioId != null) {
                hql += "AND (p.usuario.id = :uid OR a.usuario.id = :uid) ";
            }
            hql += "ORDER BY a.fechaHora DESC, a.id DESC";

            var query = session.createQuery(hql, Auditoria.class)
                .setParameter("asigId", asignacionId);
            if (usuarioId != null) {
                query.setParameter("uid", usuarioId);
            }
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar auditorías de asignación por usuario: " + e.getMessage(), e);
        }
    }

    @Override
    public void resecuenciarAuditoriasDeProyecto(Long proyectoId) {
        if (proyectoId == null) return;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            List<Auditoria> generales = session.createQuery(
                "SELECT a FROM Auditoria a WHERE a.proyecto.id = :proyectoId AND a.asignacion IS NULL ORDER BY a.fechaHora ASC, a.id ASC",
                Auditoria.class
            ).setParameter("proyectoId", proyectoId).list();

            long seqGen = 1L;
            for (Auditoria a : generales) {
                a.setNumeroEnProyecto(seqGen++);
                a.setTipoAuditoria("GENERAL");
                session.merge(a);
            }

            List<Long> asignacionesIds = session.createQuery(
                "SELECT DISTINCT a.asignacion.id FROM Auditoria a WHERE a.proyecto.id = :proyectoId AND a.asignacion IS NOT NULL",
                Long.class
            ).setParameter("proyectoId", proyectoId).list();

            for (Long asigId : asignacionesIds) {
                List<Auditoria> asignadas = session.createQuery(
                    "SELECT a FROM Auditoria a WHERE a.proyecto.id = :proyectoId AND a.asignacion.id = :asigId ORDER BY a.fechaHora ASC, a.id ASC",
                    Auditoria.class
                ).setParameter("proyectoId", proyectoId).setParameter("asigId", asigId).list();

                long seqAsig = 1L;
                for (Auditoria a : asignadas) {
                    a.setNumeroEnAsignacion(seqAsig++);
                    a.setTipoAuditoria("ASIGNACION");
                    session.merge(a);
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al resecuenciar auditorías del proyecto: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<Auditoria> buscarPorAsignacionId(Long asignacionId) {
        if (asignacionId == null) return java.util.Collections.emptyList();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.asignacion.id = :asignacionId",
                Auditoria.class
            ).setParameter("asignacionId", asignacionId).list();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar auditorías por asignación: " + e.getMessage(), e);
        }
    }

    @Override
    public void actualizarAuditoria(Auditoria auditoria) {
        if (auditoria == null) return;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            session.merge(auditoria);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al actualizar la auditoría: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public void eliminarAuditoriasDeAsignacion(Long asignacionId) {
        if (asignacionId == null) return;
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            List<Long> proyectosIds = session.createQuery(
                "SELECT DISTINCT a.proyecto.id FROM Auditoria a WHERE a.asignacion.id = :asigId AND a.proyecto IS NOT NULL",
                Long.class
            ).setParameter("asigId", asignacionId).list();

            // 1. Limpieza de solicitudes_retroalimentacion vinculadas
            session.createNativeMutationQuery(
                "DELETE FROM solicitudes_retroalimentacion WHERE hallazgo_id IN (" +
                "  SELECT h.id FROM hallazgos h " +
                "  JOIN resultados_regla rr ON h.resultado_regla_id = rr.id " +
                "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                "  WHERE a.asignacion_id = :asigId" +
                ")"
            ).setParameter("asigId", asignacionId).executeUpdate();

            // 2. Limpieza de hallazgos
            session.createNativeMutationQuery(
                "DELETE FROM hallazgos WHERE resultado_regla_id IN (" +
                "  SELECT rr.id FROM resultados_regla rr " +
                "  JOIN auditorias a ON rr.auditoria_id = a.id " +
                "  WHERE a.asignacion_id = :asigId" +
                ")"
            ).setParameter("asigId", asignacionId).executeUpdate();

            // 3. Limpieza de resultados_regla
            session.createNativeMutationQuery(
                "DELETE FROM resultados_regla WHERE auditoria_id IN (" +
                "  SELECT a.id FROM auditorias a WHERE a.asignacion_id = :asigId" +
                ")"
            ).setParameter("asigId", asignacionId).executeUpdate();

            // 4. Limpieza de auditorias
            session.createNativeMutationQuery(
                "DELETE FROM auditorias WHERE asignacion_id = :asigId"
            ).setParameter("asigId", asignacionId).executeUpdate();

            tx.commit();

            for (Long pid : proyectosIds) {
                resecuenciarAuditoriasDeProyecto(pid);
            }
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ignored) {}
            }
            throw new RuntimeException("Error al eliminar auditorías de la asignación: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    @Override
    public List<ResultadoRegla> consultarResultadosDeReglas(Long baseId, Long comparadaId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT r FROM ResultadoRegla r " +
                "JOIN FETCH r.regla " +
                "JOIN FETCH r.auditoria " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE r.auditoria.id IN (:baseId, :comparadaId)",
                ResultadoRegla.class
            )
            .setParameter("baseId", baseId)
            .setParameter("comparadaId", comparadaId)
            .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar los resultados de reglas: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Auditoria> consultarTodas() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "ORDER BY a.fechaHora DESC",
                Auditoria.class
            ).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar todas las auditorías: " + e.getMessage(), e);
        }
    }

    @Override
    public Auditoria buscarPorId(Long id) {
        if (id == null) return null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.id = :id",
                Auditoria.class
            )
            .setParameter("id", id)
            .uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error al buscar auditoría por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Auditoria> consultarHistorialDeAuditoriasPorUsuario(Long usuarioId) {
        if (usuarioId == null) {
            return java.util.Collections.emptyList();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                "SELECT DISTINCT a FROM Auditoria a " +
                "LEFT JOIN FETCH a.proyecto " +
                "LEFT JOIN FETCH a.asignacion " +
                "LEFT JOIN FETCH a.resultados r " +
                "LEFT JOIN FETCH r.regla " +
                "LEFT JOIN FETCH r.hallazgo " +
                "WHERE a.usuario.id = :usuarioId OR a.proyecto.usuario.id = :usuarioId " +
                "ORDER BY a.fechaHora DESC",
                Auditoria.class
            )
            .setParameter("usuarioId", usuarioId)
            .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar auditorías por usuario: " + e.getMessage(), e);
        }
    }
}
