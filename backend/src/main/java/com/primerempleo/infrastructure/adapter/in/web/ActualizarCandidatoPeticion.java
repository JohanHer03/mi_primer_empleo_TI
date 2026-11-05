package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoAcademico;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/candidatos/me (docs/contrato-api.md, 5.1), con las validaciones del registro (2.3).
 * El correo no se edita y las habilidades van por PUT /api/candidatos/me/habilidades.
 */
public record ActualizarCandidatoPeticion(
        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String nombres,

        @NotBlank(message = "es obligatorio")
        @Size(max = 100, message = "máximo 100 caracteres")
        String apellidos,

        @NotNull(message = "es obligatorio")
        EstadoAcademico estadoAcademico,

        @Min(value = 1, message = "debe estar entre 1 y 12")
        @Max(value = 12, message = "debe estar entre 1 y 12")
        Integer semestre,

        @Size(max = 150, message = "máximo 150 caracteres")
        String universidad,

        @Size(max = 150, message = "máximo 150 caracteres")
        String programa,

        @Size(max = 100, message = "máximo 100 caracteres")
        String ciudad,

        @Size(max = 255, message = "máximo 255 caracteres")
        @Pattern(regexp = "^https://github\\.com/\\S+$", message = "debe empezar por https://github.com/")
        String githubUrl,

        @Size(max = 1000, message = "máximo 1000 caracteres")
        String resumen) {
}
