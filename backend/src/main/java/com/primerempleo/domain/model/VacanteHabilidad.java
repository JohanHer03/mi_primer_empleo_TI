package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Objects;
import java.util.UUID;

/**
 * Habilidad que pide una vacante. Las obligatorias pesan el doble en la compatibilidad (CLAUDE.md).
 */
public record VacanteHabilidad(UUID habilidadId, NivelHabilidad nivelRequerido, boolean obligatoria) {

    public VacanteHabilidad {
        Objects.requireNonNull(habilidadId, "habilidadId");
        if (nivelRequerido == null) {
            throw new DatosInvalidosException("nivelRequerido: es obligatorio");
        }
    }
}
