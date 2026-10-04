package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.NivelSeveridadEnum;
import com.gr4.owlaudit.model.Regla;
import com.gr4.owlaudit.model.TipoMotorEnum;

public class ReglaServiceImpl implements ReglaService {

    private final ReglaDAO reglaDAO;

    public ReglaServiceImpl() {
        this(new ReglaDAOImpl());
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO) {
        this.reglaDAO = reglaDAO;
    }

    @Override
    public void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos de la regla no pueden ser nulos.");
        }

        // 1. Validar sintaxis y formato
        validarSintaxisYFormato(dto);

        // 2. Verificar unicidad por nombre
        if (reglaDAO.existePorNombre(dto.getNombreRepresentativo())) {
            throw new ExcepcionNegocio("Ya existe una regla de evaluación registrada con el nombre: " + dto.getNombreRepresentativo());
        }

        // 3. Instanciar entidad y persistir
        Regla regla = new Regla(dto);
        regla.setActiva(true);
        reglaDAO.guardar(regla);
    }

    private void validarSintaxisYFormato(NuevaReglaDTO dto) {
        // Validación de nulidad o vacío
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

        // Validación de tipoMotor
        String tipoMotorStr = dto.getTipoMotor().trim();
        TipoMotorEnum tipoMotor;
        try {
            tipoMotor = TipoMotorEnum.valueOf(tipoMotorStr);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionNegocio("El tipo de motor '" + tipoMotorStr + "' no es válido. Debe ser PRESENCIA_ARCHIVO, RESTRICCION_ARCHIVOS o ESTRUCTURA_CARPETAS.");
        }

        // Validación de nombreRepresentativo (1 a 100 caracteres)
        String nombre = dto.getNombreRepresentativo().trim();
        if (nombre.length() < 1 || nombre.length() > 100) {
            throw new ExcepcionNegocio("El nombre representativo debe tener entre 1 y 100 caracteres.");
        }

        // Validación de descripcionDetallada (1 a 200 caracteres)
        String desc = dto.getDescripcionDetallada().trim();
        if (desc.length() < 1 || desc.length() > 200) {
            throw new ExcepcionNegocio("La descripción detallada debe tener entre 1 y 200 caracteres.");
        }

        // Validación de parametroExacto
        String param = dto.getParametroExacto();
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
            boolean esArchivoExacto = !param.contains("*") && param.length() > 0;
            if (!esExtension && !esArchivoExacto) {
                throw new ExcepcionNegocio("Para RESTRICCION_ARCHIVOS, el parámetro debe ser una extensión (.ext, *.ext) o el nombre de un archivo prohibido exacto.");
            }
        }

        // Validación de nivelSeveridad
        String severidadStr = dto.getNivelSeveridad().trim();
        try {
            NivelSeveridadEnum.valueOf(severidadStr);
        } catch (IllegalArgumentException e) {
            throw new ExcepcionNegocio("El nivel de severidad '" + severidadStr + "' no es válido. Debe ser ALTA, MEDIA o BAJA.");
        }

        // Validación de ponderacion
        if (dto.getPonderacion() <= 0) {
            throw new ExcepcionNegocio("La ponderación debe ser un número entero estrictamente mayor a cero.");
        }
    }
}
