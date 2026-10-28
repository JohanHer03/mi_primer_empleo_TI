package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;

/**
 * HU-10: el administrador agrega habilidades al catálogo.
 */
public interface CrearHabilidadUseCase {

    Habilidad crear(CrearHabilidadComando comando);

    record CrearHabilidadComando(String nombre, CategoriaHabilidad categoria) {
    }
}
