package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoAcademico;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tabla candidatos. fecha_creacion no se mapea: la pone la base con su valor por defecto.
 * semestre es SMALLINT, por eso Short (con Integer, ddl-auto=validate rechaza el tipo).
 * Sus habilidades (candidato_habilidades) son una colección de valores: se guardan y reemplazan junto con el candidato.
 */
@Entity
@Table(name = "candidatos")
public class CandidatoEntity extends EntidadConIdAsignado {

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private UUID usuarioId;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "universidad")
    private String universidad;

    @Column(name = "programa")
    private String programa;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_academico", nullable = false)
    private EstadoAcademico estadoAcademico;

    @Column(name = "semestre")
    private Short semestre;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "github_url")
    private String githubUrl;

    @Column(name = "resumen", columnDefinition = "text")
    private String resumen;

    @ElementCollection
    @CollectionTable(name = "candidato_habilidades", joinColumns = @JoinColumn(name = "candidato_id"))
    private List<CandidatoHabilidadEmbeddable> habilidades = new ArrayList<>();

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getUniversidad() {
        return universidad;
    }

    public void setUniversidad(String universidad) {
        this.universidad = universidad;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public EstadoAcademico getEstadoAcademico() {
        return estadoAcademico;
    }

    public void setEstadoAcademico(EstadoAcademico estadoAcademico) {
        this.estadoAcademico = estadoAcademico;
    }

    public Short getSemestre() {
        return semestre;
    }

    public void setSemestre(Short semestre) {
        this.semestre = semestre;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public List<CandidatoHabilidadEmbeddable> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<CandidatoHabilidadEmbeddable> habilidades) {
        this.habilidades = habilidades;
    }
}
