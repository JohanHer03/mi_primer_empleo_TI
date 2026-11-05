package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface HabilidadRepositoryPort {

    /** Todas las habilidades, o solo las de la categoría si no es null. Sin orden garantizado. */
    List<Habilidad> listar(CategoriaHabilidad categoria);

    /** Las habilidades con esos ids que existan; las que no están en el catálogo no aparecen. */
    List<Habilidad> buscarPorIds(Collection<UUID> ids);

    /** Busca sin distinguir mayúsculas (índice único sobre lower(nombre)). */
    boolean existePorNombre(String nombre);

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     */
    Habilidad crear(Habilidad habilidad);
}
