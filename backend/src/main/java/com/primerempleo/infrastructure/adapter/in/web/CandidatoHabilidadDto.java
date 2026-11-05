package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import java.util.UUID;

/** Habilidad del candidato con los datos del catálogo (`CandidatoHabilidad` de docs/contrato-api.md, 5.1 y 5.2). */
public record CandidatoHabilidadDto(UUID habilidadId, String nombre, CategoriaHabilidad categoria, NivelHabilidad nivel) {
}
