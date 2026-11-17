package com.primerempleo.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Compatibilidad actual de un candidato con una vacante y lo que le falta (HU-04, docs/contrato-api.md 6.1).
 * No se guarda: la que se guarda es la de la postulación (regla 6), que va aparte si el candidato ya se postuló.
 * Se arma con {@code CalculadoraCompatibilidad.detallar}.
 *
 * @param habilidades todas las habilidades de la vacante, en el orden del detalle; las que no cumple son "lo que te falta"
 * @param postulacion la postulación del candidato a esta vacante, o null si no se ha postulado
 */
public record CompatibilidadDetalle(UUID vacanteId, BigDecimal porcentaje, List<HabilidadEvaluada> habilidades,
                                    Postulacion postulacion) {

    /**
     * Habilidad que pide la vacante, con el nivel del candidato.
     *
     * @param nivelCandidato null si el candidato no la tiene
     * @param cumple true si el nivel del candidato alcanza el requerido (puntaje 1)
     */
    public record HabilidadEvaluada(HabilidadDeVacante requerida, NivelHabilidad nivelCandidato, boolean cumple) {

        public HabilidadEvaluada {
            Objects.requireNonNull(requerida, "requerida");
        }
    }

    public CompatibilidadDetalle {
        Objects.requireNonNull(vacanteId, "vacanteId");
        Objects.requireNonNull(porcentaje, "porcentaje");
        habilidades = List.copyOf(habilidades);
    }
}
