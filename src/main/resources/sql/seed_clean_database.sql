-- ====================================================================
-- Script de Limpieza y Reinicio de Índices a Cero con Datos Simulados
-- Base de Datos: owl_auditv1
-- Mantiene intacta la tabla `usuarios`
-- ====================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Limpieza total de tablas dependientes y reseteo de secuencias
TRUNCATE TABLE solicitudes_retroalimentacion;
TRUNCATE TABLE hallazgos;
TRUNCATE TABLE resultados_regla;
TRUNCATE TABLE auditorias;
TRUNCATE TABLE asignacion_regla;
TRUNCATE TABLE asignaciones;
TRUNCATE TABLE proyectos;
TRUNCATE TABLE reglas;
TRUNCATE TABLE listado_estudiantes;
TRUNCATE TABLE cursos;

ALTER TABLE cursos AUTO_INCREMENT = 1;
ALTER TABLE reglas AUTO_INCREMENT = 1;
ALTER TABLE proyectos AUTO_INCREMENT = 1;
ALTER TABLE asignaciones AUTO_INCREMENT = 1;
ALTER TABLE auditorias AUTO_INCREMENT = 1;
ALTER TABLE resultados_regla AUTO_INCREMENT = 1;
ALTER TABLE hallazgos AUTO_INCREMENT = 1;
ALTER TABLE solicitudes_retroalimentacion AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- 1.5. Inserción de Curso Base y Matrículas (ListadoEstudiantes)
INSERT INTO cursos (id_curso, id_docente, codigo, nombre, periodo_academico, descripcion) VALUES
(1, 1, 'SW-2026B', 'Construcción de Software', '2026B', 'Curso de Ingeniería y Calidad de Software.');

INSERT INTO listado_estudiantes (id_curso, id_estudiante, fecha_matricula, estado) VALUES
(1, 2, '2026-10-15 08:00:00', 'ACTIVO'),
(1, 3, '2026-10-15 08:05:00', 'ACTIVO');

-- 2. Inserción de Reglas de Evaluación Base del Curso (Total: 100 pts)
INSERT INTO reglas (id, id_curso, nombre_representativo, tipo_motor, parametro_exacto, ponderacion, nivel_severidad, vigencia, activa, descripcion_detallada) VALUES
(1, 1, 'Verificar presencia de README.md', 'PRESENCIA_ARCHIVO', 'README.md', 25, 'ALTA', 'VIGENTE', 1, 'Verifica la existencia del archivo de documentación técnica README.md en la raíz del repositorio.'),
(2, 1, 'Estructura estándar de código Java', 'ESTRUCTURA_CARPETAS', 'src/main/java', 25, 'MEDIA', 'VIGENTE', 1, 'Comprueba que el repositorio disponga de la estructura estándar de paquetes Java bajo src/main/java.'),
(3, 1, 'Estructura de pruebas unitarias', 'ESTRUCTURA_CARPETAS', 'src/test/java', 25, 'MEDIA', 'VIGENTE', 1, 'Asegura la inclusión de la carpeta de pruebas unitarias src/test/java para la suite de testing.'),
(4, 1, 'Exclusión de binarios compilados', 'RESTRICCION_ARCHIVOS', '.class', 25, 'ALTA', 'VIGENTE', 1, 'Restringe la inclusión de archivos binarios compilados .class que deben ser excluidos mediante .gitignore.');

-- 3. Inserción de Proyectos Académicos Base
-- Estudiante Uno (id=2) tiene 2 proyectos; Estudiante Dos (id=3) tiene 1 proyecto.
INSERT INTO proyectos (id, nombre, url_github, fecha_registro, usuario_id, id_estudiante) VALUES
(1, 'TDD GREEN', 'https://github.com/AndresVeas/Jibaja_Veas', '2026-10-20 09:00:00', 2, 1),
(2, 'Sistema Biblioteca', 'https://github.com/AndresVeas/biblioteca-app', '2026-10-25 14:00:00', 2, 2),
(3, 'OwlAudit Scanner', 'https://github.com/maria-gonzalez/owlaudit-scanner', '2026-10-28 11:30:00', 3, 1);

-- 4. Inserción de Asignaciones Académicas del Curso
INSERT INTO asignaciones (id, id_curso, nombre, descripcion, umbral_aprobacion, id_secuencial, numero_intentos, fecha_inicio, fecha_limite) VALUES
(1, 1, 'Taller 1: Fundamentos de Arquitectura Java', 'Evaluación de estructura inicial del proyecto Java.', 70.0, 1, 3, '2026-10-20 00:00:00', '2026-11-15 23:59:59'),
(2, 1, 'Entrega Semestral: Calidad Continua', 'Evaluación integral de reglas de calidad del curso.', 80.0, 2, 5, '2026-10-25 00:00:00', '2026-11-30 23:59:59');

-- Vinculación Asignaciones <-> Reglas
-- Taller 1: Reglas 1, 2, 3 (Total 75 pts)
INSERT INTO asignacion_regla (asignacion_id, regla_id, nivel_severidad, ponderacion) VALUES
(1, 1, 'ALTA', 25),
(1, 2, 'MEDIA', 25),
(1, 3, 'MEDIA', 25);

