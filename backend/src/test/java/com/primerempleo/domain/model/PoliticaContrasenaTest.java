package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.Test;

class PoliticaContrasenaTest {

    @Test
    void deberiaAceptarUnaContrasenaDe8A72Bytes() {
        assertThatCode(() -> PoliticaContrasena.validar("Medellin2026")).doesNotThrowAnyException();
        assertThatCode(() -> PoliticaContrasena.validar("a".repeat(72))).doesNotThrowAnyException();
    }

    @Test
    void deberiaRechazarUnaContrasenaCorta() {
        assertThatThrownBy(() -> PoliticaContrasena.validar("corta"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("password: debe tener al menos 8 caracteres");
    }

    @Test
    void deberiaRechazarUnaContrasenaNula() {
        assertThatThrownBy(() -> PoliticaContrasena.validar(null))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void deberiaContarBytesYNoCaracteres() {
        // 40 caracteres pasan el @Size(max = 72) del DTO, pero son 80 bytes: BCrypt los rechazaría con un 500
        String conEnes = "ñ".repeat(40);

        assertThatThrownBy(() -> PoliticaContrasena.validar(conEnes))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("password: es demasiado larga");
    }
}
