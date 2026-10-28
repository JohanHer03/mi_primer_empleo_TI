package com.primerempleo.infrastructure.security;

import com.primerempleo.domain.model.Rol;
import java.util.UUID;

/**
 * Usuario que viene en un JWT válido. Es el principal del contexto de seguridad:
 * los controladores lo reciben con {@code @AuthenticationPrincipal UsuarioAutenticado usuario}.
 */
public record UsuarioAutenticado(UUID id, String email, Rol rol) {
}
