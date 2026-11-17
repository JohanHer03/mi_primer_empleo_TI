package com.primerempleo.domain.model;

import java.util.UUID;

/**
 * Filtros de la búsqueda de vacantes del candidato (HU-03, docs/contrato-api.md 5.4). Todos opcionales.
 * {@code tipo} es el "nivel" de HU-03 (práctica o junior); la ciudad se compara sin mayúsculas ni espacios a los lados.
 */
public record FiltroVacantes(Modalidad modalidad, TipoVacante tipo, UUID habilidadId, String ciudad) {

    public FiltroVacantes {
        ciudad = Textos.opcional(ciudad);
    }

    public static FiltroVacantes sinFiltros() {
        return new FiltroVacantes(null, null, null, null);
    }
}
