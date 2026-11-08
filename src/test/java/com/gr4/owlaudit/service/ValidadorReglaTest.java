package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas unitarias para ValidadorRegla adaptadas al contexto de curso (Task 2.3).
 */
public class ValidadorReglaTest {

    private ValidadorRegla validador;

    @BeforeEach
    public void setUp() {
        validador = new ValidadorRegla();
    }

    private NuevaReglaDTO crearDTOValido(String motor, String nombre, String desc, String param, String severidad, int ponderacion) {
        NuevaReglaDTO dto = new NuevaReglaDTO(motor, nombre, desc, param, severidad, ponderacion);
        dto.setIdCurso(1L);
        return dto;
    }

    @Test
    @DisplayName("Validar regla Presencia de Archivo exitosa con idCurso")
    public void testValidarReglaPresenciaArchivoExitosa() {
        NuevaReglaDTO dto = crearDTOValido(
                "PRESENCIA_ARCHIVO",
                "Verificar README",
                "Debe existir archivo README",
                "README.md",
                "ALTA",
                5
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dto));
        assertDoesNotThrow(() -> validador.validar(dto));
    }

    @Test
    @DisplayName("Validar regla Estructura de Carpetas exitosa con idCurso")
    public void testValidarReglaEstructuraCarpetasExitosa() {
        NuevaReglaDTO dto = crearDTOValido(
                "ESTRUCTURA_CARPETAS",
                "Verificar carpeta src",
                "Debe existir la estructura src/main",
                "src/main",
                "MEDIA",
                3
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dto));
    }

    @Test
    @DisplayName("Validar regla Restricción de Archivos exitosa con extensiones y comodines")
    public void testValidarReglaRestriccionArchivosExtensionExitosa() {
        NuevaReglaDTO dto = crearDTOValido(
                "RESTRICCION_ARCHIVOS",
                "Prohibir .env",
                "No debe subirse archivos de configuración",
                ".env",
                "ALTA",
                4
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dto));

        NuevaReglaDTO dtoAsterisco = crearDTOValido(
                "RESTRICCION_ARCHIVOS",
                "Prohibir binarios exe",
                "No permitir ejecutables",
                "*.exe",
                "ALTA",
                4
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dtoAsterisco));

        NuevaReglaDTO dtoExacto = crearDTOValido(
                "RESTRICCION_ARCHIVOS",
                "Prohibir id_rsa",
                "No permitir claves privadas",
                "id_rsa",
                "ALTA",
                5
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dtoExacto));
    }

    @Test
    @DisplayName("Rechazar DTO nulo")
    public void testValidarDTONulo() {
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                validador.validarSintaxisYFormato(null));
        assertEquals("Los datos de la regla no pueden ser nulos.", ex.getMessage());
    }

    @Test
    @DisplayName("Rechazar campos obligatorios nulos o vacíos incluyendo idCurso")
    public void testValidarCamposObligatoriosNulosOVacios() {
        // idCurso obligatorio
        NuevaReglaDTO dtoSinCurso = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "param", "ALTA", 1);
        dtoSinCurso.setIdCurso(null);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoSinCurso)).getMessage());

        // Tipo motor obligatorio
        NuevaReglaDTO dto1 = crearDTOValido(null, "Nombre", "Desc", "param", "ALTA", 1);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto1)).getMessage());

        NuevaReglaDTO dto1Vacio = crearDTOValido(" ", "Nombre", "Desc", "param", "ALTA", 1);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto1Vacio)).getMessage());

        // Nombre representativo obligatorio
        NuevaReglaDTO dto2 = crearDTOValido("PRESENCIA_ARCHIVO", null, "Desc", "param", "ALTA", 1);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto2)).getMessage());

        // Descripción obligatoria
        NuevaReglaDTO dto3 = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "", "param", "ALTA", 1);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto3)).getMessage());

        // Parámetro exacto obligatorio
        NuevaReglaDTO dto4 = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", null, "ALTA", 1);
        assertEquals(ValidadorRegla.MENSAJE_CAMPOS_OBLIGATORIOS,
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto4)).getMessage());
    }

    @Test
    @DisplayName("Rechazar tipo de motor inválido")
    public void testValidarTipoMotorInvalido() {
        NuevaReglaDTO dto = crearDTOValido("TIPO_INVENTADO", "Nombre", "Desc", "param", "ALTA", 1);
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () ->
                validador.validarSintaxisYFormato(dto));
        assertEquals("El tipo de motor 'TIPO_INVENTADO' no es válido. Debe ser PRESENCIA_ARCHIVO, RESTRICCION_ARCHIVOS, ESTRUCTURA_CARPETAS o CONVENCION_COMMITS.",
                ex.getMessage());
    }

    @Test
    @DisplayName("Rechazar nombre representativo con longitud mayor a 100")
    public void testValidarNombreLongitudInvalida() {
        NuevaReglaDTO dtoLargo = crearDTOValido("PRESENCIA_ARCHIVO", "A".repeat(101), "Desc", "README.md", "ALTA", 1);
        assertEquals("El nombre representativo debe tener entre 1 y 100 caracteres.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoLargo)).getMessage());
    }

    @Test
    @DisplayName("Rechazar descripción detallada con longitud mayor a 200")
    public void testValidarDescripcionLongitudInvalida() {
        NuevaReglaDTO dtoLarga = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "D".repeat(201), "README.md", "ALTA", 1);
        assertEquals("La descripción detallada debe tener entre 1 y 200 caracteres.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoLarga)).getMessage());
    }

    @Test
    @DisplayName("Rechazar parámetro con espacios en los extremos")
    public void testValidarParametroEspaciosExtremos() {
        NuevaReglaDTO dto = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", " README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede tener espacios en blanco al inicio o al final.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto)).getMessage());
    }

    @Test
    @DisplayName("Rechazar secuencias prohibidas de navegación en parámetro")
    public void testValidarParametroSecuenciasProhibidas() {
        NuevaReglaDTO dtoNavegacion = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "../README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede contener secuencias de navegación '..'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoNavegacion)).getMessage());

        NuevaReglaDTO dtoDobleBarra = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "src//README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede contener barras dobles '//'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoDobleBarra)).getMessage());
    }

    @Test
    @DisplayName("Rechazar caracteres prohibidos en parámetro")
    public void testValidarParametroCaracteresProhibidos() {
        char[] prohibidos = {':', '\\', '"', '<', '>', '|', '?'};
        for (char c : prohibidos) {
            NuevaReglaDTO dto = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "test" + c + "file", "ALTA", 1);
            ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto));
            assertEquals("El parámetro exacto contiene el carácter prohibido: '" + c + "'.", ex.getMessage());
        }
    }

    @Test
    @DisplayName("Rechazar reglas específicas inválidas por tipo de motor")
    public void testValidarReglasEspecificasPorTipoMotor() {
        // PRESENCIA_ARCHIVO
        NuevaReglaDTO dtoPresenciaSlashInicio = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "/README.md", "ALTA", 1);
        assertEquals("Para PRESENCIA_ARCHIVO, la ruta no puede empezar ni terminar con '/'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoPresenciaSlashInicio)).getMessage());

        NuevaReglaDTO dtoPresenciaComodin = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "*.md", "ALTA", 1);
        assertEquals("Para PRESENCIA_ARCHIVO, el parámetro no puede contener el carácter comodín '*'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoPresenciaComodin)).getMessage());

        // ESTRUCTURA_CARPETAS
        NuevaReglaDTO dtoCarpetasSlashFin = crearDTOValido("ESTRUCTURA_CARPETAS", "Nombre", "Desc", "src/", "ALTA", 1);
        assertEquals("Para ESTRUCTURA_CARPETAS, la ruta no puede empezar ni terminar con '/'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCarpetasSlashFin)).getMessage());

        NuevaReglaDTO dtoCarpetasComodin = crearDTOValido("ESTRUCTURA_CARPETAS", "Nombre", "Desc", "src/*", "ALTA", 1);
        assertEquals("Para ESTRUCTURA_CARPETAS, el parámetro no puede contener el carácter comodín '*'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCarpetasComodin)).getMessage());

        // RESTRICCION_ARCHIVOS
        NuevaReglaDTO dtoRestriccionSlash = crearDTOValido("RESTRICCION_ARCHIVOS", "Nombre", "Desc", "src/secret.key", "ALTA", 1);
        assertEquals("Para RESTRICCION_ARCHIVOS, el parámetro no puede contener barras separadoras '/'.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoRestriccionSlash)).getMessage());

        NuevaReglaDTO dtoRestriccionComodinInvalido = crearDTOValido("RESTRICCION_ARCHIVOS", "Nombre", "Desc", "abc*def", "ALTA", 1);
        assertEquals("Para RESTRICCION_ARCHIVOS, el parámetro debe ser una extensión (.ext, *.ext) o el nombre de un archivo prohibido exacto.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoRestriccionComodinInvalido)).getMessage());
    }

    @Test
    @DisplayName("Rechazar nivel de severidad inválido")
    public void testValidarSeveridadInvalida() {
        NuevaReglaDTO dto = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "CRITICA", 1);
        assertEquals("El nivel de severidad 'CRITICA' no es válido. Debe ser ALTA, MEDIA o BAJA.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto)).getMessage());
    }

    @Test
    @DisplayName("Rechazar ponderación fuera de rango 1 a 100")
    public void testValidarPonderacionInvalida() {
        NuevaReglaDTO dtoCero = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "ALTA", 0);
        assertEquals("La ponderación debe ser un número entero entre 1 y 100.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCero)).getMessage());

        NuevaReglaDTO dtoNegativo = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "ALTA", -5);
        assertEquals("La ponderación debe ser un número entero entre 1 y 100.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoNegativo)).getMessage());

        NuevaReglaDTO dtoExceso = crearDTOValido("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "ALTA", 101);
        assertEquals("La ponderación debe ser un número entero entre 1 y 100.",
                assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoExceso)).getMessage());
    }
}
