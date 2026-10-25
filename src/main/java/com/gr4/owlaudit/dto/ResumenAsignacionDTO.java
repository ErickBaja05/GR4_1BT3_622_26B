package com.gr4.owlaudit.dto;

import com.gr4.owlaudit.model.*;
import java.util.List;

public class ResumenAsignacionDTO {
    private Long idAsignacion;
    private String nombreAsignacion;
    private double umbralAprobacion;
    private List<Regla> reglasAsociadas;
    private List<Proyecto> proyectosEstudiante;

    public ResumenAsignacionDTO() {
    }

    public Long getIdAsignacion() {
        return idAsignacion;
    }

    public void setIdAsignacion(Long idAsignacion) {
        this.idAsignacion = idAsignacion;
    }

    public String getNombreAsignacion() {
        return nombreAsignacion;
    }

    public void setNombreAsignacion(String nombreAsignacion) {
        this.nombreAsignacion = nombreAsignacion;
    }

    public double getUmbralAprobacion() {
        return umbralAprobacion;
    }

    public void setUmbralAprobacion(double umbralAprobacion) {
        this.umbralAprobacion = umbralAprobacion;
    }

    public List<Regla> getReglasAsociadas() {
        return reglasAsociadas;
    }

    public void setReglasAsociadas(List<Regla> reglasAsociadas) {
        this.reglasAsociadas = reglasAsociadas;
    }

    public List<Proyecto> getProyectosEstudiante() {
        return proyectosEstudiante;
    }

    public void setProyectosEstudiante(List<Proyecto> proyectosEstudiante) {
        this.proyectosEstudiante = proyectosEstudiante;
    }

}
