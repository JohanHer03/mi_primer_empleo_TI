package com.primerempleo.infrastructure.security;

import com.primerempleo.application.port.out.GeneradorTokenPort;
import com.primerempleo.domain.model.Usuario;
import org.springframework.stereotype.Component;

/** Adaptador del puerto de tokens con JwtService. */
@Component
public class GeneradorTokenJwt implements GeneradorTokenPort {

    private final JwtService jwtService;

    public GeneradorTokenJwt(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public String generarToken(Usuario usuario) {
        return jwtService.generarToken(usuario.id(), usuario.email(), usuario.rol());
    }
}
