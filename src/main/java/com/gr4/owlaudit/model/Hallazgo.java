package com.gr4.owlaudit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "hallazgos")
public class Hallazgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidad", nullable = false, length = 20)
    private NivelSeveridadEnum nivelSeveridad;

    @Column(name = "evidencia", nullable = false, length = 500)
    private String evidencia;

    @Column(name = "recomendacion", nullable = false, length = 500)
    private String recomendacion;

    @OneToOne
    @JoinColumn(name = "resultado_regla_id", nullable = false, unique = true)
    private ResultadoRegla resultadoRegla;

    public Hallazgo() {
    }

    public Hallazgo(NivelSeveridadEnum nivelSeveridad, String evidencia, String recomendacion,
                    ResultadoRegla resultadoRegla) {
        this.nivelSeveridad = nivelSeveridad;
        this.evidencia = evidencia;
        this.recomendacion = recomendacion;
        this.resultadoRegla = resultadoRegla;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NivelSeveridadEnum getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getRecomendacion() {
        return recomendacion;
    }

    public void setRecomendacion(String recomendacion) {
        this.recomendacion = recomendacion;
    }

    public ResultadoRegla getResultadoRegla() {
        return resultadoRegla;
    }

    public void setResultadoRegla(ResultadoRegla resultadoRegla) {
        this.resultadoRegla = resultadoRegla;
    }
}
