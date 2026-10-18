package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.TipoMotorEnum;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ValidadorRegla {

    public void validarSintaxisYFormato(NuevaReglaDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos de la regla no pueden ser nulos.");
        }

        validarCamposObligatorios(dto);

        TipoMotorEnum tipoMotor = validarTipoMotor(dto.getTipoMotor());
        validarNombreRepresentativo(dto.getNombreRepresentativo());
        validarDescripcionDetallada(dto.getDescripcionDetallada());
        validarParametroExacto(dto.getParametroExacto(), tipoMotor);
        validarNivelSeveridad(dto.getNivelSeveridad());
        validarPonderacion(dto.getPonderacion());
    }

    public void validar(NuevaReglaDTO dto) {
        validarSintaxisYFormato(dto);
    }

    private void validarCamposObligatorios(NuevaReglaDTO dto) {
        if (dto.getTipoMotor() == null || dto.getTipoMotor().trim().isEmpty()) {
            throw new ExcepcionNegocio("El tipo de motor es obligatorio.");
        }
        if (dto.getNombreRepresentativo() == null || dto.getNombreRepresentativo().trim().isEmpty()) {
            throw new ExcepcionNegocio("El nombre representativo es obligatorio.");
        }
        if (dto.getDescripcionDetallada() == null || dto.getDescripcionDetallada().trim().isEmpty()) {
            throw new ExcepcionNegocio("La descripción detallada es obligatoria.");
        }
        if (dto.getParametroExacto() == null || dto.getParametroExacto().isEmpty()) {
            throw new ExcepcionNegocio("El parámetro exacto es obligatorio.");
        }
        if (dto.getNivelSeveridad() == null || dto.getNivelSeveridad().trim().isEmpty()) {
            throw new ExcepcionNegocio("El nivel de severidad es obligatorio.");
        }
    }

    private TipoMotorEnum validarTipoMotor(String tipoMotorStr) {
        String trimmed = tipoMotorStr.trim();
        try {
            return TipoMotorEnum.valueOf(trimmed);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionNegocio("El tipo de motor '" + trimmed
                    + "' no es válido. Debe ser PRESENCIA_ARCHIVO, RESTRICCION_ARCHIVOS, ESTRUCTURA_CARPETAS o CONVENCION_COMMITS.");
        }
    }

    private void validarNombreRepresentativo(String nombre) {
        String trimmed = nombre.trim();
        if (trimmed.length() < 1 || trimmed.length() > 100) {
            throw new ExcepcionNegocio("El nombre representativo debe tener entre 1 y 100 caracteres.");
        }
    }

    private void validarDescripcionDetallada(String desc) {
        String trimmed = desc.trim();
        if (trimmed.length() < 1 || trimmed.length() > 200) {
            throw new ExcepcionNegocio("La descripción detallada debe tener entre 1 y 200 caracteres.");
        }
    }

    private void validarParametroExacto(String param, TipoMotorEnum tipoMotor) {
        if (param.startsWith(" ") || param.endsWith(" ")) {
            throw new ExcepcionNegocio("El parámetro exacto no puede tener espacios en blanco al inicio o al final.");
        }
        if (param.length() < 1 || param.length() > 100) {
            throw new ExcepcionNegocio("El parámetro exacto debe tener entre 1 y 100 caracteres.");
        }

        // Si es CONVENCION_COMMITS, validamos sintaxis de expresión regular y prefijos
        // de commits
        if (tipoMotor == TipoMotorEnum.CONVENCION_COMMITS) {
            if (!param.contains(":")) {
                throw new ExcepcionNegocio("Para CONVENCION_COMMITS, el patrón debe incluir el separador ':'.");
            }
            String prefijosParte = param.substring(0, param.indexOf(":"));
            if (prefijosParte.matches(".*\\d+.*")) {
                throw new ExcepcionNegocio("Los prefijos de commit no pueden contener números.");
            }
            List<String> prefijosValidos = List.of("feat", "fix", "docs", "style", "refactor", "test", "chore");
            boolean tienePrefijo = prefijosValidos.stream()
                    .anyMatch(p -> Pattern.compile("\\b" + p + "\\b").matcher(prefijosParte).find());
            if (!tienePrefijo) {
                throw new ExcepcionNegocio(
                        "El patrón debe incluir al menos un prefijo válido (feat, fix, docs, style, refactor, test, chore).");
            }
            try {
                Pattern.compile(param);
            } catch (PatternSyntaxException e) {
                throw new ExcepcionNegocio("El parámetro exacto no es una expresión regular válida.");
            }
            return;
        }

        // Validaciones para motores basados en rutas/archivos
        if (param.contains("..")) {
            throw new ExcepcionNegocio("El parámetro exacto no puede contener secuencias de navegación '..'.");
        }
        if (param.contains("//")) {
            throw new ExcepcionNegocio("El parámetro exacto no puede contener barras dobles '//'.");
        }

        char[] prohibidos = { ':', '\\', '"', '<', '>', '|', '?' };
        for (char c : prohibidos) {
            if (param.indexOf(c) >= 0) {
                throw new ExcepcionNegocio("El parámetro exacto contiene el carácter prohibido: '" + c + "'.");
            }
        }

        if (tipoMotor == TipoMotorEnum.PRESENCIA_ARCHIVO) {
            if (param.startsWith("/") || param.endsWith("/")) {
                throw new ExcepcionNegocio("Para PRESENCIA_ARCHIVO, la ruta no puede empezar ni terminar con '/'.");
            }
            if (param.contains("*")) {
                throw new ExcepcionNegocio(
                        "Para PRESENCIA_ARCHIVO, el parámetro no puede contener el carácter comodín '*'.");
            }
            int lastSlash = param.lastIndexOf('/');
            String nombreArchivo = (lastSlash >= 0) ? param.substring(lastSlash + 1) : param;
            if (nombreArchivo.trim().isEmpty()) {
                throw new ExcepcionNegocio("Para PRESENCIA_ARCHIVO, debe especificar un nombre de archivo válido.");
            }
        } else if (tipoMotor == TipoMotorEnum.ESTRUCTURA_CARPETAS) {
            if (param.startsWith("/") || param.endsWith("/")) {
                throw new ExcepcionNegocio("Para ESTRUCTURA_CARPETAS, la ruta no puede empezar ni terminar con '/'.");
            }
            if (param.contains("*")) {
                throw new ExcepcionNegocio(
                        "Para ESTRUCTURA_CARPETAS, el parámetro no puede contener el carácter comodín '*'.");
            }
        } else if (tipoMotor == TipoMotorEnum.RESTRICCION_ARCHIVOS) {
            if (param.contains("/")) {
                throw new ExcepcionNegocio(
                        "Para RESTRICCION_ARCHIVOS, el parámetro no puede contener barras separadoras '/'.");
            }
            boolean esExtension = param.startsWith(".") || param.startsWith("*.");
            boolean esArchivoExacto = !param.contains("*") && !param.isEmpty();
            if (!esExtension && !esArchivoExacto) {
                throw new ExcepcionNegocio(
                        "Para RESTRICCION_ARCHIVOS, el parámetro debe ser una extensión (.ext, *.ext) o el nombre de un archivo prohibido exacto.");
            }
        }
    }

    private void validarNivelSeveridad(String severidadStr) {
        String trimmed = severidadStr.trim();
        try {
            NivelSeveridadEnum.valueOf(trimmed);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionNegocio(
                    "El nivel de severidad '" + trimmed + "' no es válido. Debe ser ALTA, MEDIA o BAJA.");
        }
    }

    private void validarPonderacion(int ponderacion) {
        if (ponderacion <= 0 || ponderacion > 100) {
            throw new ExcepcionNegocio("La ponderación debe ser un número entero entre 1 y 100.");
        }
    }
}
