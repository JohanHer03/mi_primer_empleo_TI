package com.primerempleo.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cuerpo de PUT /api/empresas/me (docs/contrato-api.md, 4.2), con las validaciones del registro (2.4).
 * El NIT y el correo no se editan.
 */
public record ActualizarEmpresaPeticion(
        @NotBlank(message = "es obligatoria")
        @Size(max = 200, message = "máximo 200 caracteres")
        String razonSocial,

        @Size(max = 100, message = "máximo 100 caracteres")
        String sector,

        @Size(max = 100, message = "máximo 100 caracteres")
        String ciudad,

        @Size(max = 255, message = "máximo 255 caracteres")
        @Pattern(regexp = "^https?://\\S+$", message = "debe ser una URL que empiece por http:// o https://")
        String sitioWeb) {
}
