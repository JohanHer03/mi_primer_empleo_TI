package com.primerempleo.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de POST /api/auth/registro/empresa (docs/contrato-api.md, 2.4).
 * El formato del NIT (6 a 15 dígitos tras quitar puntos, guiones y espacios) lo valida el dominio.
 */
public record RegistroEmpresaPeticion(
        @NotBlank(message = "es obligatorio")
        @Email(message = "escribe un correo válido")
        @Size(max = 255, message = "máximo 255 caracteres")
        String email,

        @NotBlank(message = "es obligatoria")
        @Size(min = 8, max = 72, message = "debe tener entre 8 y 72 caracteres")
        String password,

        @NotBlank(message = "es obligatoria")
        @Size(max = 200, message = "máximo 200 caracteres")
        String razonSocial,

        @NotBlank(message = "es obligatorio")
        @Size(max = 20, message = "máximo 20 caracteres")
        String nit,

        @Size(max = 100, message = "máximo 100 caracteres")
        String sector,

        @Size(max = 100, message = "máximo 100 caracteres")
        String ciudad,

        @Size(max = 255, message = "máximo 255 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "debe ser una URL que empiece por http:// o https://")
        String sitioWeb) {

    /** Sin la contraseña, por si la petición termina en un log. */
    @Override
    public String toString() {
        return "RegistroEmpresaPeticion[email=" + email + ", nit=" + nit + "]";
    }
}
