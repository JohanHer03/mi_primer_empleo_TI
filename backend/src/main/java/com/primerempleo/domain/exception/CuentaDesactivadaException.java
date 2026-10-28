package com.primerempleo.domain.exception;

/**
 * La cuenta existe y la contraseña es correcta, pero está desactivada (responde 403).
 */
public class CuentaDesactivadaException extends RuntimeException {

    public CuentaDesactivadaException() {
        super("Tu cuenta está desactivada");
    }
}
