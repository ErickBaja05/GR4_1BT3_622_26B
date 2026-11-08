package com.gr4.owlaudit.service;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import com.gr4.owlaudit.model.EstadoVigencia;
import com.gr4.owlaudit.model.Regla;
import java.util.List;

public interface ReglaService {
    void registrarReglaEvaluacionTecnica(NuevaReglaDTO dto);

    public boolean cambiarEstadoVigenciaDeUnaRegla(String nombreRegla, EstadoVigencia nuevoEstado);

    boolean cambiarEstadoVigenciaDeUnaRegla(Long idRegla, Long idCurso, EstadoVigencia nuevoEstado);

    void modificarDescripcionYVigencia(Long idRegla, Long idCurso, Long idDocente, String descripcion, EstadoVigencia vigencia);

    List<Regla> listarReglasPorCurso(Long idCurso);

    List<Regla> listarReglasVigentesPorCurso(Long idCurso);
}
