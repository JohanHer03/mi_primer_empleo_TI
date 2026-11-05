package com.primerempleo.domain.exception;

/**
 * El estado actual del recurso no permite la operación (responde 409).
 */
public class OperacionNoPermitidaException extends RuntimeException {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    /** Una vacante cerrada no se edita; reabrirla queda fuera del MVP (docs/contrato-api.md, 4.4). */
    public static OperacionNoPermitidaException vacanteCerrada() {
        return new OperacionNoPermitidaException("No se puede editar una vacante cerrada");
    }
}
