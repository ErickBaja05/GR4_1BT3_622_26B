package com.gr4.owlaudit.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auditorias")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "puntaje_obtenido", nullable = false)
    private int puntajeObtenido;

    @Column(name = "puntaje_maximo", nullable = false)
    private int puntajeMaximo;

    @Column(name = "porcentaje", nullable = false)
    private double porcentaje;

    // Composición: al guardar la Auditoria se guardan sus ResultadoRegla (y sus Hallazgo)
    @OneToMany(mappedBy = "auditoria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResultadoRegla> resultados;

    public Auditoria() {
        this.fechaHora = LocalDateTime.now();
        this.resultados = new ArrayList<>();
    }

    public void asignarPonderacionCompleta(Regla regla) {
        ResultadoRegla resultado = new ResultadoRegla(true, regla.getPonderacion(), regla, this);
        resultados.add(resultado);
    }

    public void registrarHallazgoDeIncumplimiento(Regla regla) {
        ResultadoRegla resultado = new ResultadoRegla(false, 0, regla, this);
        // TODO DECISION D3: evidencia y recomendación generadas según el tipo de motor
        Hallazgo hallazgo = new Hallazgo(
            regla.getNivelSeveridad(),
            construirEvidencia(regla),
            construirRecomendacion(regla),
            resultado
        );
        resultado.setHallazgo(hallazgo);
        resultados.add(resultado);
    }

    private String construirEvidencia(Regla regla) {
        String parametro = regla.getParametroExacto();
        switch (regla.getTipoMotor()) {
            case PRESENCIA_ARCHIVO:
                return "No se encontró el archivo '" + parametro + "' en el repositorio";
            case ESTRUCTURA_CARPETAS:
                return "No se encontró la carpeta '" + parametro + "' en el repositorio";
            case RESTRICCION_ARCHIVOS:
                return "El repositorio contiene archivos prohibidos: '" + parametro + "'";
            default:
                return "La regla '" + regla.getNombreRepresentativo() + "' no se cumple";
        }
    }

    private String construirRecomendacion(Regla regla) {
        String parametro = regla.getParametroExacto();
        switch (regla.getTipoMotor()) {
            case PRESENCIA_ARCHIVO:
                return "Agregue el archivo '" + parametro + "' al repositorio";
            case ESTRUCTURA_CARPETAS:
                return "Cree la carpeta '" + parametro + "' en el repositorio";
            case RESTRICCION_ARCHIVOS:
                return "Elimine del repositorio los archivos '" + parametro + "' y agréguelos al .gitignore";
            default:
                return "Corrija el repositorio para cumplir la regla '" + regla.getNombreRepresentativo() + "'";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public int getPuntajeObtenido() {
        return puntajeObtenido;
    }

    public void setPuntajeObtenido(int puntajeObtenido) {
        this.puntajeObtenido = puntajeObtenido;
    }

    public int getPuntajeMaximo() {
        return puntajeMaximo;
    }

    public void setPuntajeMaximo(int puntajeMaximo) {
        this.puntajeMaximo = puntajeMaximo;
    }

    public double getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(double porcentaje) {
        this.porcentaje = porcentaje;
    }

    public List<ResultadoRegla> getResultados() {
        return resultados;
    }

    public void setResultados(List<ResultadoRegla> resultados) {
        this.resultados = resultados;
    }

    public Proyecto getProyecto() {
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto) {
        this.proyecto = proyecto;
    }
}
