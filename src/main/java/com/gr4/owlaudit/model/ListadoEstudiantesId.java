package com.gr4.owlaudit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ListadoEstudiantesId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "id_curso", nullable = false)
    private Long idCurso;

    @Column(name = "id_estudiante", nullable = false)
    private Long idEstudiante;

    public ListadoEstudiantesId() {
    }

    public ListadoEstudiantesId(Long idCurso, Long idEstudiante) {
        this.idCurso = idCurso;
        this.idEstudiante = idEstudiante;
    }

    public Long getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Long idCurso) {
        this.idCurso = idCurso;
    }

    public Long getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Long idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListadoEstudiantesId that = (ListadoEstudiantesId) o;
        return Objects.equals(idCurso, that.idCurso) && Objects.equals(idEstudiante, that.idEstudiante);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCurso, idEstudiante);
    }
}
