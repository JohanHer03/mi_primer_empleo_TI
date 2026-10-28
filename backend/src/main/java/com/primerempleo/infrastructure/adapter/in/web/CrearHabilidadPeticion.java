package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CategoriaHabilidad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Cuerpo de POST /api/habilidades (docs/contrato-api.md, 4.1). */
public record CrearHabilidadPeticion(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String nombre,

        @NotNull(message = "es obligatoria")
        CategoriaHabilidad categoria) {
}
