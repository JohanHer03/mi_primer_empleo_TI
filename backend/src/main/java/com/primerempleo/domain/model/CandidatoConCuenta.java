package com.primerempleo.domain.model;

import java.util.Objects;

/**
 * Candidato junto con el correo de su cuenta, que muestra el perfil.
 */
public record CandidatoConCuenta(Candidato candidato, String email) {

    public CandidatoConCuenta {
        Objects.requireNonNull(candidato, "candidato");
        Objects.requireNonNull(email, "email");
    }

    public CandidatoConCuenta conCandidato(Candidato actualizado) {
        return new CandidatoConCuenta(actualizado, email);
    }
}
