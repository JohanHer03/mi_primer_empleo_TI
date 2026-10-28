package com.primerempleo.domain.exception;

/**
 * Datos que no cumplen una regla del dominio (responde 400).
 * El mensaje sigue el formato "campo: mensaje" para que el frontend lo muestre bajo el campo.
 */
public class DatosInvalidosException extends RuntimeException {

    public DatosInvalidosException(String mensaje) {
        super(mensaje);
    }
}
