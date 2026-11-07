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
import java.time.LocalDateTime;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "hallazgo_id", nullable = false)
    private Hallazgo hallazgo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_docente", nullable = true)
    private Docente docente;

    @Column(name = "fecha_solicitud")
    private LocalDateTime fechaSolicitud;

    public SolicitudRetroalimentacion() {
        this.estado = EstadoRetroEnum.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
    }

    public SolicitudRetroalimentacion(SolicitudDTO dto) {
        this.justificacion = dto != null ? dto.getJustificacion() : null;
        this.orientacionTecnica = dto != null ? dto.getOrientacion() : null;
        this.estado = EstadoRetroEnum.PENDIENTE;
        this.fechaSolicitud = LocalDateTime.now();
        if (dto != null && dto.getHallazgoId() != null) {
            Hallazgo h = new Hallazgo();
            h.setId(dto.getHallazgoId());
            this.hallazgo = h;
        }
        if (dto != null && dto.getUsuarioId() != null) {
            Estudiante e = new Estudiante();
            e.setId(dto.getUsuarioId());
            this.estudiante = e;
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

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Usuario getUsuario() {
        return estudiante;
    }

    public void setUsuario(Usuario usuario) {
        this.estudiante = Estudiante.desdeUsuario(usuario);
    }

    public void marcarSolicitudComoAtendida() {
        this.estado = EstadoRetroEnum.ATENDIDA;
    }

    public void marcarSolicitudComoAtendida(Docente docente) {
        this.docente = docente;
        this.estado = EstadoRetroEnum.ATENDIDA;
    }
}
