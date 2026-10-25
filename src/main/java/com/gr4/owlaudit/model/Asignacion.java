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

    // Clave candidata en el MER: no nula y única (unique = true)
    @Column(name = "nombre", nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "umbral", nullable = false)
    private Double umbral;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "asignacion_regla", joinColumns = @JoinColumn(name = "asignacion_id"), inverseJoinColumns = @JoinColumn(name = "regla_id"))
    private List<Regla> reglas = new ArrayList<>();

    public Asignacion() {
        this.reglas = new ArrayList<>();
    }

    public Asignacion(String nombre, Double umbral, List<Regla> reglas) {
        this.nombre = nombre;
        this.umbral = umbral;
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

    public Double getUmbral() {
        return umbral;
    }

    public void setUmbral(Double umbral) {
        this.umbral = umbral;
    }

    public List<Regla> getReglas() {
        return reglas;
    }

    public void setReglas(List<Regla> reglas) {
        this.reglas = (reglas != null) ? new ArrayList<>(reglas) : new ArrayList<>();
    }
}
