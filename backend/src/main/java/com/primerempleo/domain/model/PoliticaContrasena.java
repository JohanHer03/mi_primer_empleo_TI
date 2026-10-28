package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.nio.charset.StandardCharsets;

/**
 * Reglas de una contraseña nueva: mínimo 8 caracteres y máximo 72 bytes.
 * El máximo es el límite de BCrypt, que cuenta bytes en UTF-8: las tildes y la ñ ocupan 2,
 * así que 72 caracteres con tildes pueden pasarse aunque cumplan el @Size del DTO.
 */
public final class PoliticaContrasena {

    private static final int CARACTERES_MINIMOS = 8;
    private static final int BYTES_MAXIMOS = 72;

    private PoliticaContrasena() {
    }

    public static void validar(String contrasena) {
        if (contrasena == null || contrasena.length() < CARACTERES_MINIMOS) {
            throw new DatosInvalidosException("password: debe tener al menos 8 caracteres");
        }
        if (contrasena.getBytes(StandardCharsets.UTF_8).length > BYTES_MAXIMOS) {
            throw new DatosInvalidosException(
                    "password: es demasiado larga (máximo 72 bytes; las tildes y la ñ ocupan 2)");
        }
    }
}
