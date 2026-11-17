package com.primerempleo.application.port.out;

import java.util.Set;
import java.util.UUID;

public interface PostulacionRepositoryPort {

    /** Ids de las vacantes a las que el candidato ya se postuló (regla 1). */
    Set<UUID> idsVacantesPostuladas(UUID candidatoId);
}
