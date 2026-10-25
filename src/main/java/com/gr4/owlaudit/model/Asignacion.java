package com.gr4.owlaudit.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "asignaciones")
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "umbral_aprobacion", nullable = false)
    private Double umbralAprobacion;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "asignacion_regla",
        joinColumns = @JoinColumn(name = "asignacion_id"),
        inverseJoinColumns = @JoinColumn(name = "regla_id")
    )
    private List<Regla> reglas = new ArrayList<>();

    public Asignacion() {
        this.reglas = new ArrayList<>();
    }

    public Asignacion(String nombre, Double umbralAprobacion, List<Regla> reglas) {
        this.nombre = nombre;
        this.umbralAprobacion = umbralAprobacion;
        this.reglas = (reglas != null) ? new ArrayList<>(reglas) : new ArrayList<>();
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

    public Double getUmbral() {
        return getUmbralAprobacion();
    }

    public void setUmbral(Double umbral) {
        setUmbralAprobacion(umbral);
    }

    public List<Regla> getReglas() {
        return reglas;
    }

    public void setReglas(List<Regla> reglas) {
        this.reglas = (reglas != null) ? new ArrayList<>(reglas) : new ArrayList<>();
    }

    public void agregarRegla(Regla regla) {
        if (regla != null && !this.reglas.contains(regla)) {
            this.reglas.add(regla);
        }
    }
}
