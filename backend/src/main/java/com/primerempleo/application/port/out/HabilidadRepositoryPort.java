package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;

public interface HabilidadRepositoryPort {

    /** Todas las habilidades, o solo las de la categoría si no es null. Sin orden garantizado. */
    List<Habilidad> listar(CategoriaHabilidad categoria);

    /** Busca sin distinguir mayúsculas (índice único sobre lower(nombre)). */
    boolean existePorNombre(String nombre);

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     */
    Habilidad crear(Habilidad habilidad);
}
