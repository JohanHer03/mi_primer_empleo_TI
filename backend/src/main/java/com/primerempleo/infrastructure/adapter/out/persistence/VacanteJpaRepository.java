package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoVacante;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * El listado con y sin filtro de estado son dos métodos: un parámetro null en "is null" falla en PostgreSQL.
 */
public interface VacanteJpaRepository extends JpaRepository<VacanteEntity, UUID> {

    Page<VacanteEntity> findByEmpresaId(UUID empresaId, Pageable pagina);

    Page<VacanteEntity> findByEmpresaIdAndEstado(UUID empresaId, EstadoVacante estado, Pageable pagina);

    /**
     * Pares [vacante_id, total] de las vacantes que tienen postulaciones. Es SQL nativo porque las postulaciones
     * todavía no tienen entidad JPA (semana 4); con ella puede pasar a JPQL.
     */
    @Query(value = "select p.vacante_id, count(*) from postulaciones p where p.vacante_id in (:ids) group by p.vacante_id",
            nativeQuery = true)
    List<Object[]> contarPostulaciones(@Param("ids") Collection<UUID> ids);
}
