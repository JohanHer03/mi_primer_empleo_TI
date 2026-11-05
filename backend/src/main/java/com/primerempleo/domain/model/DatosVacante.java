package com.primerempleo.domain.model;

import java.time.LocalDate;
import java.util.List;

/**
 * Lo que la empresa escribe al publicar o editar una vacante (docs/contrato-api.md, 4.4).
 * Solo transporta los datos: las reglas se validan al crear la {@link Vacante}.
 */
public record DatosVacante(
        String titulo,
        String descripcion,
        TipoVacante tipo,
        Modalidad modalidad,
        String ciudad,
        Long salarioMin,
        Long salarioMax,
        LocalDate fechaCierre,
        List<VacanteHabilidad> habilidades) {

    public DatosVacante {
        habilidades = habilidades == null ? List.of() : List.copyOf(habilidades);
    }
}
