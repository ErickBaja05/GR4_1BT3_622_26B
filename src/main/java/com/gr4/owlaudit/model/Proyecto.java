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

    @jakarta.persistence.ManyToOne(optional = false)
    @jakarta.persistence.JoinColumn(name = "usuario_id", nullable = false)
    private Estudiante estudiante;

    @Column(name = "id_estudiante")
    private Long idEstudiante;

    public Proyecto() {
    }

    public Proyecto(NuevoProyectoDTO dto) {
        if (dto != null) {
            this.nombre = dto.getNombre();
            this.urlGithub = dto.getUrlGithub();
            this.fechaRegistro = LocalDateTime.now();
            this.idEstudiante = dto.getIdEstudiante();
            if (dto.getUsuarioId() != null) {
                Estudiante e = new Estudiante();
                e.setId(dto.getUsuarioId());
                this.estudiante = e;
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

    public String getUrlRepositorio() {
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

    public Estudiante getEstudiante() {
        return estudiante;
    }

    public void setEstudiante(Estudiante estudiante) {
        this.estudiante = estudiante;
    }

    public Usuario getUsuario() {
        return estudiante;
    }

    public void setUsuario(Usuario usuario) {
        this.estudiante = Estudiante.desdeUsuario(usuario);
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

    public boolean esPropietario(Long usuarioId) {
        return usuarioId != null && this.estudiante != null && usuarioId.equals(this.estudiante.getId());
    }

    /**
     * Refactorización Martin Fowler: Move Method (Moving Features Between Objects).
     * Mueve la lógica de actualización de atributos desde ProyectoServiceImpl hacia la entidad Proyecto.
     */
    public void actualizarDesdeDTO(NuevoProyectoDTO dto) {
        if (dto != null) {
            if (dto.getNombre() != null) {
                this.nombre = dto.getNombre().trim();
            }
            if (dto.getUrlGithub() != null) {
                this.urlGithub = dto.getUrlGithub().trim();
            }
        }
    }
}
