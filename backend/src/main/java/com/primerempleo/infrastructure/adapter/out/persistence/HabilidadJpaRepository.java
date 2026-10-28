package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.CategoriaHabilidad;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HabilidadJpaRepository extends JpaRepository<HabilidadEntity, UUID> {

    List<HabilidadEntity> findByCategoria(CategoriaHabilidad categoria);

    /** Usa lower(nombre), la misma expresión del índice único uq_habilidades_nombre (V4). */
    @Query("select count(h) > 0 from HabilidadEntity h where lower(h.nombre) = lower(:nombre)")
    boolean existePorNombre(@Param("nombre") String nombre);
}