-- Entrega Semestral: Reglas 1, 2, 3, 4 (Total 100 pts)
INSERT INTO asignacion_regla (asignacion_id, regla_id, nivel_severidad, ponderacion) VALUES
(2, 1, 'ALTA', 25),
(2, 2, 'MEDIA', 25),
(2, 3, 'MEDIA', 25),
(2, 4, 'ALTA', 25);

-- 5. Inserción de Auditorías Simuladas para Proyecto 1 ("TDD GREEN", Estudiante 2) vinculadas a Asignaciones del Curso
INSERT INTO auditorias (id, fecha_hora, porcentaje, puntaje_maximo, puntaje_obtenido, proyecto_id, usuario_id, dictamen, asignacion_id, numero_en_asignacion, numero_en_proyecto, tipo_auditoria) VALUES
(1, '2026-10-26 10:00:00', 50.0, 100, 50, 1, 2, 'NO_APROBADA', 2, 1, 1, 'ASIGNACION'),
(2, '2026-10-28 14:30:00', 75.0, 100, 75, 1, 2, 'NO_APROBADA', 2, 2, 2, 'ASIGNACION'),
(3, '2026-10-30 18:00:00', 100.0, 100, 100, 1, 2, 'APROBADA', 2, 3, 3, 'ASIGNACION');

-- Auditorías de Asignación (AA-1, AA-2 en Asignación 1 'Taller 1')
INSERT INTO auditorias (id, fecha_hora, porcentaje, puntaje_maximo, puntaje_obtenido, proyecto_id, usuario_id, dictamen, asignacion_id, numero_en_asignacion, numero_en_proyecto, tipo_auditoria) VALUES
(4, '2026-10-29 09:15:00', 66.67, 75, 50, 1, 2, 'NO_APROBADA', 1, 1, 4, 'ASIGNACION'),
(5, '2026-10-31 16:45:00', 100.0, 75, 75, 1, 2, 'APROBADA', 1, 2, 5, 'ASIGNACION');

-- 6. Inserción de Resultados de Evaluación por Regla
-- Auditoría 1 (General 1): Falla regla 3 y regla 4
INSERT INTO resultados_regla (id, auditoria_id, regla_id, cumple, puntos_obtenidos) VALUES
(1, 1, 1, 1, 25),
(2, 1, 2, 1, 25),
(3, 1, 3, 0, 0),
(4, 1, 4, 0, 0);

-- Auditoría 2 (General 2): Corrige regla 3, persiste falla en regla 4
INSERT INTO resultados_regla (id, auditoria_id, regla_id, cumple, puntos_obtenidos) VALUES
(5, 2, 1, 1, 25),
(6, 2, 2, 1, 25),
(7, 2, 3, 1, 25),
(8, 2, 4, 0, 0);

-- Auditoría 3 (General 3): Corrige todas las reglas
INSERT INTO resultados_regla (id, auditoria_id, regla_id, cumple, puntos_obtenidos) VALUES
(9, 3, 1, 1, 25),
(10, 3, 2, 1, 25),
(11, 3, 3, 1, 25),
(12, 3, 4, 1, 25);

-- Auditoría 4 (Asignación 1, Intento 1): Falla regla 3
INSERT INTO resultados_regla (id, auditoria_id, regla_id, cumple, puntos_obtenidos) VALUES
(13, 4, 1, 1, 25),
(14, 4, 2, 1, 25),
(15, 4, 3, 0, 0);

-- Auditoría 5 (Asignación 1, Intento 2): Aprueba todas las reglas de la asignación
INSERT INTO resultados_regla (id, auditoria_id, regla_id, cumple, puntos_obtenidos) VALUES
(16, 5, 1, 1, 25),
(17, 5, 2, 1, 25),
(18, 5, 3, 1, 25);

-- 7. Inserción de Hallazgos Técnicos
INSERT INTO hallazgos (id, resultado_regla_id, nivel_severidad, evidencia, recomendacion) VALUES
(1, 3, 'MEDIA', 'No se encontró la carpeta \'src/test/java\' en el repositorio', 'Cree la carpeta \'src/test/java\' en el repositorio'),
(2, 4, 'ALTA', 'El repositorio contiene archivos prohibidos: \'.class\'', 'Elimine del repositorio los archivos \'.class\' y agréguelos al .gitignore'),
(3, 8, 'ALTA', 'El repositorio contiene archivos prohibidos: \'.class\'', 'Elimine del repositorio los archivos \'.class\' y agréguelos al .gitignore'),
(4, 15, 'MEDIA', 'No se encontró la carpeta \'src/test/java\' en el repositorio', 'Cree la carpeta \'src/test/java\' en el repositorio');

-- 8. Inserción de Solicitud de Retroalimentación de Ejemplo
INSERT INTO solicitudes_retroalimentacion (id, hallazgo_id, usuario_id, id_docente, estado, justificacion, orientacion_tecnica, fecha_solicitud) VALUES
(1, 2, 2, NULL, 'PENDIENTE', 'Solicito aclaración sobre cómo estructurar el archivo .gitignore para evitar incluir binarios compilados .class en subdirectorios.', NULL, '2026-10-30 19:00:00');
