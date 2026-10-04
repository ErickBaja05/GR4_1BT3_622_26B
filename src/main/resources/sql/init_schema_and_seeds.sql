-- ========================================================
-- OwlAudit - Incremento 1: Datos Semilla (Seeds) de Usuarios
-- ========================================================
-- NOTA DE ARQUITECTURA ORM (Hibernate):
-- La estructura de las tablas (usuarios, reglas, proyectos) es generada
-- y actualizada automáticamente por Hibernate ORM a través de la propiedad
-- <property name="hbm2ddl.auto">update</property> en hibernate.cfg.xml.
--
-- Por lo tanto, NO se requiere ejecutar DDL (CREATE TABLE) manual.
-- Este script se proporciona exclusivamente para cargar los datos semilla
-- de prueba requeridos para validar el inicio de sesión (Login).
-- ========================================================

-- Limpieza preventiva de usuarios de prueba (opcional)
DELETE FROM usuarios WHERE correo IN ('docente@epn.edu.ec', 'estudiante1@epn.edu.ec', 'estudiante2@epn.edu.ec');

-- Inserción de 1 usuario con rol DOCENTE
INSERT INTO usuarios (nombre, correo, contrasena, rol)
VALUES ('Profesor Titular', 'docente@epn.edu.ec', 'docente123', 'DOCENTE');

-- Inserción de 2 usuarios con rol ESTUDIANTE
INSERT INTO usuarios (nombre, correo, contrasena, rol)
VALUES 
    ('Estudiante Uno', 'estudiante1@epn.edu.ec', 'estudiante123', 'ESTUDIANTE'),
    ('Estudiante Dos', 'estudiante2@epn.edu.ec', 'estudiante456', 'ESTUDIANTE');
