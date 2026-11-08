package com.gr4.owlaudit.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ListadoEstudiantesTest {

    @Test
    void deberiaCrearMatriculaActivaConClaveCompuestaYFechaMatricula() {
        Curso curso = new Curso();
        curso.setId(10L);
        curso.setCodigo("CSW-2026B");
        curso.setNombre("Construcción de Software");

        Estudiante estudiante = new Estudiante();
        estudiante.setId(25L);
        estudiante.setNombre("Erick Bajaña");

        // Método de dominio a crear en GREEN
        ListadoEstudiantes matricula = ListadoEstudiantes.crearNuevaMatricula(curso, estudiante);
        curso.getListadoEstudiantes().add(matricula);

        assertNotNull(matricula.getId());
        assertEquals(10L, matricula.getId().getIdCurso());
        assertEquals(25L, matricula.getId().getIdEstudiante());
        assertNotNull(matricula.getFechaMatricula());
        assertEquals("ACTIVO", matricula.getEstado());
        assertTrue(curso.estaMatriculado(25L));
    }
}
