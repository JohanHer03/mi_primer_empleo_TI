package com.primerempleo.domain.model;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Cuenta para iniciar sesión. Un usuario CANDIDATO tiene un Candidato y uno EMPRESA una Empresa;
 * el ADMIN no tiene perfil.
 */
public record Usuario(UUID id, String email, String passwordHash, Rol rol, boolean activo, Instant fechaCreacion) {

    public Usuario {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(rol, "rol");
        Objects.requireNonNull(fechaCreacion, "fechaCreacion");
        email = normalizarEmail(email);
    }

    /** Cuenta nueva y activa. La contraseña ya debe venir cifrada. */
    public static Usuario nuevo(String email, String passwordHash, Rol rol) {
        return new Usuario(UUID.randomUUID(), email, passwordHash, rol, true, Instant.now());
    }

    /**
     * El correo se guarda sin espacios y en minúsculas: Ana@Correo.com y ana@correo.com son la misma cuenta
     * (índice único sobre lower(email) de la migración V3).
     */
    public static String normalizarEmail(String email) {
        return Textos.obligatorio(email, "email").toLowerCase(Locale.ROOT);
    }

    /** Sin el hash de la contraseña, por si el usuario termina en un log. */
    @Override
    public String toString() {
        return "Usuario[id=" + id + ", email=" + email + ", rol=" + rol + ", activo=" + activo + "]";
    }
}
