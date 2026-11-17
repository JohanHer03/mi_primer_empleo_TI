package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Postulación vista por el candidato (`PostulacionDeCandidato` de docs/contrato-api.md, 5.5 y 5.6).
 * {@code compatibilidad} es la que se guardó al postularse (regla 6).
 */
public record PostulacionDeCandidatoDto(
        UUID id,
        Instant fecha,
        EstadoPostulacion estado,
        BigDecimal compatibilidad,
        VacanteDto vacante) {

    public record VacanteDto(
            UUID id,
            String titulo,
            EmpresaDto empresa,
            Modalidad modalidad,
            String ciudad,
            EstadoVacante estado,
            LocalDate fechaCierre) {
    }

    public record EmpresaDto(UUID id, String razonSocial) {
    }
}
