package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoVacante;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * La búsqueda del candidato, con varios filtros opcionales, usa {@link VacanteEspecificaciones}.
 * El listado con y sin filtro de estado son dos métodos: un parámetro null en "is null" falla en PostgreSQL.
 */
public interface VacanteJpaRepository extends JpaRepository<VacanteEntity, UUID>,
        JpaSpecificationExecutor<VacanteEntity> {

    Page<VacanteEntity> findByEmpresaId(UUID empresaId, Pageable pagina);

    Page<VacanteEntity> findByEmpresaIdAndEstado(UUID empresaId, EstadoVacante estado, Pageable pagina);

    /** Pares [vacanteId, total] de las vacantes que tienen postulaciones. */
    @Query("select p.vacanteId, count(p) from PostulacionEntity p where p.vacanteId in :ids group by p.vacanteId")
    List<Object[]> contarPostulaciones(@Param("ids") Collection<UUID> ids);
}
