package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Estudiante;
import com.gr4.owlaudit.model.RolEnum;
import com.gr4.owlaudit.model.Usuario;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class UsuarioDAOImpl implements UsuarioDAO {

    @Override
    public Usuario buscarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(TRIM(u.correo)) = LOWER(TRIM(:correo))",
                Usuario.class
            );
            query.setParameter("correo", correo);
            return query.uniqueResult();
        }
    }

    @Override
    public Usuario buscarPorCredenciales(String correo, String contrasena) {
        if (correo == null || contrasena == null) {
            return null;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Usuario> query = session.createQuery(
                "SELECT u FROM Usuario u WHERE LOWER(TRIM(u.correo)) = LOWER(TRIM(:correo)) AND u.contrasena = :contrasena",
                Usuario.class
            );
            query.setParameter("correo", correo);
            query.setParameter("contrasena", contrasena);
            return query.uniqueResult();
        }
    }

    @Override
    public void guardar(Usuario usuario) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Usuario entidad = usuario;
            if (usuario != null && !(usuario instanceof Docente) && !(usuario instanceof Estudiante)) {
                if (usuario.getRol() == RolEnum.DOCENTE) {
                    entidad = Docente.desdeUsuario(usuario);
                } else if (usuario.getRol() == RolEnum.ESTUDIANTE) {
                    entidad = Estudiante.desdeUsuario(usuario);
                }
            }
            session.persist(entidad);
            if (entidad != usuario && usuario != null) {
                usuario.setId(entidad.getId());
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try {
                    tx.rollback();
                } catch (Exception rollbackEx) {
                    e.addSuppressed(rollbackEx);
                }
            }
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
