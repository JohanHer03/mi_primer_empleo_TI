package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        return new VacanteDetalle(vacante, empresa, HabilidadDeVacante.unirConCatalogo(vacante.habilidades(), catalogo));
    }
}
