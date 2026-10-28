package com.primerempleo.domain.exception;

/**
 * El recurso pedido no existe (responde 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException empresa() {
        return new RecursoNoEncontradoException("La empresa no existe");
    }
}
