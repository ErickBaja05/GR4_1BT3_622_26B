package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.SolicitudDTO;
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
import jakarta.persistence.Table;

/**
 * Trazabilidad Diagrama de Clases: com.gr4.owlaudit.model.SolicitudRetroalimentacion
 * Entidad JPA que representa una solicitud de retroalimentación formulada por un Estudiante
 * sobre un hallazgo específico de una auditoría.
 */
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hallazgo_id")
    private Hallazgo hallazgo;

    public SolicitudRetroalimentacion() {
    }

    /**
     * Constructor 1:1 definido en Diagrama de Clases: + SolicitudRetroalimentacion(dto : SolicitudDTO)
     * Inicializa la entidad a partir del DTO recibido en el escenario de creación.
     */
    public SolicitudRetroalimentacion(SolicitudDTO dto) {
        if (dto != null) {
            this.id = dto.getSolicitudId();
            this.justificacion = dto.getJustificacion();
            this.orientacionTecnica = dto.getOrientacion();
            this.estado = EstadoRetroEnum.PENDIENTE; // Estado inicial según CU05 Postcondición
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

    /**
     * Método 1:1 definido en Diagrama de Clases: + setOrientacionTecnica(orientacion : String) : void
     */
    public void setOrientacionTecnica(String orientacionTecnica) {
        this.orientacionTecnica = orientacionTecnica;
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

    /**
     * Método 1:1 definido en Diagrama de Clases: + marcarSolicitudComoAtendida() : void
     * Cambia el estado de la solicitud a ATENDIDA tras la respuesta del docente.
     */
    public void marcarSolicitudComoAtendida() {
        this.estado = EstadoRetroEnum.ATENDIDA;
    }
}
