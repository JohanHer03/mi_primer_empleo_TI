package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoPostulacion;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * `Postulante` de docs/contrato-api.md (7.1): la postulación con la compatibilidad guardada y el perfil del candidato
 * con el formato de `CandidatoPerfil` (5.1).
 */
public record PostulanteDto(
        UUID id,
        Instant fecha,
        EstadoPostulacion estado,
        BigDecimal compatibilidad,
        CandidatoPerfilDto candidato) {
}
