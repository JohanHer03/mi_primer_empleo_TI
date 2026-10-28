package com.primerempleo.application.port.in;

/**
 * Crea la cuenta ADMIN inicial. Los administradores no se registran por la API (docs/contrato-api.md, 2.3).
 */
public interface CrearAdministradorUseCase {

    /** Crea el administrador si no existe una cuenta con ese correo. Devuelve true si lo creó. */
    boolean crearSiNoExiste(String email, String password);
}
