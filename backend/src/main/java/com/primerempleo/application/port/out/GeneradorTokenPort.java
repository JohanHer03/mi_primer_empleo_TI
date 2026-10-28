package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Usuario;

/**
 * Emisión del token de sesión. Lo implementa infrastructure.security con JWT.
 */
public interface GeneradorTokenPort {

    String generarToken(Usuario usuario);
}
