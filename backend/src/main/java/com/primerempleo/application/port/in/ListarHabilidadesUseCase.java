package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;

/**
 * Catálogo de habilidades para los selectores de perfiles y vacantes (docs/contrato-api.md, 4.1).
 */
public interface ListarHabilidadesUseCase {

    /** Todo el catálogo, o solo una categoría si no es null, en el orden de {@link Habilidad#ORDEN_CATALOGO}. */
    List<Habilidad> listar(CategoriaHabilidad categoria);
}
