package com.gr4.owlaudit.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "listado_estudiantes")
public class ListadoEstudiantes {

    @EmbeddedId
    private ListadoEstudiantesId id = new ListadoEstudiantesId();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idCurso")
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("idEstudiante")
    @JoinColumn(name = "id_estudiante", nullable = false)
    private Estudiante estudiante;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDateTime fechaMatricula;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    public ListadoEstudiantes() {
        this.fechaMatricula = LocalDateTime.now();
        this.estado = "ACTIVO";
    }

    public ListadoEstudiantes(Curso curso, Estudiante estudiante, LocalDateTime fechaMatricula, String estado) {
        this.curso = curso;
        this.estudiante = estudiante;
        if (curso != null && curso.getId() != null) {
            this.id.setIdCurso(curso.getId());
        }
        if (estudiante != null && estudiante.getId() != null) {
            this.id.setIdEstudiante(estudiante.getId());
        }
        this.fechaMatricula = fechaMatricula != null ? fechaMatricula : LocalDateTime.now();
        this.estado = estado != null ? estado : "ACTIVO";
    }

    public ListadoEstudiantesId getId() {
        return id;
    }

    public void setId(ListadoEstudiantesId id) {
        this.id = id != null ? id : new ListadoEstudiantesId();
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
        if (curso != null && curso.getId() != null) {
            this.id.setIdCurso(curso.getId());
        }
    }

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
        if (estudiante != null && estudiante.getId() != null) {
            this.id.setIdEstudiante(estudiante.getId());
        }
    }

    public LocalDateTime getFechaMatricula() {
        return fechaMatricula;
    }

    public void setFechaMatricula(LocalDateTime fechaMatricula) {
        this.fechaMatricula = fechaMatricula;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
