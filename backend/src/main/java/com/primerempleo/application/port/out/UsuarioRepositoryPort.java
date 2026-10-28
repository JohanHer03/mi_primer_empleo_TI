package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Usuario;
import java.util.Optional;

public interface UsuarioRepositoryPort {

    /** Busca sin distinguir mayúsculas (índice único sobre lower(email)). */
    boolean existePorEmail(String email);

    /** Busca sin distinguir mayúsculas (índice único sobre lower(email)). */
    Optional<Usuario> buscarPorEmail(String email);

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     * Para editar, agregar un método que cargue la entidad con findById y le copie los cambios.
     */
    Usuario crear(Usuario usuario);
}
