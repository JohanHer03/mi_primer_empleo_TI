package com.primerempleo.domain.exception;

import com.primerempleo.domain.model.EstadoPostulacion;
import java.util.stream.Collectors;

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

    /** Regla 2: la vacante está cerrada o su fecha de cierre ya pasó (docs/contrato-api.md, 5.5). */
    public static OperacionNoPermitidaException vacanteSinPostulaciones() {
        return new OperacionNoPermitidaException("Esta vacante ya no recibe postulaciones");
    }

    /** Regla 5: el mensaje dice a qué estados sí puede pasar (docs/contrato-api.md, 7.2). */
    public static OperacionNoPermitidaException transicionNoPermitida(EstadoPostulacion actual) {
        String permitidos = actual.siguientes().stream()
                .map(Enum::name)
                .collect(Collectors.joining(" o "));
        return new OperacionNoPermitidaException(
                "Una postulación " + actual.name() + " solo puede pasar a " + permitidos);
    }

    /** Regla 5: CONTRATADO y RECHAZADA son finales. */
    public static OperacionNoPermitidaException postulacionEnEstadoFinal(EstadoPostulacion actual) {
        return new OperacionNoPermitidaException("Una postulación " + actual.name() + " ya no cambia de estado");
    }
}
