package com.primerempleo.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * La consulta "con correo" une candidatos y usuarios por usuario_id (no hay relación JPA entre las entidades).
 */
public interface CandidatoJpaRepository extends JpaRepository<CandidatoEntity, UUID> {

    @Query("select new com.primerempleo.infrastructure.adapter.out.persistence.CandidatoConCorreoFila(c, u.email) "
            + "from CandidatoEntity c join UsuarioEntity u on u.id = c.usuarioId where c.usuarioId = :usuarioId")
    Optional<CandidatoConCorreoFila> buscarConCorreoPorUsuarioId(@Param("usuarioId") UUID usuarioId);
}
