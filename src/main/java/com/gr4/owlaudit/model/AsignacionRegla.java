package com.gr4.owlaudit.model;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entidad que representa la asociación entre una {@link Asignacion} y una {@link Regla}
 * con atributos propios (severidad y ponderación).
 */
@Entity
@Table(name = "asignacion_regla")
@IdClass(AsignacionReglaId.class)
public class AsignacionRegla implements Serializable {

    @Id
    @Column(name = "asignacion_id")
    private Long asignacionId;

    @Id
    @Column(name = "regla_id")
    private Long reglaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asignacion_id", insertable = false, updatable = false)
    private Asignacion asignacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regla_id", insertable = false, updatable = false)
    private Regla regla;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidad", nullable = false, length = 20)
    private NivelSeveridadEnum nivelSeveridad;

    @Column(name = "ponderacion", nullable = false)
    private int ponderacion;

    public AsignacionRegla() {}

    public AsignacionRegla(Long asignacionId, Long reglaId, NivelSeveridadEnum nivelSeveridad, int ponderacion) {
        this.asignacionId = asignacionId;
        this.reglaId = reglaId;
        this.nivelSeveridad = nivelSeveridad;
        this.ponderacion = ponderacion;
    }

    public Long getAsignacionId() { return asignacionId; }
    public void setAsignacionId(Long asignacionId) { this.asignacionId = asignacionId; }

    public Long getReglaId() { return reglaId; }
    public void setReglaId(Long reglaId) { this.reglaId = reglaId; }

    public Asignacion getAsignacion() { return asignacion; }
    public void setAsignacion(Asignacion asignacion) { this.asignacion = asignacion; }

    public Regla getRegla() { return regla; }
    public void setRegla(Regla regla) { this.regla = regla; }

    public NivelSeveridadEnum getNivelSeveridad() { return nivelSeveridad; }
    public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) { this.nivelSeveridad = nivelSeveridad; }

    public int getPonderacion() { return ponderacion; }
    public void setPonderacion(int ponderacion) { this.ponderacion = ponderacion; }
}

