package com.primerempleo.infrastructure.adapter.out.persistence;

/**
 * Fila de las consultas que unen empresas con usuarios para traer el correo de la cuenta.
 * La crea Hibernate con la expresión {@code select new ...} de {@link EmpresaJpaRepository}.
 */
public record EmpresaConCorreoFila(EmpresaEntity empresa, String email) {
}
