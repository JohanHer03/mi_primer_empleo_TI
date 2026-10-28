package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.model.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class UsuarioPersistenceAdapter implements UsuarioRepositoryPort {

    private final UsuarioJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public UsuarioPersistenceAdapter(UsuarioJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public boolean existePorEmail(String email) {
        return repositorio.existePorEmail(email);
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        return repositorio.buscarPorEmail(email).map(mapper::aDominio);
    }

    @Override
    public Usuario crear(Usuario usuario) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(usuario)));
    }
}
