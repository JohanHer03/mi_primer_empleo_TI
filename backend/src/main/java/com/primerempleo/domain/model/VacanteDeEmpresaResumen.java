package com.primerempleo.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vacante en el listado de la empresa (docs/contrato-api.md, 3.2), con cuántos candidatos se han postulado.
 */
public record VacanteDeEmpresaResumen(
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
