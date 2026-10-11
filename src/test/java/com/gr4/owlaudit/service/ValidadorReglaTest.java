package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidadorReglaTest {

    private ValidadorRegla validador;

    @BeforeEach
    public void setUp() {
        validador = new ValidadorRegla();
    }

    @Test
    public void testValidarReglaPresenciaArchivoExitosa() {
        NuevaReglaDTO dto = new NuevaReglaDTO(
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
    public void testValidarReglaEstructuraCarpetasExitosa() {
        NuevaReglaDTO dto = new NuevaReglaDTO(
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
    public void testValidarReglaRestriccionArchivosExtensionExitosa() {
        NuevaReglaDTO dto = new NuevaReglaDTO(
            "RESTRICCION_ARCHIVOS",
            "Prohibir .env",
            "No debe subirse archivos de configuración",
            ".env",
            "ALTA",
            4
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dto));

        NuevaReglaDTO dtoAsterisco = new NuevaReglaDTO(
            "RESTRICCION_ARCHIVOS",
            "Prohibir binarios exe",
            "No permitir ejecutables",
            "*.exe",
            "ALTA",
            4
        );
        assertDoesNotThrow(() -> validador.validarSintaxisYFormato(dtoAsterisco));

        NuevaReglaDTO dtoExacto = new NuevaReglaDTO(
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
    public void testValidarDTONulo() {
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(null));
        assertEquals("Los datos de la regla no pueden ser nulos.", ex.getMessage());
    }

    @Test
    public void testValidarCamposObligatoriosNulosOVacios() {
        // Tipo motor obligatorio
        NuevaReglaDTO dto1 = new NuevaReglaDTO(null, "Nombre", "Desc", "param", "ALTA", 1);
        assertEquals("El tipo de motor es obligatorio.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto1)).getMessage());

        NuevaReglaDTO dto1Vacio = new NuevaReglaDTO("  ", "Nombre", "Desc", "param", "ALTA", 1);
        assertEquals("El tipo de motor es obligatorio.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto1Vacio)).getMessage());

        // Nombre representativo obligatorio
        NuevaReglaDTO dto2 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", null, "Desc", "param", "ALTA", 1);
        assertEquals("El nombre representativo es obligatorio.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto2)).getMessage());

        // Descripción obligatoria
        NuevaReglaDTO dto3 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "", "param", "ALTA", 1);
        assertEquals("La descripción detallada es obligatoria.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto3)).getMessage());

        // Parametro exacto obligatorio
        NuevaReglaDTO dto4 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", null, "ALTA", 1);
        assertEquals("El parámetro exacto es obligatorio.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto4)).getMessage());

        // Nivel severidad obligatorio
        NuevaReglaDTO dto5 = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "param", " ", 1);
        assertEquals("El nivel de severidad es obligatorio.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto5)).getMessage());
    }

    @Test
    public void testValidarTipoMotorInvalido() {
        NuevaReglaDTO dto = new NuevaReglaDTO("TIPO_INVENTADO", "Nombre", "Desc", "param", "ALTA", 1);
        ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto));
        assertEquals("El tipo de motor 'TIPO_INVENTADO' no es válido. Debe ser PRESENCIA_ARCHIVO, RESTRICCION_ARCHIVOS o ESTRUCTURA_CARPETAS.", ex.getMessage());
    }

    @Test
    public void testValidarNombreLongitudInvalida() {
        NuevaReglaDTO dtoLargo = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "A".repeat(101), "Desc", "param", "ALTA", 1);
        assertEquals("El nombre representativo debe tener entre 1 y 100 caracteres.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoLargo)).getMessage());
    }

    @Test
    public void testValidarDescripcionLongitudInvalida() {
        NuevaReglaDTO dtoLarga = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "D".repeat(201), "param", "ALTA", 1);
        assertEquals("La descripción detallada debe tener entre 1 y 200 caracteres.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoLarga)).getMessage());
    }

    @Test
    public void testValidarParametroEspaciosExtremos() {
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", " README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede tener espacios en blanco al inicio o al final.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto)).getMessage());
    }

    @Test
    public void testValidarParametroSecuenciasProhibidas() {
        NuevaReglaDTO dtoNavegacion = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "../README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede contener secuencias de navegación '..'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoNavegacion)).getMessage());

        NuevaReglaDTO dtoDobleBarra = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "src//README.md", "ALTA", 1);
        assertEquals("El parámetro exacto no puede contener barras dobles '//'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoDobleBarra)).getMessage());
    }

    @Test
    public void testValidarParametroCaracteresProhibidos() {
        char[] prohibidos = {':', '\\', '"', '<', '>', '|', '?'};
        for (char c : prohibidos) {
            NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "test" + c + "file", "ALTA", 1);
            ExcepcionNegocio ex = assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto));
            assertEquals("El parámetro exacto contiene el carácter prohibido: '" + c + "'.", ex.getMessage());
        }
    }

    @Test
    public void testValidarReglasEspecificasPorTipoMotor() {
        // PRESENCIA_ARCHIVO
        NuevaReglaDTO dtoPresenciaSlashInicio = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "/README.md", "ALTA", 1);
        assertEquals("Para PRESENCIA_ARCHIVO, la ruta no puede empezar ni terminar con '/'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoPresenciaSlashInicio)).getMessage());

        NuevaReglaDTO dtoPresenciaComodin = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "*.md", "ALTA", 1);
        assertEquals("Para PRESENCIA_ARCHIVO, el parámetro no puede contener el carácter comodín '*'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoPresenciaComodin)).getMessage());

        // ESTRUCTURA_CARPETAS
        NuevaReglaDTO dtoCarpetasSlashFin = new NuevaReglaDTO("ESTRUCTURA_CARPETAS", "Nombre", "Desc", "src/", "ALTA", 1);
        assertEquals("Para ESTRUCTURA_CARPETAS, la ruta no puede empezar ni terminar con '/'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCarpetasSlashFin)).getMessage());

        NuevaReglaDTO dtoCarpetasComodin = new NuevaReglaDTO("ESTRUCTURA_CARPETAS", "Nombre", "Desc", "src/*", "ALTA", 1);
        assertEquals("Para ESTRUCTURA_CARPETAS, el parámetro no puede contener el carácter comodín '*'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCarpetasComodin)).getMessage());

        // RESTRICCION_ARCHIVOS
        NuevaReglaDTO dtoRestriccionSlash = new NuevaReglaDTO("RESTRICCION_ARCHIVOS", "Nombre", "Desc", "src/secret.key", "ALTA", 1);
        assertEquals("Para RESTRICCION_ARCHIVOS, el parámetro no puede contener barras separadoras '/'.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoRestriccionSlash)).getMessage());

        NuevaReglaDTO dtoRestriccionComodinInvalido = new NuevaReglaDTO("RESTRICCION_ARCHIVOS", "Nombre", "Desc", "abc*def", "ALTA", 1);
        assertEquals("Para RESTRICCION_ARCHIVOS, el parámetro debe ser una extensión (.ext, *.ext) o el nombre de un archivo prohibido exacto.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoRestriccionComodinInvalido)).getMessage());
    }

    @Test
    public void testValidarSeveridadInvalida() {
        NuevaReglaDTO dto = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "CRITICA", 1);
        assertEquals("El nivel de severidad 'CRITICA' no es válido. Debe ser ALTA, MEDIA o BAJA.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dto)).getMessage());
    }

    @Test
    public void testValidarPonderacionInvalida() {
        NuevaReglaDTO dtoCero = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "ALTA", 0);
        assertEquals("La ponderación debe ser un número entero estrictamente mayor a cero.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoCero)).getMessage());

        NuevaReglaDTO dtoNegativo = new NuevaReglaDTO("PRESENCIA_ARCHIVO", "Nombre", "Desc", "README.md", "ALTA", -5);
        assertEquals("La ponderación debe ser un número entero estrictamente mayor a cero.",
            assertThrows(ExcepcionNegocio.class, () -> validador.validarSintaxisYFormato(dtoNegativo)).getMessage());
    }

    public static void main(String[] args) {
        ValidadorReglaTest t = new ValidadorReglaTest();
        t.setUp();
        t.testValidarReglaPresenciaArchivoExitosa();
        t.testValidarReglaEstructuraCarpetasExitosa();
        t.testValidarReglaRestriccionArchivosExtensionExitosa();
        t.testValidarDTONulo();
        t.testValidarCamposObligatoriosNulosOVacios();
        t.testValidarTipoMotorInvalido();
        t.testValidarNombreLongitudInvalida();
        t.testValidarDescripcionLongitudInvalida();
        t.testValidarParametroEspaciosExtremos();
        t.testValidarParametroSecuenciasProhibidas();
        t.testValidarParametroCaracteresProhibidos();
        t.testValidarReglasEspecificasPorTipoMotor();
        t.testValidarSeveridadInvalida();
        t.testValidarPonderacionInvalida();
        System.out.println("Todas las pruebas de ValidadorReglaTest pasaron correctamente.");
    }
}

