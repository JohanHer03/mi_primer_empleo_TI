package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoPostulacion;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de PATCH /api/postulaciones/{id}/estado (docs/contrato-api.md, 7.2). */
public record CambiarEstadoPeticion(
        @NotNull(message = "es obligatorio")
        EstadoPostulacion estado) {
}
