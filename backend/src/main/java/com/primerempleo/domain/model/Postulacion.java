package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

/**
 * Postulación de un candidato a una vacante (HU-05). La compatibilidad se calcula al postularse y se guarda:
 * no cambia aunque después cambien las habilidades del candidato o de la vacante (regla 6).
 */
public record Postulacion(
        UUID id,
        UUID candidatoId,
        UUID vacanteId,
        Instant fecha,
        EstadoPostulacion estado,
        BigDecimal compatibilidad) {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    public Postulacion {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(candidatoId, "candidatoId");
        Objects.requireNonNull(vacanteId, "vacanteId");
        Objects.requireNonNull(fecha, "fecha");
        Objects.requireNonNull(estado, "estado");
        Objects.requireNonNull(compatibilidad, "compatibilidad");
        if (compatibilidad.signum() < 0 || compatibilidad.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("La compatibilidad debe estar entre 0 y 100: " + compatibilidad);
        }
    }

    /** Postulación recién enviada (regla 5: empieza en ENVIADA). */
    public static Postulacion nueva(UUID candidatoId, UUID vacanteId, BigDecimal compatibilidad, Instant ahora) {
        return new Postulacion(UUID.randomUUID(), candidatoId, vacanteId, ahora.truncatedTo(ChronoUnit.MICROS),
                EstadoPostulacion.ENVIADA, compatibilidad);
    }

    /**
     * Regla 5: la empresa avanza la postulación de un estado al siguiente o la rechaza. La compatibilidad no cambia (regla 6).
     *
     * @throws OperacionNoPermitidaException si la transición no está permitida (también pedir el mismo estado)
     */
    public Postulacion cambiarEstado(EstadoPostulacion nuevo) {
        if (estado.esFinal()) {
            throw OperacionNoPermitidaException.postulacionEnEstadoFinal(estado);
        }
        if (!estado.puedePasarA(nuevo)) {
            throw OperacionNoPermitidaException.transicionNoPermitida(estado);
        }
        return new Postulacion(id, candidatoId, vacanteId, fecha, nuevo, compatibilidad);
    }
}
