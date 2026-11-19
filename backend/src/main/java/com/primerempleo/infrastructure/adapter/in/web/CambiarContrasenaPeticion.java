package com.primerempleo.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/usuarios/me/contrasena (docs/contrato-api.md, 8.1).
 * El máximo real de la nueva (72 bytes) lo valida PoliticaContrasena en el dominio.
 */
public record CambiarContrasenaPeticion(
        @NotBlank(message = "es obligatoria")
        @Size(max = 72, message = "máximo 72 caracteres")
        String contrasenaActual,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String contrasenaNueva) {

    /** Sin las contraseñas, por si la petición termina en un log. */
    @Override
    public String toString() {
        return "CambiarContrasenaPeticion[]";
    }
}
