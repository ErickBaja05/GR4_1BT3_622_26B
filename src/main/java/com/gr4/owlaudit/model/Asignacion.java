package com.gr4.owlaudit.model;

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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "asignaciones",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_curso", "nombre"})
)
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "umbral_aprobacion", nullable = false)
    private Double umbralAprobacion;

    @Column(name = "id_secuencial")
    private Long idSecuencial;

    @Column(name = "numero_intentos")
    private Integer numeroIntentos;

    @Column(name = "intentos_permitidos")
    private Integer intentosPermitidos;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_limite")
    private LocalDateTime fechaLimite;

    @OneToMany(mappedBy = "asignacion", fetch = FetchType.LAZY)
    private List<AsignacionRegla> asignacionesReglas = new ArrayList<>();

    @Transient
    private List<Regla> reglas = new ArrayList<>();

    @OneToMany(mappedBy = "asignacion", fetch = FetchType.LAZY)
    private List<Auditoria> auditorias = new ArrayList<>();

    public Asignacion() {
        this.asignacionesReglas = new ArrayList<>();
        this.reglas = new ArrayList<>();
        this.auditorias = new ArrayList<>();
    }

    public Asignacion(String nombre, Double umbralAprobacion, List<Regla> reglas) {
        this.nombre = nombre;
        this.umbralAprobacion = umbralAprobacion;
        this.reglas = (reglas != null) ? new ArrayList<>(reglas) : new ArrayList<>();
        this.asignacionesReglas = new ArrayList<>();
        this.auditorias = new ArrayList<>();
    }

    public Asignacion(Curso curso, String nombre, Double umbralAprobacion, List<Regla> reglas) {
        this(nombre, umbralAprobacion, reglas);
        this.curso = curso;
    }

    public Asignacion(Curso curso, String nombre, LocalDateTime fechaLimite, Integer intentosPermitidos, Double umbralAprobacion) {
        this.curso = curso;
        this.nombre = nombre;
        this.fechaLimite = fechaLimite;
        this.intentosPermitidos = intentosPermitidos;
        this.numeroIntentos = intentosPermitidos;
        this.umbralAprobacion = umbralAprobacion;
        this.asignacionesReglas = new ArrayList<>();
        this.reglas = new ArrayList<>();
        this.auditorias = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Double getUmbralAprobacion() {
        return umbralAprobacion;
    }

    public void setUmbralAprobacion(Double umbralAprobacion) {
        this.umbralAprobacion = umbralAprobacion;
    }

    public Long getIdSecuencial() {
        return idSecuencial;
    }

    public void setIdSecuencial(Long idSecuencial) {
        this.idSecuencial = idSecuencial;
    }

    public Long getIdVisible() {
        return idSecuencial != null ? idSecuencial : id;
    }

    public List<AsignacionRegla> getAsignacionesReglas() {
        return asignacionesReglas;
    }

    public void setAsignacionesReglas(List<AsignacionRegla> asignacionesReglas) {
        this.asignacionesReglas = (asignacionesReglas != null) ? asignacionesReglas : new ArrayList<>();
    }

    public List<Regla> getReglas() {
        if (this.asignacionesReglas != null && !this.asignacionesReglas.isEmpty()) {
            List<Regla> list = new ArrayList<>();
            for (AsignacionRegla ar : this.asignacionesReglas) {
                if (ar != null && ar.getRegla() != null) {
                    Regla r = ar.getRegla();
                    r.setPonderacion(ar.getPonderacion());
                    r.setNivelSeveridad(ar.getNivelSeveridad());
                    list.add(r);
                }
            }
            return list;
        }
        return (this.reglas != null) ? this.reglas : new ArrayList<>();
    }

    public void setReglas(List<Regla> reglas) {
        this.reglas = (reglas != null) ? new ArrayList<>(reglas) : new ArrayList<>();
    }

    public void agregarRegla(Regla regla) {
        if (regla != null && !this.reglas.contains(regla)) {
            this.reglas.add(regla);
        }
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getNumeroIntentos() {
        return numeroIntentos;
    }

    public void setNumeroIntentos(Integer numeroIntentos) {
        this.numeroIntentos = numeroIntentos;
    }

    public Integer getIntentosPermitidos() {
        return intentosPermitidos != null ? intentosPermitidos : numeroIntentos;
    }

    public void setIntentosPermitidos(Integer intentosPermitidos) {
        this.intentosPermitidos = intentosPermitidos;
        this.numeroIntentos = intentosPermitidos;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaLimite() {
        return fechaLimite;
    }

    public void setFechaLimite(LocalDateTime fechaLimite) {
        this.fechaLimite = fechaLimite;
    }

    public List<Auditoria> getAuditorias() {
        return auditorias;
    }

    public void setAuditorias(List<Auditoria> auditorias) {
        this.auditorias = (auditorias != null) ? new ArrayList<>(auditorias) : new ArrayList<>();
    }
}
