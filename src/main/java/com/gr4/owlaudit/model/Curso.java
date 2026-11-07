package com.gr4.owlaudit.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cursos")
public class Curso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_docente", nullable = false)
    private Docente docente;

    @Column(name = "codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "periodo_academico", nullable = false, length = 20)
    private String periodoAcademico;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ListadoEstudiantes> listadoEstudiantes = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Regla> reglas = new ArrayList<>();

    @OneToMany(mappedBy = "curso", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Asignacion> asignaciones = new ArrayList<>();

    public Curso() {
    }

    public Curso(Docente docente, String codigo, String nombre, String periodoAcademico, String descripcion) {
        this.docente = docente;
        this.codigo = codigo;
        this.nombre = nombre;
        this.periodoAcademico = periodoAcademico;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPeriodoAcademico() {
        return periodoAcademico;
    }

    public void setPeriodoAcademico(String periodoAcademico) {
        this.periodoAcademico = periodoAcademico;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<ListadoEstudiantes> getListadoEstudiantes() {
        return listadoEstudiantes;
    }

    public void setListadoEstudiantes(List<ListadoEstudiantes> listadoEstudiantes) {
        this.listadoEstudiantes = listadoEstudiantes != null ? new ArrayList<>(listadoEstudiantes) : new ArrayList<>();
    }

    public List<Regla> getReglas() {
        return reglas;
    }

    public void setReglas(List<Regla> reglas) {
        this.reglas = reglas != null ? new ArrayList<>(reglas) : new ArrayList<>();
    }

    public List<Asignacion> getAsignaciones() {
        return asignaciones;
    }

    public void setAsignaciones(List<Asignacion> asignaciones) {
        this.asignaciones = asignaciones != null ? new ArrayList<>(asignaciones) : new ArrayList<>();
    }

    public boolean esDocenteTitular(Long docenteId) {
        return docenteId != null && this.docente != null && docenteId.equals(this.docente.getId());
    }
}
