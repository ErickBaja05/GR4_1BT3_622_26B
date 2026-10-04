package com.gr4.owlaudit.reglas.strategy;

import com.gr4.owlaudit.model.TipoMotorEnum;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas del patrón Strategy de CU03 (sin base de datos ni internet). */
class EvaluadoresTest {

    // Estructura del caso de prueba CP-CU03-01 más algunas rutas anidadas
    private final List<String> rutas = List.of(
        ".gitignore", "README.md", ".env", "docs/", "docs/manual.pdf", "lib/", "lib/util.jar"
    );

    @Test
    void presenciaArchivoCumpleSiElArchivoExiste() {
        EvaluadorReglaStrategy evaluador = new EvaluadorPresenciaArchivo();
        assertTrue(evaluador.evaluar(".gitignore", rutas));
        assertTrue(evaluador.evaluar("docs/manual.pdf", rutas));
        System.out.println("PRESENCIA_ARCHIVO '.gitignore' -> cumple");
    }

    @Test
    void presenciaArchivoNoCumpleSiElArchivoNoExiste() {
        EvaluadorReglaStrategy evaluador = new EvaluadorPresenciaArchivo();
        assertFalse(evaluador.evaluar("LICENSE", rutas));
        assertFalse(evaluador.evaluar("manual.pdf", rutas)); // está en docs/, no en la raíz
        System.out.println("PRESENCIA_ARCHIVO 'LICENSE' -> no cumple");
    }

    @Test
    void estructuraCarpetasCumpleSiLaCarpetaExiste() {
        EvaluadorReglaStrategy evaluador = new EvaluadorEstructuraCarpetas();
        assertTrue(evaluador.evaluar("docs", rutas));
        assertTrue(evaluador.evaluar("docs/", rutas));
        System.out.println("ESTRUCTURA_CARPETAS 'docs' -> cumple");
    }

    @Test
    void estructuraCarpetasNoCumpleSiLaCarpetaNoExiste() {
        EvaluadorReglaStrategy evaluador = new EvaluadorEstructuraCarpetas();
        assertFalse(evaluador.evaluar("src", rutas));
        assertFalse(evaluador.evaluar("README.md", rutas)); // es archivo, no carpeta
        System.out.println("ESTRUCTURA_CARPETAS 'src' -> no cumple");
    }

    @Test
    void restriccionArchivosNoCumpleSiHayArchivosProhibidos() {
        EvaluadorReglaStrategy evaluador = new EvaluadorRestriccionArchivos();
        assertFalse(evaluador.evaluar(".env", rutas));
        assertFalse(evaluador.evaluar("*.jar", rutas));
        assertFalse(evaluador.evaluar("manual.pdf", rutas));
        System.out.println("RESTRICCION_ARCHIVOS '.env' y '*.jar' -> no cumple");
    }

    @Test
    void restriccionArchivosCumpleSiNoHayArchivosProhibidos() {
        EvaluadorReglaStrategy evaluador = new EvaluadorRestriccionArchivos();
        assertTrue(evaluador.evaluar(".class", rutas));
        assertTrue(evaluador.evaluar("*.exe", rutas));
        assertTrue(evaluador.evaluar("secreto.txt", rutas));
        assertTrue(evaluador.evaluar("docs", rutas)); // las carpetas se ignoran
        System.out.println("RESTRICCION_ARCHIVOS '.class' -> cumple");
    }

    @Test
    void fabricaDevuelveElEvaluadorDeCadaTipoDeMotor() {
        EvaluadorReglaFactory fabrica = new EvaluadorReglaFactory();
        assertInstanceOf(EvaluadorPresenciaArchivo.class, fabrica.obtenerEvaluador(TipoMotorEnum.PRESENCIA_ARCHIVO));
        assertInstanceOf(EvaluadorRestriccionArchivos.class, fabrica.obtenerEvaluador(TipoMotorEnum.RESTRICCION_ARCHIVOS));
        assertInstanceOf(EvaluadorEstructuraCarpetas.class, fabrica.obtenerEvaluador(TipoMotorEnum.ESTRUCTURA_CARPETAS));
        System.out.println("EvaluadorReglaFactory -> devuelve el evaluador correcto para los 3 motores");
    }
}
