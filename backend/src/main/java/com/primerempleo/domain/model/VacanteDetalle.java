package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vacante con la empresa que la publica y los datos del catálogo de cada habilidad (detalle de la 4.4).
 */
public record VacanteDetalle(Vacante vacante, Empresa empresa, List<HabilidadDeVacante> habilidades) {

    public VacanteDetalle {
        Objects.requireNonNull(vacante, "vacante");
        Objects.requireNonNull(empresa, "empresa");
        habilidades = List.copyOf(habilidades);
    }

    /**
     * Une cada habilidad de la vacante con su elemento del catálogo y las ordena para mostrarlas.
     *
     * @throws DatosInvalidosException si alguna habilidad no está en el catálogo
     */
    public static VacanteDetalle de(Vacante vacante, Empresa empresa, Collection<Habilidad> catalogo) {
        Map<UUID, Habilidad> porId = catalogo.stream()
                .collect(Collectors.toMap(Habilidad::id, Function.identity(), (a, b) -> a));
        List<HabilidadDeVacante> habilidades = vacante.habilidades().stream()
                .map(requerida -> {
                    Habilidad habilidad = porId.get(requerida.habilidadId());
                    if (habilidad == null) {
                        throw new DatosInvalidosException(
                                "habilidades: la habilidad " + requerida.habilidadId() + " no existe");
                    }
                    return new HabilidadDeVacante(habilidad, requerida.nivelRequerido(), requerida.obligatoria());
                })
                .sorted(HabilidadDeVacante.ORDEN_DETALLE)
                .toList();
        return new VacanteDetalle(vacante, empresa, habilidades);
    }
}
