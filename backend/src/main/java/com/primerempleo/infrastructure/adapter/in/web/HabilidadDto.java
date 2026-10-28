package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CategoriaHabilidad;
import java.util.UUID;

/** Habilidad del catálogo (docs/contrato-api.md, 4.1). */
public record HabilidadDto(UUID id, String nombre, CategoriaHabilidad categoria) {
}
