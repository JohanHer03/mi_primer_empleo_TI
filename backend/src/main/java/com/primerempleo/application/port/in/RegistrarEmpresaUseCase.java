package com.primerempleo.application.port.in;

/**
 * HU-06: una empresa crea su cuenta. Queda sin verificar: puede iniciar sesión pero no publicar.
 */
public interface RegistrarEmpresaUseCase {

    /** Crea el usuario EMPRESA y su perfil, y devuelve el JWT de la sesión. */
    String registrar(RegistrarEmpresaComando comando);

    record RegistrarEmpresaComando(
            String email,
            String password,
            String razonSocial,
            String nit,
            String sector,
            String ciudad,
            String sitioWeb) {

        /** Sin la contraseña, por si el comando termina en un log. */
        @Override
        public String toString() {
            return "RegistrarEmpresaComando[email=" + email + ", nit=" + nit + "]";
        }
    }
}
