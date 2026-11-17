package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PostulacionPersistenceAdapter implements PostulacionRepositoryPort {

    private final PostulacionJpaRepository repositorio;

    public PostulacionPersistenceAdapter(PostulacionJpaRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Set<UUID> idsVacantesPostuladas(UUID candidatoId) {
        return new HashSet<>(repositorio.idsVacantesDe(candidatoId));
    }
}
