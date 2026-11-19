package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.CambiarContrasenaUseCase;
import com.primerempleo.application.port.in.CambiarContrasenaUseCase.CambiarContrasenaComando;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cuenta del usuario autenticado, de cualquier rol (docs/contrato-api.md, 8.1).
 * No va bajo /api/auth/** porque esa ruta es pública.
 */
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("isAuthenticated()")
public class UsuarioController {

    private final CambiarContrasenaUseCase cambiarContrasena;

    public UsuarioController(CambiarContrasenaUseCase cambiarContrasena) {
        this.cambiarContrasena = cambiarContrasena;
    }

    @PutMapping("/me/contrasena")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarMiContrasena(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                    @Valid @RequestBody CambiarContrasenaPeticion peticion) {
        cambiarContrasena.cambiarContrasena(
                new CambiarContrasenaComando(usuario.id(), peticion.contrasenaActual(), peticion.contrasenaNueva()));
    }
}
