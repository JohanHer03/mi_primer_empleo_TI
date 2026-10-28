package com.primerempleo.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Empresa junto con los datos de su cuenta que muestran el perfil y el listado del administrador:
 * el correo de inicio de sesión y la fecha de registro.
 */
public record EmpresaConCuenta(Empresa empresa, String email, Instant fechaCreacion) {

    public EmpresaConCuenta {
        Objects.requireNonNull(empresa, "empresa");
        Objects.requireNonNull(email, "email");
    }

    public EmpresaConCuenta conEmpresa(Empresa actualizada) {
        return new EmpresaConCuenta(actualizada, email, fechaCreacion);
    }
}
