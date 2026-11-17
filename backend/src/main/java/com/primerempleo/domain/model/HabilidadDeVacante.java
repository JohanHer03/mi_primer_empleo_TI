package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /**
     * Une cada habilidad de la vacante con su elemento del catálogo y las ordena para mostrarlas
     * (detalle de la vacante y de la compatibilidad).
     *
     * @throws DatosInvalidosException si alguna habilidad no está en el catálogo
     */
    public static List<HabilidadDeVacante> unirConCatalogo(List<VacanteHabilidad> requeridas,
                                                           Collection<Habilidad> catalogo) {
        Map<UUID, Habilidad> porId = catalogo.stream()
                .collect(Collectors.toMap(Habilidad::id, Function.identity(), (x, y) -> x));
        return requeridas.stream()
                .map(requerida -> {
                    Habilidad habilidad = porId.get(requerida.habilidadId());
                    if (habilidad == null) {
                        throw new DatosInvalidosException(
                                "habilidades: la habilidad " + requerida.habilidadId() + " no existe");
                    }
                    return new HabilidadDeVacante(habilidad, requerida.nivelRequerido(), requerida.obligatoria());
                })
                .sorted(ORDEN_DETALLE)
                .toList();
    }
}
