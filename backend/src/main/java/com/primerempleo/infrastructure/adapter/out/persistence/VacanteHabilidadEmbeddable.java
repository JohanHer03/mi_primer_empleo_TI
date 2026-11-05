package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.NivelHabilidad;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;

/**
 * Fila de vacante_habilidades. Es un valor de la vacante (sin id propio): la clave primaria
 * de la tabla es (vacante_id, habilidad_id).
 */
@Embeddable
public class VacanteHabilidadEmbeddable {

    @Column(name = "habilidad_id", nullable = false)
    private UUID habilidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_requerido", nullable = false)
    private NivelHabilidad nivelRequerido;

    @Column(name = "obligatoria", nullable = false)
    private boolean obligatoria;

    public UUID getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(UUID habilidadId) {
        this.habilidadId = habilidadId;
    }

    public NivelHabilidad getNivelRequerido() {
        return nivelRequerido;
    }

    public void setNivelRequerido(NivelHabilidad nivelRequerido) {
        this.nivelRequerido = nivelRequerido;
    }

    public boolean isObligatoria() {
        return obligatoria;
    }

    public void setObligatoria(boolean obligatoria) {
        this.obligatoria = obligatoria;
    }
}
