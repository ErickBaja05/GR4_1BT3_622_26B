package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.TipoMotorEnum;

/**
 * Validador encargado de la verificación de sintaxis y formato de reglas técnicas.
 * Extraído de ReglaServiceImpl para cumplir con el Principio de Responsabilidad Única (SRP).
 */
public class ValidadorRegla {

    /**
     * Valida la sintaxis y formato del DTO de una nueva regla.
     *
     * @param dto Datos de la regla a validar.
     * @throws ExcepcionNegocio Si alguna validación de sintaxis o formato falla.
     */
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

    /**
     * Método alternativo de conveniencia que delega en {@link #validarSintaxisYFormato(NuevaReglaDTO)}.
     *
     * @param dto Datos de la regla a validar.
     */
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
            throw new ExcepcionNegocio("El tipo de motor '" + trimmed + "' no es válido. Debe ser PRESENCIA_ARCHIVO, RESTRICCION_ARCHIVOS o ESTRUCTURA_CARPETAS.");
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
        if (param.contains("..")) {
            throw new ExcepcionNegocio("El parámetro exacto no puede contener secuencias de navegación '..'.");
        }
        if (param.contains("//")) {
            throw new ExcepcionNegocio("El parámetro exacto no puede contener barras dobles '//'.");
        }

        // Caracteres prohibidos: :, \, ", <, >, |, ?
        char[] prohibidos = {':', '\\', '"', '<', '>', '|', '?'};
        for (char c : prohibidos) {
            if (param.indexOf(c) >= 0) {
                throw new ExcepcionNegocio("El parámetro exacto contiene el carácter prohibido: '" + c + "'.");
            }
        }

        // Reglas específicas según tipoMotor
        if (tipoMotor == TipoMotorEnum.PRESENCIA_ARCHIVO) {
            if (param.startsWith("/") || param.endsWith("/")) {
                throw new ExcepcionNegocio("Para PRESENCIA_ARCHIVO, la ruta no puede empezar ni terminar con '/'.");
            }
            if (param.contains("*")) {
                throw new ExcepcionNegocio("Para PRESENCIA_ARCHIVO, el parámetro no puede contener el carácter comodín '*'.");
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
                throw new ExcepcionNegocio("Para ESTRUCTURA_CARPETAS, el parámetro no puede contener el carácter comodín '*'.");
            }
        } else if (tipoMotor == TipoMotorEnum.RESTRICCION_ARCHIVOS) {
            if (param.contains("/")) {
                throw new ExcepcionNegocio("Para RESTRICCION_ARCHIVOS, el parámetro no puede contener barras separadoras '/'.");
            }
            boolean esExtension = param.startsWith(".") || param.startsWith("*.");
            boolean esArchivoExacto = !param.contains("*") && !param.isEmpty();
            if (!esExtension && !esArchivoExacto) {
                throw new ExcepcionNegocio("Para RESTRICCION_ARCHIVOS, el parámetro debe ser una extensión (.ext, *.ext) o el nombre de un archivo prohibido exacto.");
            }
        }
    }

    private void validarNivelSeveridad(String severidadStr) {
        String trimmed = severidadStr.trim();
        try {
            NivelSeveridadEnum.valueOf(trimmed);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionNegocio("El nivel de severidad '" + trimmed + "' no es válido. Debe ser ALTA, MEDIA o BAJA.");
        }
    }

    private void validarPonderacion(int ponderacion) {
        if (ponderacion <= 0) {
            throw new ExcepcionNegocio("La ponderación debe ser un número entero estrictamente mayor a cero.");
        }
    }
}

