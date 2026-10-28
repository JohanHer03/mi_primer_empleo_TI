package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EstadoAcademico;

/**
 * HU-01: un estudiante o egresado crea su cuenta y queda con la sesión iniciada.
 */
public interface RegistrarCandidatoUseCase {

    /** Crea el usuario CANDIDATO y su perfil, y devuelve el JWT de la sesión. */
    String registrar(RegistrarCandidatoComando comando);

    record RegistrarCandidatoComando(
            String email,
            String password,
            String nombres,
            String apellidos,
            EstadoAcademico estadoAcademico,
            Integer semestre,
            String universidad,
            String programa,
            String ciudad) {

        /** Sin la contraseña, por si el comando termina en un log. */
        @Override
        public String toString() {
            return "RegistrarCandidatoComando[email=" + email + "]";
        }
    }
}
