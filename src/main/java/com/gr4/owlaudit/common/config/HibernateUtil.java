package com.gr4.owlaudit.common.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            Configuration configuration = new Configuration().configure();

            String dbHost = System.getenv("DB_HOST");
            String dbPort = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String dbUser = System.getenv("DB_USER");
            String dbPass = System.getenv("DB_PASSWORD");

            if (dbHost != null && !dbHost.trim().isEmpty()) {
                String port = (dbPort != null && !dbPort.trim().isEmpty()) ? dbPort.trim() : "3306";
                String db = (dbName != null && !dbName.trim().isEmpty()) ? dbName.trim() : "owl_auditv1";
                String url = "jdbc:mariadb://" + dbHost.trim() + ":" + port + "/" + db + "?useSSL=false&serverTimezone=UTC";
                configuration.setProperty("hibernate.connection.url", url);
            }
            if (dbUser != null && !dbUser.trim().isEmpty()) {
                configuration.setProperty("hibernate.connection.username", dbUser.trim());
            }
            if (dbPass != null && !dbPass.trim().isEmpty()) {
                configuration.setProperty("hibernate.connection.password", dbPass.trim());
            }

            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Fallo al inicializar la SessionFactory de Hibernate: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
