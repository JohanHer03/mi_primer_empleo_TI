package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class HabilidadPersistenceAdapter implements HabilidadRepositoryPort {

    private final HabilidadJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public HabilidadPersistenceAdapter(HabilidadJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public List<Habilidad> listar(CategoriaHabilidad categoria) {
        List<HabilidadEntity> entidades = categoria == null
                ? repositorio.findAll()
                : repositorio.findByCategoria(categoria);
        return entidades.stream().map(mapper::aDominio).toList();
    }

    @Override
    public boolean existePorNombre(String nombre) {
        return repositorio.existePorNombre(nombre);
    }

    @Override
    public Habilidad crear(Habilidad habilidad) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(habilidad)));
    }
}
