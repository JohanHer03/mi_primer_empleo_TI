package com.primerempleo.domain.exception;

/**
 * Correo inexistente o contraseña errónea (responde 401).
 * Un solo mensaje para ambos casos, así no se revela qué correos están registrados.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contraseña incorrectos");
    }
}
