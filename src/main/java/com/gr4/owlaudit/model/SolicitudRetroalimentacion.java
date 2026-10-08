package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.SolicitudDTO;
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
@Table(name = "solicitudes_retroalimentacion")
public class SolicitudRetroalimentacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "justificacion", nullable = false, length = 1000)
    private String justificacion;

    @Column(name = "orientacion_tecnica", length = 1000)
    private String orientacionTecnica;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoRetroEnum estado;

    @OneToOne
    @JoinColumn(name = "hallazgo_id", nullable = false, unique = true)
    private Hallazgo hallazgo;

    public SolicitudRetroalimentacion() {
        this.estado = EstadoRetroEnum.PENDIENTE;
    }

    public SolicitudRetroalimentacion(SolicitudDTO dto) {
        this.justificacion = dto != null ? dto.getJustificacion() : null;
        this.orientacionTecnica = dto != null ? dto.getOrientacion() : null;
        this.estado = EstadoRetroEnum.PENDIENTE;
        if (dto != null && dto.getHallazgoId() != null) {
            Hallazgo h = new Hallazgo();
            h.setId(dto.getHallazgoId());
            this.hallazgo = h;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getOrientacionTecnica() {
        return orientacionTecnica;
    }

    public void setOrientacionTecnica(String orientacion) {
        this.orientacionTecnica = orientacion;
    }

    public EstadoRetroEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoRetroEnum estado) {
        this.estado = estado;
    }

    public Hallazgo getHallazgo() {
        return hallazgo;
    }

    public void setHallazgo(Hallazgo hallazgo) {
        this.hallazgo = hallazgo;
    }

    public void marcarSolicitudComoAtendida() {
        this.estado = EstadoRetroEnum.ATENDIDA;
    }
}
