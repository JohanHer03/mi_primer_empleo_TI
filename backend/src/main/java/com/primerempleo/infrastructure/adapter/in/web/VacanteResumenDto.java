package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.TipoVacante;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vacante en la búsqueda del candidato (`VacanteResumen` de docs/contrato-api.md, 3.3 y 5.4).
 * {@code compatibilidad} va de 0.0 a 100.0 con un decimal; se calcula al consultar y no se guarda.
 */
public record VacanteResumenDto(
        UUID id,
        String titulo,
        EmpresaDto empresa,
        TipoVacante tipo,
        Modalidad modalidad,
        String ciudad,
        Long salarioMin,
        Long salarioMax,
        Instant fechaPublicacion,
        LocalDate fechaCierre,
        BigDecimal compatibilidad,
        boolean postulado) {

    public record EmpresaDto(UUID id, String razonSocial) {
    }
}
