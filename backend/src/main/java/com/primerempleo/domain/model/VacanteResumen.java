package com.primerempleo.domain.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Vacante en la búsqueda del candidato (docs/contrato-api.md, 3.3 y 5.4): con la empresa, la compatibilidad
 * calculada al consultar (no se guarda) y si el candidato ya se postuló.
 */
public record VacanteResumen(Vacante vacante, Empresa empresa, BigDecimal compatibilidad, boolean postulado) {

    public VacanteResumen {
        Objects.requireNonNull(vacante, "vacante");
        Objects.requireNonNull(empresa, "empresa");
        Objects.requireNonNull(compatibilidad, "compatibilidad");
    }
}
