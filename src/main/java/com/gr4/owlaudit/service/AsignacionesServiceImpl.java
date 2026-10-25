package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;
import com.gr4.owlaudit.service.AsignacionesService;

public class AsignacionesServiceImpl implements AsignacionesService {

    private final AsignacionesDAO asignacionesDAO;
    private final ReglaDAO reglaDAO;

    public AsignacionesServiceImpl(AsignacionesDAO asignacionesDAO, ReglaDAO reglaDAO) {
        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        // Retorno vacío en FASE RED: hará que todos los tests fallen
        return null;
    }
}
