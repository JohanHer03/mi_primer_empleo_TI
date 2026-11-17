package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.FiltroVacantes;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Consulta de la búsqueda de vacantes con filtros opcionales. Se arma con Criteria porque cada filtro puede faltar
 * (un parámetro null en "is null" falla en PostgreSQL).
 */
final class VacanteEspecificaciones {

    private VacanteEspecificaciones() {
    }

    /**
     * Vacantes ABIERTA con fecha de cierre desde {@code hoy} que cumplen los filtros. Trae las habilidades en la misma
     * consulta (fetch join) para calcular la compatibilidad sin una consulta por vacante.
     */
    static Specification<VacanteEntity> abiertasCon(FiltroVacantes filtro, LocalDate hoy) {
        return (raiz, consulta, cb) -> {
            // En una consulta de conteo no se puede hacer fetch
            if (consulta.getResultType() != Long.class && consulta.getResultType() != long.class) {
                raiz.fetch("habilidades", JoinType.LEFT);
                consulta.distinct(true);
            }
            List<Predicate> condiciones = new ArrayList<>();
            condiciones.add(cb.equal(raiz.get("estado"), EstadoVacante.ABIERTA));
            condiciones.add(cb.greaterThanOrEqualTo(raiz.get("fechaCierre"), hoy));
            if (filtro.modalidad() != null) {
                condiciones.add(cb.equal(raiz.get("modalidad"), filtro.modalidad()));
            }
            if (filtro.tipo() != null) {
                condiciones.add(cb.equal(raiz.get("tipo"), filtro.tipo()));
            }
            if (filtro.ciudad() != null) {
                condiciones.add(cb.equal(cb.lower(cb.trim(raiz.get("ciudad"))), filtro.ciudad().toLowerCase()));
            }
            if (filtro.habilidadId() != null) {
                // Subconsulta aparte: filtrar sobre el fetch join recortaría las habilidades que se cargan
                Subquery<UUID> conHabilidad = consulta.subquery(UUID.class);
                Root<VacanteEntity> otra = conHabilidad.from(VacanteEntity.class);
                Join<VacanteEntity, VacanteHabilidadEmbeddable> habilidad = otra.join("habilidades");
                conHabilidad.select(otra.get("id"))
                        .where(cb.equal(habilidad.get("habilidadId"), filtro.habilidadId()));
                condiciones.add(raiz.get("id").in(conHabilidad));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
    }
}
