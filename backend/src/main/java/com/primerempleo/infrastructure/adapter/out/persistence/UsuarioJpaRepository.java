package com.primerempleo.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Las búsquedas por correo usan lower(email), la misma expresión del índice único uq_usuarios_email (V3),
 * para que PostgreSQL pueda usar ese índice.
 */
public interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

    @Query("select count(u) > 0 from UsuarioEntity u where lower(u.email) = lower(:email)")
    boolean existePorEmail(@Param("email") String email);

    @Query("select u from UsuarioEntity u where lower(u.email) = lower(:email)")
    Optional<UsuarioEntity> buscarPorEmail(@Param("email") String email);
}
