package com.gr4.owlaudit.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resultados_regla")
public class ResultadoRegla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cumple", nullable = false)
    private boolean cumple;

    @Column(name = "puntos_obtenidos", nullable = false)
    private int puntosObtenidos;

    // Sin cascade: la Regla ya existe y la auditoría no debe crearla ni borrarla
    @ManyToOne
    @JoinColumn(name = "regla_id", nullable = false)
    private Regla regla;

    @ManyToOne
    @JoinColumn(name = "auditoria_id", nullable = false)
    private Auditoria auditoria;

    @OneToOne(mappedBy = "resultadoRegla", cascade = CascadeType.ALL)
    private Hallazgo hallazgo;

    public ResultadoRegla() {
    }

    public ResultadoRegla(boolean cumple, int puntosObtenidos, Regla regla, Auditoria auditoria) {
        this.cumple = cumple;
        this.puntosObtenidos = puntosObtenidos;
        this.regla = regla;
        this.auditoria = auditoria;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isCumple() {
        return cumple;
    }

    public void setCumple(boolean cumple) {
        this.cumple = cumple;
    }

    public int getPuntosObtenidos() {
        return puntosObtenidos;
    }

    public void setPuntosObtenidos(int puntosObtenidos) {
        this.puntosObtenidos = puntosObtenidos;
    }

    public Regla getRegla() {
        return regla;
    }

    public void setRegla(Regla regla) {
        this.regla = regla;
    }

    public Auditoria getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Auditoria auditoria) {
        this.auditoria = auditoria;
    }

    public Hallazgo getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(Hallazgo hallazgo) {
        this.hallazgo = hallazgo;
    }
}
