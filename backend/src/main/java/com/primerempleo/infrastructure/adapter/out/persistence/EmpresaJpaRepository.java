package com.primerempleo.infrastructure.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaJpaRepository extends JpaRepository<EmpresaEntity, UUID> {

    boolean existsByNit(String nit);
}
