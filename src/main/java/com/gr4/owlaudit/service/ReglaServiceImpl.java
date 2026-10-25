package com.gr4.owlaudit.service;

import com.gr4.owlaudit.common.exception.ExcepcionNegocio;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;

public class ReglaServiceImpl implements ReglaService {

    private final ReglaDAO reglaDAO;
    private final ValidadorRegla validadorRegla;

    public ReglaServiceImpl() {
        this(new ReglaDAOImpl(), new ValidadorRegla());
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO) {
        this(reglaDAO, new ValidadorRegla());
    }

    public ReglaServiceImpl(ValidadorRegla validadorRegla) {
        this(new ReglaDAOImpl(), validadorRegla);
    }

    public ReglaServiceImpl(ReglaDAO reglaDAO, ValidadorRegla validadorRegla) {
        this.reglaDAO = reglaDAO;
        this.validadorRegla = validadorRegla;
    }

    @Override
    public void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto) {
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos de la regla no pueden ser nulos.");
        }

        // 1. Validar sintaxis y formato
        validadorRegla.validarSintaxisYFormato(dto);

        // 2. Verificar unicidad por nombre
        if (reglaDAO.existePorNombre(dto.getNombreRepresentativo())) {
            throw new ExcepcionNegocio(
                    "Ya existe una regla de evaluación registrada con el nombre: " + dto.getNombreRepresentativo());
        }

        // 3. Instanciar entidad y persistir
        Regla regla = new Regla(dto);
        regla.setActiva(true);
        reglaDAO.guardar(regla);
    }

    @Override
    public boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado) {
        return false; // Aún no hay lógica programada
    }

}
