package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.CursoDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.Curso;
import com.gr4.owlaudit.model.Docente;
import com.gr4.owlaudit.model.Regla;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TDD Suite para EP-01 >> FT-02 >> HU-01: Crear reglas para el curso.
 * Ciclo 1: CA3 - Escenario 3.1 (Campos obligatorios).
 * Ciclo 2: CA3 - Escenario 3.2 (Parámetro según motor).
 * Ciclo 3: CA2 - Escenarios 2.1 y 2.2 (Unicidad por curso).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CreacionReglaCursoTest {

    @Mock
    private ReglaDAO reglaDAO;

    @Mock
    private CursoDAO cursoDAO;

    private ValidadorRegla validadorRegla;
    private ReglaServiceImpl reglaService;

    private Curso cursoCSW;
    private Curso cursoCAL;
    private Curso cursoARQ;

    @BeforeEach
    void setUp() {
        validadorRegla = new ValidadorRegla();
        reglaService = new ReglaServiceImpl(reglaDAO, cursoDAO, validadorRegla);

        Docente docente = new Docente();
        docente.setId(1L);
        docente.setNombre("Docente Titular");

        cursoCSW = new Curso(docente, "CSW-2026B", "Construcción de Software", "2026B", "Curso CSW");
        cursoCSW.setId(10L);

        cursoCAL = new Curso(docente, "CAL-2026B", "Calidad de Software", "2026B", "Curso CAL");
        cursoCAL.setId(20L);

        cursoARQ = new Curso(docente, "ARQ-2026B", "Arquitectura de Software", "2026B", "Curso ARQ");
        cursoARQ.setId(30L);

        when(cursoDAO.buscarPorId(10L)).thenReturn(cursoCSW);
        when(cursoDAO.buscarPorId(20L)).thenReturn(cursoCAL);
        when(cursoDAO.buscarPorId(30L)).thenReturn(cursoARQ);
    }

    // ========================================================================
    // CA3 - Escenario 3.1: Campos obligatorios
    // ========================================================================
    @Test
    @DisplayName("CA3 - Escenario 3.1: Given docente en formulario de regla, When omite campos obligatorios o curso, Then bloquea el registro e informa que todos los campos son obligatorios")
    void givenCamposVaciosOSinCurso_whenValidarRegla_thenLanzaExcepcionCamposObligatorios() {
        // Caso A: Sin curso (idCurso nulo)
        NuevaReglaDTO dtoSinCurso = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "Descripción", "README.md", "ALTA", 25);
        dtoSinCurso.setIdCurso(null);
        ExcepcionNegocio ex0 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinCurso)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex0.getMessage(), "Debe exigir curso obligatorio");

        // Caso B: Tipo de motor vacío
        NuevaReglaDTO dtoSinMotor = new NuevaReglaDTO("", "Exigir README", "Descripción", "README.md", "ALTA", 25);
        dtoSinMotor.setIdCurso(10L);
        ExcepcionNegocio ex1 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinMotor)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex1.getMessage(), "Debe exigir tipo de motor obligatorio");

        // Caso C: Nombre representativo vacío o solo espacios
        NuevaReglaDTO dtoSinNombre = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "   ", "Descripción", "README.md", "ALTA", 25);
        dtoSinNombre.setIdCurso(10L);
        ExcepcionNegocio ex2 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinNombre)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex2.getMessage(), "Debe exigir nombre representativo obligatorio");

        // Caso D: Descripción detallada vacía
        NuevaReglaDTO dtoSinDesc = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "", "README.md", "ALTA", 25);
        dtoSinDesc.setIdCurso(10L);
        ExcepcionNegocio ex3 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinDesc)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex3.getMessage(), "Debe exigir descripción obligatoria");

        // Caso E: Parámetro exacto vacío
        NuevaReglaDTO dtoSinParam = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Exigir README", "Descripción", "", "ALTA", 25);
        dtoSinParam.setIdCurso(10L);
        ExcepcionNegocio ex4 = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dtoSinParam)
        );
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS, ex4.getMessage(), "Debe exigir parámetro exacto obligatorio");
    }

    // ========================================================================
    // CA3 - Escenario 3.2: Parámetro exacto por motor
    // ========================================================================
    @ParameterizedTest(name = "Motor: {0} | Parámetro inválido: {1}")
    @CsvSource(delimiter = ';', value = {
            "CONVENCION_COMMITS; mensaje_sin_dos_puntos; Para CONVENCION_COMMITS, el patrón debe incluir el separador ':'.",
            "RESTRICCION_ARCHIVOS; src/test/archivo.class; Para RESTRICCION_ARCHIVOS, el parámetro no puede contener barras separadoras '/'.",
            "ESTRUCTURA_CARPETAS; /src/main/java/; Para ESTRUCTURA_CARPETAS, la ruta no puede empezar ni terminar con '/'.",
            "PRESENCIA_ARCHIVO; doc?.pdf; El parámetro exacto contiene el carácter prohibido: '?'."
    })
    @DisplayName("CA3 - Escenario 3.2: Given motor seleccionado, When ingresa parametro sintacticamente invalido, Then rechaza registro con mensaje indicando formato esperado")
    void givenDocenteSeleccionaTipoMotor_whenIngresaParametroInvalidoParaEseMotor_thenRechazaRegistroConMensajeFormatoEsperado(
            String motor, String parametroInvalido, String mensajeEsperado) {
        NuevaReglaDTO dto = new NuevaReglaDTO(
                motor,
                "Regla Formato Invalido",
                "Descripción detallada de la regla de prueba",
                parametroInvalido,
                "ALTA",
                25
        );
        dto.setIdCurso(10L);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                validadorRegla.validarSintaxisYFormato(dto)
        );
        assertEquals(mensajeEsperado.trim(), ex.getMessage().trim());
    }

    // ========================================================================
    // CA2 - Escenario 2.1: Rechazo por nombre duplicado en el mismo curso
    // ========================================================================
    @Test
    @DisplayName("CA2 - Escenario 2.1: Given regla existente en curso CSW-2026B, When docente intenta registrar regla con mismo nombre en ese curso, Then rechaza registro con mensaje de duplicidad por curso")
    void givenReglaExistenteEnCurso_whenDocenteRegistraMismoNombreEnEseCurso_thenLanzaExcepcionNombreDuplicadoEnCurso() {
        NuevaReglaDTO dto = new NuevaReglaDTO(
                "ESTRUCTURA_CARPETAS",
                "Validar Estructura MVC",
                "Estructura estándar de controladores y vistas",
                "src/main/java",
                "MEDIA",
                20
        );
        dto.setIdCurso(10L);

        // Simulamos que en CSW-2026B (id=10) YA existe una regla con ese nombre
        when(reglaDAO.existeNombreEnCurso("Validar Estructura MVC", 10L)).thenReturn(true);

        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                reglaService.registrarReglaEvaluacionTecnica(dto)
        );

        assertEquals(ReglaServiceImpl.MENSAJE_DUPLICADO_CURSO, ex.getMessage());
        verify(reglaDAO, never()).guardar(any(Regla.class));
    }

    // ========================================================================
    // CA2 - Escenario 2.2: Permitir mismo nombre en cursos diferentes
    // ========================================================================
    @Test
    @DisplayName("CA2 - Escenario 2.2: Given regla existente en curso CSW-2026B, When docente registra regla con mismo nombre en curso ARQ-2026B, Then registro es exitoso asociando al curso correspondiente")
    void givenReglaExistenteEnUnCurso_whenDocenteRegistraMismoNombreEnOtroCursoDiferente_thenRegistroExitoso() {
        // En CSW-2026B (id=10) existe la regla
        when(reglaDAO.existeNombreEnCurso("Validar Estructura MVC", 10L)).thenReturn(true);
        // En ARQ-2026B (id=30) NO existe la regla
        when(reglaDAO.existeNombreEnCurso("Validar Estructura MVC", 30L)).thenReturn(false);

        NuevaReglaDTO dtoARQ = new NuevaReglaDTO(
                "ESTRUCTURA_CARPETAS",
                "Validar Estructura MVC",
                "Estructura para arquitectura",
                "src/main/java",
                "MEDIA",
                20
        );
        dtoARQ.setIdCurso(30L);

        reglaService.registrarReglaEvaluacionTecnica(dtoARQ);

        ArgumentCaptor<Regla> reglaCaptor = ArgumentCaptor.forClass(Regla.class);
        verify(reglaDAO).guardar(reglaCaptor.capture());

        Regla guardada = reglaCaptor.getValue();
        assertNotNull(guardada);
        assertEquals("Validar Estructura MVC", guardada.getNombreRepresentativo());
        assertNotNull(guardada.getCurso());
        assertEquals(30L, guardada.getCurso().getId());
    }

    // ========================================================================
    // CA1 - Escenario 1.1: Registro exitoso con estado VIGENTE asociado al curso
    // ========================================================================
    @Test
    @DisplayName("CA1 - Escenario 1.1: Given docente en detalle de curso CSW-2026B, When registra regla con valores validos, Then regla queda asociada al curso y con estado VIGENTE")
    void givenDocenteEnDetalleCurso_whenRegistraReglaConValoresValidos_thenReglaQuedaAsociadaAlCursoYEnEstadoVigente() {
        NuevaReglaDTO dto = new NuevaReglaDTO(
                "PRESENCIA_ARCHIVO",
                "Exigir README",
                "Verifica la presencia de README.md en la raíz",
                "README.md",
                "ALTA",
                25
        );
        dto.setIdCurso(10L);

        when(reglaDAO.existeNombreEnCurso("Exigir README", 10L)).thenReturn(false);

        reglaService.registrarReglaEvaluacionTecnica(dto);

        ArgumentCaptor<Regla> reglaCaptor = ArgumentCaptor.forClass(Regla.class);
        verify(reglaDAO).guardar(reglaCaptor.capture());

        Regla guardada = reglaCaptor.getValue();
        assertNotNull(guardada);
        assertEquals("Exigir README", guardada.getNombreRepresentativo());
        assertEquals("README.md", guardada.getParametroExacto());
        assertEquals(com.gr4.owlaudit.model.EstadoVigencia.VIGENTE, guardada.getVigencia());
        assertNotNull(guardada.getCurso());
        assertEquals(10L, guardada.getCurso().getId());
    }

    // ========================================================================
    // CA1 - Escenario 1.2: Independencia del catálogo de reglas entre distintos cursos
    // ========================================================================
    @Test
    @DisplayName("CA1 - Escenario 1.2: Given reglas registradas en distintos cursos, When docente consulta catalogo de curso CAL-2026B, Then lista unicamente las reglas pertenecientes a ese curso")
    void givenReglasRegistradasEnDistintosCursos_whenDocenteConsultaCatalogoDeUnCurso_thenListaUnicamenteReglasPertenecientesAEseCurso() {
        Regla reglaCSW = new Regla();
        reglaCSW.setId(101L);
        reglaCSW.setNombreRepresentativo("Exigir README");
        reglaCSW.setCurso(cursoCSW);

        Regla reglaCAL = new Regla();
        reglaCAL.setId(201L);
        reglaCAL.setNombreRepresentativo("Estructura src/main/java");
        reglaCAL.setCurso(cursoCAL);

        when(reglaDAO.listarPorCurso(10L)).thenReturn(java.util.List.of(reglaCSW));
        when(reglaDAO.listarPorCurso(20L)).thenReturn(java.util.List.of(reglaCAL));

        java.util.List<Regla> catalogoCAL = reglaService.listarReglasPorCurso(20L);

        assertNotNull(catalogoCAL, "El catálogo del curso no debe ser nulo");
        assertEquals(1, catalogoCAL.size());
        assertEquals("Estructura src/main/java", catalogoCAL.get(0).getNombreRepresentativo());
        assertTrue(catalogoCAL.stream().noneMatch(r -> "Exigir README".equals(r.getNombreRepresentativo())));
    }
}
