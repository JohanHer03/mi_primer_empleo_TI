package com.primerempleo.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/auth/login (docs/contrato-api.md, 2.5). */
public record LoginPeticion(
        @NotBlank(message = "es obligatorio")
        @Email(message = "escribe un correo válido")
        String email,

        @NotBlank(message = "es obligatoria")
        String password) {

    /** Sin la contraseña, por si la petición termina en un log. */
    @Override
    public String toString() {
        return "LoginPeticion[email=" + email + "]";
    }
}
