package com.primerempleo.domain.model;

/**
 * Estados de una postulación (regla 5): ENVIADA → EN_REVISION → ENTREVISTA → CONTRATADO;
 * RECHAZADA desde cualquiera antes de CONTRATADO.
 */
public enum EstadoPostulacion {
    ENVIADA,
    EN_REVISION,
    ENTREVISTA,
    RECHAZADA,
    CONTRATADO
}
