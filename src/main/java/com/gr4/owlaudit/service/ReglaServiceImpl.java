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
        regla.setVigencia(EstadoVigencia.VIGENTE);
        reglaDAO.guardar(regla);
    }

    @Override
    public boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado) {
        // 1. Obtener la regla actual desde el DAO
        Regla reglaActual = reglaDAO.obtenerPorNombre(nombreRegla);

        // Validación defensiva: si la regla no existe o el estado enviado es nulo
        if (reglaActual == null || nuevoEstado == null) {
            return false;
        }

        // 2. Validar que el estado solicitado sea distinto al actual (Escenario 1.3)
        if (reglaActual.getVigencia() == nuevoEstado) {
            return false;
        }

        // 3. Actualizar el estado en el objeto de dominio y persistir invocando al DAO
        reglaActual.setVigencia(nuevoEstado);
        return reglaDAO.actualizarEstadoVigencia(nombreRegla, nuevoEstado);
    }

    @Override
    public void actualizarRegla(Long id, NuevaReglaDTO dto) {
        validarParametrosActualizacion(id, dto);
        validadorRegla.validarSintaxisYFormato(dto);

        Regla regla = obtenerReglaExistente(id);
        validarUnicidadNombreAlActualizar(regla, dto.getNombreRepresentativo().trim());

        // Patrón Martin Fowler: Move Method (actualizarDesdeDTO en entidad Regla)
        regla.actualizarDesdeDTO(dto);
        reglaDAO.actualizar(regla);
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private void validarParametrosActualizacion(Long id, NuevaReglaDTO dto) {
        if (id == null) {
            throw new ExcepcionNegocio("El identificador de la regla es obligatorio.");
        }
        if (dto == null) {
            throw new ExcepcionNegocio("Los datos de la regla no pueden ser nulos.");
        }
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private Regla obtenerReglaExistente(Long id) {
        Regla regla = reglaDAO.buscarPorId(id);
        if (regla == null) {
            throw new ExcepcionNegocio("No se encontró la regla con ID: " + id);
        }
        return regla;
    }

    /**
     * Refactorización Martin Fowler: Extract Method (Composing Methods).
     */
    private void validarUnicidadNombreAlActualizar(Regla regla, String nuevoNombre) {
        if (!regla.getNombreRepresentativo().equalsIgnoreCase(nuevoNombre) && reglaDAO.existePorNombre(nuevoNombre)) {
            throw new ExcepcionNegocio("Ya existe una regla de evaluación registrada con el nombre: " + nuevoNombre);
        }
    }

    @Override
    public void eliminarRegla(Long id) {
        if (id == null) {
            throw new ExcepcionNegocio("El ID de la regla es obligatorio para eliminar.");
        }
        Regla regla = reglaDAO.buscarPorId(id);
        if (regla == null) {
            throw new ExcepcionNegocio("No se encontró la regla a eliminar con ID: " + id);
        }
        reglaDAO.eliminar(id);
    }

    @Override
    public Regla obtenerReglaPorId(Long id) {
        if (id == null) {
            return null;
        }
        return reglaDAO.buscarPorId(id);
    }

}
