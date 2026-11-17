package com.primerempleo.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostulacionJpaRepository extends JpaRepository<PostulacionEntity, UUID> {

    @Query("select p.vacanteId from PostulacionEntity p where p.candidatoId = :candidatoId")
    List<UUID> idsVacantesDe(@Param("candidatoId") UUID candidatoId);
}
