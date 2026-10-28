package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.domain.model.Candidato;
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
}
