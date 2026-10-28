package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.model.Empresa;
import org.springframework.stereotype.Component;

@Component
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public EmpresaPersistenceAdapter(EmpresaJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public boolean existePorNit(String nit) {
        return repositorio.existsByNit(nit);
    }

    @Override
    public Empresa crear(Empresa empresa) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(empresa)));
    }
}
