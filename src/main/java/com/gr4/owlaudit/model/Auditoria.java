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
import java.util.Objects;

@Entity
@Table(name = "auditorias")
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "asignacion_id", nullable = true)
    private Asignacion asignacion;

    @Column(name = "dictamen", nullable = true)
    private String dictamen;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "puntaje_obtenido", nullable = false)
    private int puntajeObtenido;

    @Column(name = "puntaje_maximo", nullable = false)
    private int puntajeMaximo;

    @Column(name = "porcentaje", nullable = false)
    private double porcentaje;

    @Column(name = "tipo_auditoria", length = 30)
    private String tipoAuditoria;

    @Column(name = "numero_en_proyecto")
    private Long numeroEnProyecto;

    @Column(name = "numero_en_asignacion")
    private Long numeroEnAsignacion;

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Asignacion getAsignacion() {
        return asignacion;
    }

    public void setAsignacion(Asignacion asignacion) {
        this.asignacion = asignacion;
    }

    public String getDictamen() {
        return dictamen;
    }

    public void setDictamen(String dictamen) {
        this.dictamen = dictamen;
    }

    public String getTipoAuditoria() {
        if (tipoAuditoria != null && !tipoAuditoria.isBlank()) {
            return tipoAuditoria;
        }
        return asignacion != null ? "ASIGNACION" : "GENERAL";
    }

    public void setTipoAuditoria(String tipoAuditoria) {
        this.tipoAuditoria = tipoAuditoria;
    }

    public Long getNumeroEnProyecto() {
        return numeroEnProyecto;
    }

    public void setNumeroEnProyecto(Long numeroEnProyecto) {
        this.numeroEnProyecto = numeroEnProyecto;
    }

    public Long getNumeroEnAsignacion() {
        return numeroEnAsignacion;
    }

    public void setNumeroEnAsignacion(Long numeroEnAsignacion) {
        this.numeroEnAsignacion = numeroEnAsignacion;
    }

    public boolean esAuditoriaGeneral() {
        return "GENERAL".equalsIgnoreCase(getTipoAuditoria()) || asignacion == null;
    }

    public boolean esAuditoriaAsignacion() {
        return "ASIGNACION".equalsIgnoreCase(getTipoAuditoria()) || asignacion != null;
    }

    public Long getIdVisible() {
        if (esAuditoriaAsignacion()) {
            return numeroEnAsignacion != null ? numeroEnAsignacion : (numeroEnProyecto != null ? numeroEnProyecto : id);
        }
        return numeroEnProyecto != null ? numeroEnProyecto : id;
    }

    public String getCodigoVisible() {
        Long vis = getIdVisible();
        if (esAuditoriaAsignacion()) {
            return "AA-" + (vis != null ? vis : (id != null ? id : "?"));
        }
        return "AG-" + (vis != null ? vis : (id != null ? id : "?"));
    }

    public String getEtiquetaVisible() {
        Long vis = getIdVisible();
        if (esAuditoriaAsignacion()) {
            String nom = (asignacion != null && asignacion.getNombre() != null) ? " (" + asignacion.getNombre() + ")" : "";
            return "Auditoría Asignación #" + (vis != null ? vis : "?") + nom;
        }
        return "Auditoría General #" + (vis != null ? vis : "?");
    }

    public static final double UMBRAL_MINIMO_APROBACION = 70.0;
    public static final String DICTAMEN_APROBADA = "APROBADA";
    public static final String DICTAMEN_NO_APROBADA = "NO APROBADA";

    public String determinarDictamen() {
        if (this.porcentaje < UMBRAL_MINIMO_APROBACION || tieneHallazgosDeSeveridadAlta()) {
            return DICTAMEN_NO_APROBADA;
        }
        return DICTAMEN_APROBADA;
    }

    /**
     * Refactorización Martin Fowler: Move Method (Moving Features Between Objects).
     * Determina el dictamen de aprobación de la auditoría en función de un umbral específico de asignación.
     */
    public String determinarDictamenPorUmbral(double umbral) {
        return this.porcentaje >= umbral ? DICTAMEN_APROBADA : DICTAMEN_NO_APROBADA;
    }

    private boolean tieneHallazgosDeSeveridadAlta() {
        if (this.resultados == null) {
            return false;
        }
        return this.resultados.stream()
                .map(ResultadoRegla::getHallazgo)
                .filter(Objects::nonNull)
                .anyMatch(hallazgo -> hallazgo.getNivelSeveridad() == NivelSeveridadEnum.ALTA);
    }
}
