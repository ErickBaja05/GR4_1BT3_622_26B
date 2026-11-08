package com.gr4.owlaudit.model;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.mapping.PersistentClass;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InfraestructuraOrmMerTest {

    @Test
    @DisplayName("Metamodelo Hibernate: todas las entidades del MER construyen el mapeo ORM sin errores")
    void metamodeloHibernateConstruyeMapeoCompletoSinErrores() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.dialect", "org.hibernate.dialect.MariaDBDialect")
                .applySetting("hibernate.temp.use_jdbc_metadata_defaults", "false")
                .build();

        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Usuario.class)
                    .addAnnotatedClass(Docente.class)
                    .addAnnotatedClass(Estudiante.class)
                    .addAnnotatedClass(Curso.class)
                    .addAnnotatedClass(ListadoEstudiantes.class)
                    .addAnnotatedClass(Regla.class)
                    .addAnnotatedClass(Asignacion.class)
                    .addAnnotatedClass(Proyecto.class)
                    .addAnnotatedClass(Auditoria.class)
                    .addAnnotatedClass(ResultadoRegla.class)
                    .addAnnotatedClass(Hallazgo.class)
                    .addAnnotatedClass(SolicitudRetroalimentacion.class)
                    .addAnnotatedClass(AsignacionRegla.class)
                    .buildMetadata();

            assertNotNull(metadata);

            PersistentClass usuarioMapping = metadata.getEntityBinding(Usuario.class.getName());
            PersistentClass docenteMapping = metadata.getEntityBinding(Docente.class.getName());
            PersistentClass estudianteMapping = metadata.getEntityBinding(Estudiante.class.getName());
            PersistentClass cursoMapping = metadata.getEntityBinding(Curso.class.getName());
            PersistentClass listadoMapping = metadata.getEntityBinding(ListadoEstudiantes.class.getName());

            assertNotNull(usuarioMapping);
            assertNotNull(docenteMapping);
            assertNotNull(estudianteMapping);
            assertNotNull(cursoMapping);
            assertNotNull(listadoMapping);

            // Docente y Estudiante comparten la misma tabla física 'usuarios' (SINGLE_TABLE)
            assertEquals("usuarios", usuarioMapping.getTable().getName());
            assertEquals("usuarios", docenteMapping.getTable().getName());
            assertEquals("usuarios", estudianteMapping.getTable().getName());
            assertEquals("DOCENTE", docenteMapping.getDiscriminatorValue());
            assertEquals("ESTUDIANTE", estudianteMapping.getDiscriminatorValue());
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    @Test
    @DisplayName("Herencia SINGLE_TABLE: Docente y Estudiante heredan de Usuario y fijan su rol automáticamente")
    void herenciaSingleTableDocenteYEstudiante() {
        Inheritance inheritance = Usuario.class.getAnnotation(Inheritance.class);
        DiscriminatorColumn discriminator = Usuario.class.getAnnotation(DiscriminatorColumn.class);
        assertNotNull(inheritance);
        assertEquals(InheritanceType.SINGLE_TABLE, inheritance.strategy());
        assertNotNull(discriminator);
        assertEquals("rol", discriminator.name());

        assertEquals("DOCENTE", Docente.class.getAnnotation(DiscriminatorValue.class).value());
        assertEquals("ESTUDIANTE", Estudiante.class.getAnnotation(DiscriminatorValue.class).value());

        Docente docente = new Docente(1L, "Dr. Julio", "docente@epn.edu.ec", "clave123");
        Estudiante estudiante = new Estudiante(2L, "Andrés", "estudiante@epn.edu.ec", "clave456");

        assertInstanceOf(Usuario.class, docente);
        assertInstanceOf(Usuario.class, estudiante);
        assertEquals(RolEnum.DOCENTE, docente.getRol());
        assertEquals(RolEnum.ESTUDIANTE, estudiante.getRol());

        Usuario baseDocente = new Usuario(10L, "Docente Base", "d@epn.edu.ec", "123", RolEnum.DOCENTE);
        Docente convertidoDocente = Docente.desdeUsuario(baseDocente);
        assertEquals(10L, convertidoDocente.getId());
        assertEquals(RolEnum.DOCENTE, convertidoDocente.getRol());

        Usuario baseEstudiante = new Usuario(20L, "Estudiante Base", "e@epn.edu.ec", "123", RolEnum.ESTUDIANTE);
        Estudiante convertidoEstudiante = Estudiante.desdeUsuario(baseEstudiante);
        assertEquals(20L, convertidoEstudiante.getId());
        assertEquals(RolEnum.ESTUDIANTE, convertidoEstudiante.getRol());
    }

    @Test
    @DisplayName("Relación 1 Docente -> N Cursos: 1 Curso tiene asignado 1 único Docente obligatorio")
    void relacionDocenteACursosYCursoADocenteUnico() throws NoSuchFieldException {
        Field campoDocente = Curso.class.getDeclaredField("docente");
        ManyToOne manyToOne = campoDocente.getAnnotation(ManyToOne.class);
        JoinColumn joinColumn = campoDocente.getAnnotation(JoinColumn.class);

        assertNotNull(manyToOne);
        assertFalse(manyToOne.optional(), "El docente titular del curso debe ser obligatorio");
        assertNotNull(joinColumn);
        assertEquals("id_docente", joinColumn.name());
        assertFalse(joinColumn.nullable(), "La columna id_docente debe ser NOT NULL");

        Docente docente = new Docente(1L, "Profesor Titular", "docente@epn.edu.ec", "pass");
        Curso curso1 = new Curso(docente, "SW-2026B", "Construcción de Software", "2026B", "Curso 1");
        Curso curso2 = new Curso(docente, "AG-2026B", "Metodologías Ágiles", "2026B", "Curso 2");
        docente.setCursos(List.of(curso1, curso2));

        assertEquals(2, docente.getCursos().size());
        assertSame(docente, curso1.getDocente());
        assertSame(docente, curso2.getDocente());
        assertTrue(curso1.esDocenteTitular(1L));
        assertFalse(curso1.esDocenteTitular(99L));
    }

    @Test
    @DisplayName("Matrícula ListadoEstudiantes: sincroniza clave primaria compuesta (id_curso, id_estudiante)")
    void matriculaListadoEstudiantesSincronizaClaveCompuesta() {
        Docente docente = new Docente(1L, "Profesor", "doc@epn.edu.ec", "pass");
        Curso curso = new Curso(docente, "ARQ-2026B", "Arquitectura", "2026B", "Desc");
        curso.setId(5L);

        Estudiante estudiante = new Estudiante(12L, "Estudiante", "est@epn.edu.ec", "pass");
        LocalDateTime fecha = LocalDateTime.of(2026, 11, 7, 10, 0);

        ListadoEstudiantes matricula = new ListadoEstudiantes(curso, estudiante, fecha, "ACTIVO");

        assertEquals(5L, matricula.getId().getIdCurso());
        assertEquals(12L, matricula.getId().getIdEstudiante());
        assertEquals(new ListadoEstudiantesId(5L, 12L), matricula.getId());
        assertNotEquals(new ListadoEstudiantesId(5L, 99L), matricula.getId());
        assertEquals("ACTIVO", matricula.getEstado());
        assertEquals(fecha, matricula.getFechaMatricula());
    }

    @Test
    @DisplayName("Regla y Asignación: pertenecen obligatoriamente a Curso con unicidad compuesta por curso")
    void reglaYAsignacionPertenecenACursoConUnicidadCompuesta() throws NoSuchFieldException {
        Table tablaRegla = Regla.class.getAnnotation(Table.class);
        assertEquals(1, tablaRegla.uniqueConstraints().length);
        UniqueConstraint ucRegla = tablaRegla.uniqueConstraints()[0];
        assertArrayEquals(new String[]{"id_curso", "nombre_representativo"}, ucRegla.columnNames());

        Field cursoEnRegla = Regla.class.getDeclaredField("curso");
        assertFalse(cursoEnRegla.getAnnotation(ManyToOne.class).optional());
        assertFalse(cursoEnRegla.getAnnotation(JoinColumn.class).nullable());

        Table tablaAsignacion = Asignacion.class.getAnnotation(Table.class);
        assertEquals(1, tablaAsignacion.uniqueConstraints().length);
        UniqueConstraint ucAsignacion = tablaAsignacion.uniqueConstraints()[0];
        assertArrayEquals(new String[]{"id_curso", "nombre"}, ucAsignacion.columnNames());

        Field cursoEnAsignacion = Asignacion.class.getDeclaredField("curso");
        assertFalse(cursoEnAsignacion.getAnnotation(ManyToOne.class).optional());
        assertFalse(cursoEnAsignacion.getAnnotation(JoinColumn.class).nullable());
    }

    @Test
    @DisplayName("Auditoría, Proyecto y SolicitudRetroalimentacion: cardinalidades estrictas y tipado de roles")
    void auditoriaProyectoYSolicitudCardinalidadesYTipado() throws NoSuchFieldException {
        // Auditoria exige asignacion, proyecto y estudiante NOT NULL (sin auditorías libres)
        for (String nombreCampo : Arrays.asList("asignacion", "proyecto", "estudiante")) {
            Field f = Auditoria.class.getDeclaredField(nombreCampo);
            assertFalse(f.getAnnotation(ManyToOne.class).optional(), nombreCampo + " debe ser optional=false");
            assertFalse(f.getAnnotation(JoinColumn.class).nullable(), nombreCampo + " debe ser nullable=false");
        }

        // SolicitudRetroalimentacion exige estudiante NOT NULL y permite docente NULL hasta ser atendida
        Field campoEstudianteSol = SolicitudRetroalimentacion.class.getDeclaredField("estudiante");
        assertFalse(campoEstudianteSol.getAnnotation(JoinColumn.class).nullable());

        Field campoDocenteSol = SolicitudRetroalimentacion.class.getDeclaredField("docente");
        assertTrue(campoDocenteSol.getAnnotation(JoinColumn.class).nullable());

        Estudiante estudiante = new Estudiante(7L, "Alumno", "alumno@epn.edu.ec", "pass");
        Docente docente = new Docente(3L, "Docente", "docente@epn.edu.ec", "pass");

        SolicitudRetroalimentacion solicitud = new SolicitudRetroalimentacion();
        solicitud.setEstudiante(estudiante);
        assertNull(solicitud.getDocente());
        assertEquals(EstadoRetroEnum.PENDIENTE, solicitud.getEstado());

        solicitud.marcarSolicitudComoAtendida(docente);
        assertSame(docente, solicitud.getDocente());
        assertEquals(EstadoRetroEnum.ATENDIDA, solicitud.getEstado());
    }
}

