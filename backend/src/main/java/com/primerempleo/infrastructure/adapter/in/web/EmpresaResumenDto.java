package com.primerempleo.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.UUID;

/** Empresa en el listado del administrador (`EmpresaResumen` de docs/contrato-api.md, 3.1). */
public record EmpresaResumenDto(
        UUID id,
        String razonSocial,
        String nit,
        String email,
        String sector,
        String ciudad,
        String sitioWeb,
        boolean verificada,
        Instant fechaCreacion) {
}
