package com.primerempleo.domain.model;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;

/**
 * Habilidad que pide una vacante, con los datos del catálogo que muestra el detalle.
 */
public record HabilidadDeVacante(Habilidad habilidad, NivelHabilidad nivelRequerido, boolean obligatoria) {

    private static final Collator ESPANOL = Collator.getInstance(Locale.forLanguageTag("es-CO"));

    /** Orden del detalle (docs/contrato-api.md, 4.4): primero las obligatorias y luego por nombre. */
    public static final Comparator<HabilidadDeVacante> ORDEN_DETALLE = Comparator
            .comparing(HabilidadDeVacante::obligatoria).reversed()
            .thenComparing(h -> h.habilidad().nombre(), ESPANOL);

    public HabilidadDeVacante {
        Objects.requireNonNull(habilidad, "habilidad");
        Objects.requireNonNull(nivelRequerido, "nivelRequerido");
    }
}
