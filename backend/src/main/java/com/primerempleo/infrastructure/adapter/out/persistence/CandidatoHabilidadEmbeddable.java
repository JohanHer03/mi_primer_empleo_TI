package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.NivelHabilidad;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.util.UUID;

/**
 * Fila de candidato_habilidades. Es un valor del candidato (sin id propio): la clave primaria
 * de la tabla es (candidato_id, habilidad_id).
 */
@Embeddable
public class CandidatoHabilidadEmbeddable {

    @Column(name = "habilidad_id", nullable = false)
    private UUID habilidadId;

    @Enumerated(EnumType.STRING)
    @Column(name = "nivel", nullable = false)
    private NivelHabilidad nivel;

    public UUID getHabilidadId() {
        return habilidadId;
    }

    public void setHabilidadId(UUID habilidadId) {
        this.habilidadId = habilidadId;
    }

    public NivelHabilidad getNivel() {
        return nivel;
    }

    public void setNivel(NivelHabilidad nivel) {
        this.nivel = nivel;
    }
}
