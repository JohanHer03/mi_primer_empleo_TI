package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.TipoVacante;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Cuerpo de POST /api/vacantes y PUT /api/vacantes/{id} (docs/contrato-api.md, 4.4).
 * Las reglas que cruzan campos (salarios, fecha de cierre, habilidades repetidas o sin obligatoria) las valida el dominio.
 */
public record VacantePeticion(
        @NotBlank(message = "es obligatorio")
        @Size(max = 150, message = "máximo 150 caracteres")
        String titulo,

        @NotBlank(message = "es obligatoria")
        @Size(max = 5000, message = "máximo 5000 caracteres")
        String descripcion,

        @NotNull(message = "es obligatorio")
        TipoVacante tipo,

        @NotNull(message = "es obligatoria")
        Modalidad modalidad,

        @NotBlank(message = "es obligatoria")
        @Size(max = 100, message = "máximo 100 caracteres")
        String ciudad,

        @PositiveOrZero(message = "no puede ser negativo")
        @Max(value = 9_999_999_999L, message = "es demasiado alto")
        Long salarioMin,

        @PositiveOrZero(message = "no puede ser negativo")
        @Max(value = 9_999_999_999L, message = "es demasiado alto")
        Long salarioMax,

        @NotNull(message = "es obligatoria")
        LocalDate fechaCierre,

        @NotEmpty(message = "debe tener entre 1 y 15")
        @Size(max = 15, message = "debe tener entre 1 y 15")
        List<@NotNull(message = "no puede ser null") @Valid HabilidadPeticion> habilidades) {

    /** Habilidad que pide la vacante; {@code habilidadId} debe existir en el catálogo. */
    public record HabilidadPeticion(
            @NotNull(message = "es obligatorio")
            UUID habilidadId,

            @NotNull(message = "es obligatorio")
            NivelHabilidad nivelRequerido,

            @NotNull(message = "es obligatorio")
            Boolean obligatoria) {
    }
}
