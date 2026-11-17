package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoPostulacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Tabla postulaciones (V1). La restricción uq_postulaciones_candidato_vacante impide postularse dos veces (regla 1).
 */
@Entity
@Table(name = "postulaciones")
public class PostulacionEntity extends EntidadConIdAsignado {

    @Column(name = "candidato_id", nullable = false, updatable = false)
    private UUID candidatoId;

    @Column(name = "vacante_id", nullable = false, updatable = false)
    private UUID vacanteId;

    @Column(name = "fecha", nullable = false, updatable = false)
    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoPostulacion estado;

    /** Se calcula al postularse y no cambia (regla 6). */
    @Column(name = "compatibilidad", nullable = false, updatable = false)
    private BigDecimal compatibilidad;

    public UUID getCandidatoId() {
        return candidatoId;
    }

    public void setCandidatoId(UUID candidatoId) {
        this.candidatoId = candidatoId;
    }

    public UUID getVacanteId() {
        return vacanteId;
    }

    public void setVacanteId(UUID vacanteId) {
        this.vacanteId = vacanteId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public EstadoPostulacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoPostulacion estado) {
        this.estado = estado;
    }

    public BigDecimal getCompatibilidad() {
        return compatibilidad;
    }

    public void setCompatibilidad(BigDecimal compatibilidad) {
        this.compatibilidad = compatibilidad;
    }
}
