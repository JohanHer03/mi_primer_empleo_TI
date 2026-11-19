package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Usuario;
import java.util.Optional;
import java.util.UUID;
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
    public Optional<Usuario> buscarPorId(UUID id) {
        return repositorio.findById(id).map(mapper::aDominio);
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

    /** Con findById la entidad ya no es nueva: el save hace UPDATE y no INSERT (ver EntidadConIdAsignado). */
    @Override
    public Usuario actualizarContrasena(Usuario usuario) {
        UsuarioEntity entidad = repositorio.findById(usuario.id())
                .orElseThrow(RecursoNoEncontradoException::usuario);
        entidad.setPasswordHash(usuario.passwordHash());
        return mapper.aDominio(repositorio.save(entidad));
    }
}
