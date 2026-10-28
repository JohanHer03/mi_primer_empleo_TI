package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    /** Busca sin distinguir mayúsculas (índice único sobre lower(email)). */
    boolean existePorEmail(String email);

    /** Busca sin distinguir mayúsculas (índice único sobre lower(email)). */
    Optional<Usuario> buscarPorEmail(String email);

    Usuario guardar(Usuario usuario);
}
