package com.primerempleo.domain.exception;

/**
 * El recurso ya existe: correo o NIT repetido, postulación duplicada (responde 409).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
