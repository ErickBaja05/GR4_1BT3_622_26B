package com.gr4.owlaudit.model;

import com.gr4.owlaudit.dto.NuevoProyectoDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "url_github", nullable = false, length = 255)
    private String urlGithub;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "id_estudiante")
    private Long idEstudiante;

    public Proyecto() {
    }

    public Proyecto(NuevoProyectoDTO dto) {
        if (dto != null) {
            this.nombre = dto.getNombre();
            this.urlGithub = dto.getUrlGithub();
            this.fechaRegistro = LocalDateTime.now();
            if (dto.getUsuarioId() != null) {
                Usuario u = new Usuario();
                u.setId(dto.getUsuarioId());
                this.usuario = u;
            }
        }
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

    public String getUrlGithub() {
        return urlGithub;
    }

    public void setUrlGithub(String urlGithub) {
        this.urlGithub = urlGithub;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Long getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(Long idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public Long getIdVisible() {
        return idEstudiante != null ? idEstudiante : id;
    }
}
