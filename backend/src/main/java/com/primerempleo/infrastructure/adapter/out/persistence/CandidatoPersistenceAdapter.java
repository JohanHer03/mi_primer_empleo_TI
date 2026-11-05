package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CandidatoPersistenceAdapter implements CandidatoRepositoryPort {

    private final CandidatoJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public CandidatoPersistenceAdapter(CandidatoJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Candidato crear(Candidato candidato) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(candidato)));
    }

    @Override
    public Candidato actualizar(Candidato candidato) {
        CandidatoEntity entidad = repositorio.findById(candidato.id())
                .orElseThrow(RecursoNoEncontradoException::candidato);
        mapper.copiar(candidato, entidad);
        return mapper.aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<CandidatoConCuenta> buscarConCuentaPorUsuarioId(UUID usuarioId) {
        return repositorio.buscarConCorreoPorUsuarioId(usuarioId)
                .map(fila -> new CandidatoConCuenta(mapper.aDominio(fila.candidato()), fila.email()));
    }
}
