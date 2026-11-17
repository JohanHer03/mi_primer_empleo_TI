package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.NivelHabilidad;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * `CompatibilidadDetalle` de docs/contrato-api.md (6.1): porcentaje actual, cada habilidad de la vacante con el nivel
 * del candidato y si la cumple, y la postulación (null si no se ha postulado).
 */
public record CompatibilidadDetalleDto(
        UUID vacanteId,
        BigDecimal porcentaje,
        List<HabilidadEvaluadaDto> habilidades,
        PostulacionDto postulacion) {

    /** {@code nivelCandidato} es null si el candidato no tiene la habilidad. */
    public record HabilidadEvaluadaDto(
            UUID habilidadId,
            String nombre,
            CategoriaHabilidad categoria,
            NivelHabilidad nivelRequerido,
            boolean obligatoria,
            NivelHabilidad nivelCandidato,
            boolean cumple) {
    }

    /** Con la compatibilidad guardada al postularse (regla 6). */
    public record PostulacionDto(UUID id, Instant fecha, EstadoPostulacion estado, BigDecimal compatibilidad) {
    }
}
