package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Objects;
import java.util.UUID;

/**
 * Habilidad que declara el candidato con su nivel (HU-02). La compatibilidad compara este nivel con el que pide la vacante.
 */
public record CandidatoHabilidad(UUID habilidadId, NivelHabilidad nivel) {

    public CandidatoHabilidad {
        Objects.requireNonNull(habilidadId, "habilidadId");
        if (nivel == null) {
            throw new DatosInvalidosException("nivel: es obligatorio");
        }
    }
}
