package com.primerempleo.domain.model;

/**
 * Rol de un usuario. Define qué rutas puede usar (ver SecurityConfig y @PreAuthorize).
 */
public enum Rol {
    CANDIDATO,
    EMPRESA,
    ADMIN
}
