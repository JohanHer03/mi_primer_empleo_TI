package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.NivelHabilidad;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Elemento del cuerpo de PUT /api/candidatos/me/habilidades (docs/contrato-api.md, 5.2). */
public record CandidatoHabilidadPeticion(
        @NotNull(message = "es obligatorio")
        UUID habilidadId,

        @NotNull(message = "es obligatorio")
        NivelHabilidad nivel) {
}
