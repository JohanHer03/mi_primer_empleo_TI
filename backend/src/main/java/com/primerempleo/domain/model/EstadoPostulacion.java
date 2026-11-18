package com.primerempleo.domain.model;

import java.util.List;

/**
 * Estados de una postulación (regla 5): ENVIADA → EN_REVISION → ENTREVISTA → CONTRATADO;
 * RECHAZADA desde cualquiera antes de CONTRATADO. Se avanza de uno en uno (docs/contrato-api.md, 7.2).
 */
public enum EstadoPostulacion {
    ENVIADA,
    EN_REVISION,
    ENTREVISTA,
    RECHAZADA,
    CONTRATADO;

    /** Estados a los que puede pasar desde este (primero el avance y luego el rechazo); vacío para los finales. */
    public List<EstadoPostulacion> siguientes() {
        return switch (this) {
            case ENVIADA -> List.of(EN_REVISION, RECHAZADA);
            case EN_REVISION -> List.of(ENTREVISTA, RECHAZADA);
            case ENTREVISTA -> List.of(CONTRATADO, RECHAZADA);
            case RECHAZADA, CONTRATADO -> List.of();
        };
    }

    /** Pasar al mismo estado no es una transición válida. */
    public boolean puedePasarA(EstadoPostulacion nuevo) {
        return siguientes().contains(nuevo);
    }

    public boolean esFinal() {
        return siguientes().isEmpty();
    }
}
