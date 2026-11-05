package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.TipoVacante;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Detalle de una vacante (`VacanteDetalle` de docs/contrato-api.md, 4.4). */
public record VacanteDetalleDto(
        UUID id,
        String titulo,
        String descripcion,
        TipoVacante tipo,
        Modalidad modalidad,
        String ciudad,
        Long salarioMin,
        Long salarioMax,
        EstadoVacante estado,
        Instant fechaPublicacion,
        LocalDate fechaCierre,
        EmpresaDto empresa,
        List<HabilidadRequeridaDto> habilidades) {

    public record EmpresaDto(UUID id, String razonSocial) {
    }

    /** Ordenadas: primero las obligatorias y luego por nombre. */
    public record HabilidadRequeridaDto(
            UUID habilidadId,
            String nombre,
            CategoriaHabilidad categoria,
            NivelHabilidad nivelRequerido,
            boolean obligatoria) {
    }
}
