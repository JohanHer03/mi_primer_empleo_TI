package com.primerempleo.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class CifradorContrasenaBcryptTest {

    /** Costo mínimo de BCrypt para que la prueba sea rápida; el comportamiento es el mismo. */
    private final CifradorContrasenaBcrypt cifrador = new CifradorContrasenaBcrypt(new BCryptPasswordEncoder(4));

    @Test
    void deberiaCifrarYReconocerLaMismaContrasena() {
        String hash = cifrador.cifrar("Medellin2026");

        assertThat(cifrador.coincide("Medellin2026", hash)).isTrue();
        assertThat(cifrador.coincide("otraClave", hash)).isFalse();
    }

    @Test
    void noDeberiaFallarAlCompararUnaContrasenaDeMasDe72Bytes() {
        // En el login no hay validación de largo: comparar debe responder false, no lanzar excepción
        String conEnes = "ñ".repeat(40);
        String hash = cifrador.cifrar("Medellin2026");

        assertThat(cifrador.coincide(conEnes, hash)).isFalse();
        assertThatCode(() -> cifrador.simularComparacion(conEnes)).doesNotThrowAnyException();
    }
}
