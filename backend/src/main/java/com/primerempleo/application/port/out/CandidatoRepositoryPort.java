package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CandidatoRepositoryPort {

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     * Para editar, usar {@link #actualizar(Candidato)}.
     */
    Candidato crear(Candidato candidato);

    /** Guarda los cambios de un candidato que ya existe; reemplaza su lista de habilidades. */
    Candidato actualizar(Candidato candidato);

    Optional<CandidatoConCuenta> buscarConCuentaPorUsuarioId(UUID usuarioId);

    /** Los candidatos con esos ids (no los de usuario) que existan, con el correo de su cuenta. */
    List<CandidatoConCuenta> buscarConCuentaPorIds(Collection<UUID> ids);
}
