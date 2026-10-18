package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorReglaConvencionCommitsTest {

    private final ValidadorRegla validadorRegla = new ValidadorRegla();

    @DisplayName("Dado un parámetro o ponderación con casos extremos inválidos cuando se valida la sintaxis y formato entonces lanza ExcepcionNegocio")
    @ParameterizedTest(name = "Dado parametro=''{0}'' y ponderacion={1} cuando se valida entonces lanza ExcepcionNegocio")
    @CsvSource({
            // 1. Solo números donde van prefijos
            "'12345: .+', 15",
            // 2. Letras y números mezclados en el prefijo
            "'^(feat123|fix4): .+', 15",
            "'1a2b: .+', 10",
            // 3. Dos letras o palabras fuera de la convención
            "'^(hola|mundo): .+', 15",
            "'xy: .+', 15",
            // 4. Sin los dos puntos ':' obligatorios
            "'^(feat|fix) .+', 15",
            // 5. Expresión regular rota (corchetes o paréntesis sin cerrar)
            "'^(feat|fix[: .+', 15",
            "'^(feat|fix(: .+', 15",
            // 6. Espacios al inicio/final o vacío
            "'   ', 15",
            "' ^(feat|fix): .+ ', 15",
            // 7. Ponderación extrema (0, negativa o mayor a 100)
            "'^(feat|fix|docs): .+', 0",
            "'^(feat|fix|docs): .+', -5",
            "'^(feat|fix|docs): .+', 150"
    })
    void given_invalid_extreme_parameters_or_weight_when_validate_syntax_and_format_then_throws_ExcepcionNegocio(
            String parametroExacto, int ponderacion) {

        // Given
        NuevaReglaDTO dto = new NuevaReglaDTO();
        dto.setTipoMotor("CONVENCION_COMMITS");
        dto.setNombreRepresentativo("Convención de Commits");
        dto.setDescripcionDetallada("Verifica que los mensajes sigan Conventional Commits");
        dto.setParametroExacto(parametroExacto);
        dto.setNivelSeveridad("MEDIA");
        dto.setPonderacion(ponderacion);

        // When & Then
        assertThrows(ExcepcionNegocio.class, () -> {
            validadorRegla.validarSintaxisYFormato(dto);
        });
    }

    @DisplayName("Dado un patrón de convención de commits y ponderación válidos cuando se valida la sintaxis y formato entonces no lanza excepción")
    @ParameterizedTest(name = "Dado parametro=''{0}'' y ponderacion={1} cuando se valida entonces no lanza excepción")
    @CsvSource({
            "'^(feat|fix|docs|style|refactor|test|chore)(\\(.+\\))?: .{1,72}$', 15",
            "'^(feat|fix|docs): .+', 20",
            "'^(feat|fix|refactor|test): .{5,50}$', 10"
    })
    void given_valid_commit_convention_pattern_and_weight_when_validate_syntax_and_format_then_does_not_throw_exception(
            String parametroExacto, int ponderacion) {

        // Given
        NuevaReglaDTO dto = new NuevaReglaDTO();
        dto.setTipoMotor("CONVENCION_COMMITS");
        dto.setNombreRepresentativo("Convención de Commits EPN");
        dto.setDescripcionDetallada("Al menos 80% de commits con prefijo válido");
        dto.setParametroExacto(parametroExacto);
        dto.setNivelSeveridad("ALTA");
        dto.setPonderacion(ponderacion);

        // When & Then
        assertDoesNotThrow(() -> {
            validadorRegla.validarSintaxisYFormato(dto);
        });
    }
}
