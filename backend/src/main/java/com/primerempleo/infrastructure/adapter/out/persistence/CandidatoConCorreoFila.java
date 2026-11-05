package com.primerempleo.infrastructure.adapter.out.persistence;

/**
 * Fila de la consulta que une candidatos con usuarios para traer el correo de la cuenta.
 * La crea Hibernate con la expresión {@code select new ...} de {@link CandidatoJpaRepository}.
 */
public record CandidatoConCorreoFila(CandidatoEntity candidato, String email) {
}
