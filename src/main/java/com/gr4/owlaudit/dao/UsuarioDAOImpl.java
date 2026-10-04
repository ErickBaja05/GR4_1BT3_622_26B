package com.gr4.owlaudit.dao;

import com.gr4.owlaudit.common.config.HibernateUtil;
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
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(usuario);
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error al guardar el usuario: " + e.getMessage(), e);
        }
    }
}
