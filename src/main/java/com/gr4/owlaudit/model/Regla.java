package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.NuevaReglaDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reglas")
public class Regla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_motor", nullable = false, length = 50)
    private TipoMotorEnum tipoMotor;

    @Column(name = "nombre_representativo", nullable = false, unique = true, length = 100)
    private String nombreRepresentativo;

    @Column(name = "descripcion_detallada", length = 200)
    private String descripcionDetallada;

    @Column(name = "parametro_exacto", nullable = false, length = 100)
    private String parametroExacto;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_severidad", nullable = false, length = 20)
    private NivelSeveridadEnum nivelSeveridad;

    @Column(name = "ponderacion", nullable = false)
    private int ponderacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "vigencia", nullable = false, length = 20)
    private EstadoVigencia vigencia = EstadoVigencia.VIGENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "vigencia", nullable = false, length = 20)
    private EstadoVigencia vigencia = EstadoVigencia.VIGENTE;

    public Regla() {
        this.vigencia = EstadoVigencia.VIGENTE;
    }

    public Regla(NuevaReglaDTO dto) {
        if (dto != null) {
            if (dto.getTipoMotor() != null) {
                this.tipoMotor = TipoMotorEnum.valueOf(dto.getTipoMotor().trim().toUpperCase());
            }
            this.nombreRepresentativo = dto.getNombreRepresentativo();
            this.descripcionDetallada = dto.getDescripcionDetallada();
            this.parametroExacto = dto.getParametroExacto();
            if (dto.getNivelSeveridad() != null) {
                this.nivelSeveridad = NivelSeveridadEnum.valueOf(dto.getNivelSeveridad().trim().toUpperCase());
            }
            this.ponderacion = dto.getPonderacion();
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
        return ponderacion;
    }

    public void setPonderacion(int ponderacion) {
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

    public EstadoVigencia getVigencia() {
        return vigencia;
    }

    public void setVigencia(EstadoVigencia vigencia) {
        this.vigencia = vigencia;
    }

}
