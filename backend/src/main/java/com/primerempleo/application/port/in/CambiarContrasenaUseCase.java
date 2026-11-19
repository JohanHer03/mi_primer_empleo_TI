package com.primerempleo.application.port.in;

import java.util.UUID;

/**
 * Cambio de contraseña del usuario autenticado, de cualquier rol (docs/contrato-api.md, 8.1).
 */
public interface CambiarContrasenaUseCase {

    void cambiarContrasena(CambiarContrasenaComando comando);

    record CambiarContrasenaComando(UUID usuarioId, String contrasenaActual, String contrasenaNueva) {

        /** Sin las contraseñas, por si el comando termina en un log. */
        @Override
        public String toString() {
            return "CambiarContrasenaComando[usuarioId=" + usuarioId + "]";
        }
    }
}
