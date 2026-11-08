package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "reglas",
    uniqueConstraints = @UniqueConstraint(columnNames = {"id_curso", "nombre_representativo"})
)
public class Regla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_curso", nullable = false)
    private Curso curso;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_motor", nullable = false, length = 50)
    private TipoMotorEnum tipoMotor;

    @Column(name = "nombre_representativo", nullable = false, length = 100)
    private String nombreRepresentativo;

    @Column(name = "descripcion_detallada", length = 200)
    private String descripcionDetallada;

    @Column(name = "parametro_exacto", nullable = false, length = 100)
    private String parametroExacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidad", nullable = true, length = 20)
    private NivelSeveridadEnum nivelSeveridad;

    @Column(name = "ponderacion", nullable = true)
    private Integer ponderacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "vigencia", nullable = false, length = 20)
    private EstadoVigencia vigencia = EstadoVigencia.VIGENTE;

    @Column(name = "activa")
    private Boolean activa = true;

    @OneToMany(mappedBy = "regla", fetch = FetchType.LAZY)
    private List<AsignacionRegla> asignacionesReglas = new ArrayList<>();

    public Regla() {
        this.vigencia = EstadoVigencia.VIGENTE;
    }

    public Regla(NuevaReglaDTO dto) {
        if (dto != null) {
            if (dto.getTipoMotor() != null && !dto.getTipoMotor().trim().isEmpty()) {
                this.tipoMotor = TipoMotorEnum.valueOf(dto.getTipoMotor().trim().toUpperCase());
            }
            this.nombreRepresentativo = dto.getNombreRepresentativo();
            this.descripcionDetallada = dto.getDescripcionDetallada();
            this.parametroExacto = dto.getParametroExacto();
            this.vigencia = EstadoVigencia.VIGENTE;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoMotorEnum getTipoMotor() {
        return tipoMotor;
    }

    public void setTipoMotor(TipoMotorEnum tipoMotor) {
        this.tipoMotor = tipoMotor;
    }

    public String getNombreRepresentativo() {
        return nombreRepresentativo;
    }

    public void setNombreRepresentativo(String nombreRepresentativo) {
        this.nombreRepresentativo = nombreRepresentativo;
    }

    public String getDescripcionDetallada() {
        return descripcionDetallada;
    }

    public void setDescripcionDetallada(String descripcionDetallada) {
        this.descripcionDetallada = descripcionDetallada;
    }

    public String getParametroExacto() {
        return parametroExacto;
    }

    public void setParametroExacto(String parametroExacto) {
        this.parametroExacto = parametroExacto;
    }

    public NivelSeveridadEnum getNivelSeveridad() {
        return nivelSeveridad;
    }

    public void setNivelSeveridad(NivelSeveridadEnum nivelSeveridad) {
        this.nivelSeveridad = nivelSeveridad;
    }

    public int getPonderacion() {
        return ponderacion != null ? ponderacion : 0;
    }

    public void setPonderacion(Integer ponderacion) {
        this.ponderacion = ponderacion;
    }

    /**
     * Determina si la regla se encuentra actualmente vigente.
     * Patrón Martin Fowler: Introduce Query Method (Tell, Don't Ask).
     *
     * @return true si la vigencia es EstadoVigencia.VIGENTE, false en caso contrario.
     */
    public boolean esVigente() {
        return this.vigencia == EstadoVigencia.VIGENTE;
    }

    public EstadoVigencia getVigencia() {
        return vigencia;
    }

    public void setVigencia(EstadoVigencia vigencia) {
        if (vigencia == null) {
            throw new IllegalArgumentException("El estado de vigencia no puede ser nulo.");
        }
        this.vigencia = vigencia;
    }

    public Curso getCurso() {
        return curso;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public List<AsignacionRegla> getAsignacionesReglas() {
        return asignacionesReglas;
    }

    public void setAsignacionesReglas(List<AsignacionRegla> asignacionesReglas) {
        this.asignacionesReglas = (asignacionesReglas != null) ? asignacionesReglas : new ArrayList<>();
    }
}
