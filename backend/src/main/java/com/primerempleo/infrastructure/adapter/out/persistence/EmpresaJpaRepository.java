package com.primerempleo.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Las consultas "con correo" unen empresas y usuarios por usuario_id (no hay relación JPA entre las entidades).
 * El listado con y sin filtro son dos consultas: un parámetro null en "is null" falla en PostgreSQL.
 */
public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, UUID> {

    String SELECT_CON_CORREO = "select new com.primerempleo.infrastructure.adapter.out.persistence.EmpresaConCorreoFila(e, u.email) "
            + "from EmpresaEntity e join UsuarioEntity u on u.id = e.usuarioId ";

    boolean existsByNit(String nit);

    @Query(SELECT_CON_CORREO + "where e.id = :id")
    Optional<EmpresaConCorreoFila> buscarConCorreoPorId(@Param("id") UUID id);

    @Query(SELECT_CON_CORREO + "where e.usuarioId = :usuarioId")
    Optional<EmpresaConCorreoFila> buscarConCorreoPorUsuarioId(@Param("usuarioId") UUID usuarioId);

    @Query(value = SELECT_CON_CORREO + "order by e.fechaCreacion asc, e.id asc",
            countQuery = "select count(e) from EmpresaEntity e")
    Page<EmpresaConCorreoFila> listarConCorreo(Pageable pagina);

    @Query(value = SELECT_CON_CORREO + "where e.verificada = :verificada order by e.fechaCreacion asc, e.id asc",
            countQuery = "select count(e) from EmpresaEntity e where e.verificada = :verificada")
    Page<EmpresaConCorreoFila> listarConCorreoPorVerificada(@Param("verificada") boolean verificada, Pageable pagina);
}
