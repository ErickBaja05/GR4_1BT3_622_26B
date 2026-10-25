package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dao.AsignacionesDAO;
import com.gr4.owlaudit.dao.AsignacionesDAOImpl;
import com.gr4.owlaudit.dao.ReglaDAO;
import com.gr4.owlaudit.dao.ReglaDAOImpl;
import com.gr4.owlaudit.dto.NuevaAsignacionDTO;
import com.gr4.owlaudit.model.Asignacion;

/**
 * HU-02 · Implementación del servicio de asignaciones.
 * La lógica de crearAsignacion la desarrolla Erick con TDD.
 */
public class AsignacionesServiceImpl implements AsignacionesService {

    private final AsignacionesDAO asignacionesDAO;
    private final ReglaDAO reglaDAO;

    /** Constructor que usa el Servlet. */
    public AsignacionesServiceImpl() {
        this(new AsignacionesDAOImpl(), new ReglaDAOImpl());
    }

    /** Constructor para inyectar mocks en las pruebas. */
    public AsignacionesServiceImpl(AsignacionesDAO asignacionesDAO, ReglaDAO reglaDAO) {
        this.asignacionesDAO = asignacionesDAO;
        this.reglaDAO = reglaDAO;
    }

    @Override
    public Asignacion crearAsignacion(NuevaAsignacionDTO dto) {
        throw new UnsupportedOperationException("HU-02: pendiente de implementar (tarea de Erick)");
    }
}