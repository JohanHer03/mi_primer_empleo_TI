package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoAcademico;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/registro/candidato (docs/contrato-api.md, 2.3).
 * La coherencia entre semestre y estado académico la valida el dominio.
 */
public record RegistroCandidatoPeticion(
        @NotBlank(message = "es obligatorio")
        @Email(message = "escribe un correo válido")
        @Size(max = 255, message = "máximo 255 caracteres")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password,

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
        String ciudad) {

    /** Sin la contraseña, por si la petición termina en un log. */
    @Override
    public String toString() {
        return "RegistroCandidatoPeticion[email=" + email + "]";
    }
}
