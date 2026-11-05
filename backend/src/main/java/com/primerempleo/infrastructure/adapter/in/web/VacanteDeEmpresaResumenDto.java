package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.TipoVacante;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Vacante en el listado de la empresa (`VacanteDeEmpresaResumen` de docs/contrato-api.md, 3.2). */
public record VacanteDeEmpresaResumenDto(
        UUID id,
        String titulo,
        TipoVacante tipo,
        Modalidad modalidad,
        String ciudad,
        EstadoVacante estado,
        Instant fechaPublicacion,
        LocalDate fechaCierre,
        long totalPostulaciones) {
}
