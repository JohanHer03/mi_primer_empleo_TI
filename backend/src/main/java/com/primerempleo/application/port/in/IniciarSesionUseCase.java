package com.primerempleo.application.port.in;

/**
 * Inicio de sesión de cualquier rol con correo y contraseña.
 */
public interface IniciarSesionUseCase {

    /** Devuelve el JWT si las credenciales son correctas y la cuenta está activa. */
    String iniciarSesion(IniciarSesionComando comando);

    record IniciarSesionComando(String email, String password) {

        /** Sin la contraseña, por si el comando termina en un log. */
        @Override
        public String toString() {
            return "IniciarSesionComando[email=" + email + "]";
        }
    }
}
