package com.primerempleo.domain.model;

import java.util.Objects;

/**
 * Postulación vista por la empresa dueña de la vacante (HU-08, docs/contrato-api.md 7.1): con el perfil del candidato
 * y sus habilidades de hoy. La compatibilidad es la guardada al postularse (regla 6).
 */
public record Postulante(Postulacion postulacion, PerfilCandidato candidato) {

    public Postulante {
        Objects.requireNonNull(postulacion, "postulacion");
        Objects.requireNonNull(candidato, "candidato");
    }
}
