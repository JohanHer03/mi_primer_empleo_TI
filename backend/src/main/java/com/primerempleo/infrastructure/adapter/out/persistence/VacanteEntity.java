package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.TipoVacante;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tabla vacantes. Sus habilidades (vacante_habilidades) son una colección de valores: se guardan, cargan
 * y reemplazan junto con la vacante. fecha_creacion no se mapea: la pone la base con su valor por defecto.
 */
@Entity
@Table(name = "vacantes")
public class VacanteEntity extends EntidadConIdAsignado {

    @Column(name = "empresa_id", nullable = false, updatable = false)
    private UUID empresaId;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    private TipoVacante tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidad", nullable = false)
    private Modalidad modalidad;

    @Column(name = "ciudad", nullable = false)
    private String ciudad;

    @Column(name = "salario_min")
    private BigDecimal salarioMin;

    @Column(name = "salario_max")
    private BigDecimal salarioMax;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoVacante estado;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private Instant fechaPublicacion;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    @ElementCollection
    @CollectionTable(name = "vacante_habilidades", joinColumns = @JoinColumn(name = "vacante_id"))
    private List<VacanteHabilidadEmbeddable> habilidades = new ArrayList<>();

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoVacante getTipo() {
        return tipo;
    }

    public void setTipo(TipoVacante tipo) {
        this.tipo = tipo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public BigDecimal getSalarioMin() {
        return salarioMin;
    }

    public void setSalarioMin(BigDecimal salarioMin) {
        this.salarioMin = salarioMin;
    }

    public BigDecimal getSalarioMax() {
        return salarioMax;
    }

    public void setSalarioMax(BigDecimal salarioMax) {
        this.salarioMax = salarioMax;
    }

    public EstadoVacante getEstado() {
        return estado;
    }

    public void setEstado(EstadoVacante estado) {
        this.estado = estado;
    }

    public Instant getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Instant fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public List<VacanteHabilidadEmbeddable> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<VacanteHabilidadEmbeddable> habilidades) {
        this.habilidades = habilidades;
    }
}
