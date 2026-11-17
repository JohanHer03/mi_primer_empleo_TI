package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoPostulacion;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Las consultas "con vacante" unen postulaciones, vacantes y empresas por id (no hay relaciones JPA entre las entidades).
 * El listado con y sin filtro de estado son dos consultas: un parámetro null en "is null" falla en PostgreSQL.
 */
public interface PostulacionJpaRepository extends JpaRepository<PostulacionEntity, UUID> {

    String SELECT_CON_VACANTE = "select new com.primerempleo.infrastructure.adapter.out.persistence.PostulacionConVacanteFila("
            + "p, v.titulo, e.id, e.razonSocial, v.modalidad, v.ciudad, v.estado, v.fechaCierre) "
            + "from PostulacionEntity p join VacanteEntity v on v.id = p.vacanteId "
            + "join EmpresaEntity e on e.id = v.empresaId ";

    @Query("select p.vacanteId from PostulacionEntity p where p.candidatoId = :candidatoId")
    List<UUID> idsVacantesDe(@Param("candidatoId") UUID candidatoId);

    boolean existsByCandidatoIdAndVacanteId(UUID candidatoId, UUID vacanteId);

    Optional<PostulacionEntity> findByCandidatoIdAndVacanteId(UUID candidatoId, UUID vacanteId);

    @Query(value = SELECT_CON_VACANTE + "where p.candidatoId = :candidatoId order by p.fecha desc, p.id desc",
            countQuery = "select count(p) from PostulacionEntity p where p.candidatoId = :candidatoId")
    Page<PostulacionConVacanteFila> listarDeCandidato(@Param("candidatoId") UUID candidatoId, Pageable pagina);

    @Query(value = SELECT_CON_VACANTE
            + "where p.candidatoId = :candidatoId and p.estado = :estado order by p.fecha desc, p.id desc",
            countQuery = "select count(p) from PostulacionEntity p where p.candidatoId = :candidatoId and p.estado = :estado")
    Page<PostulacionConVacanteFila> listarDeCandidatoPorEstado(@Param("candidatoId") UUID candidatoId,
                                                               @Param("estado") EstadoPostulacion estado,
                                                               Pageable pagina);
}
