package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import org.junit.jupiter.api.Test;

class UsuarioTest {

    @Test
    void deberiaCrearUnaCuentaActivaConElCorreoNormalizado() {
        Usuario usuario = Usuario.nuevo("  Laura.Gomez@UdeA.edu.co ", "$2a$10$hash", Rol.CANDIDATO);

        assertThat(usuario.id()).isNotNull();
        assertThat(usuario.email()).isEqualTo("laura.gomez@udea.edu.co");
        assertThat(usuario.activo()).isTrue();
        assertThat(usuario.fechaCreacion()).isNotNull();
    }

    @Test
    void deberiaRechazarUnCorreoVacio() {
        assertThatThrownBy(() -> Usuario.nuevo("   ", "$2a$10$hash", Rol.CANDIDATO))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("email: es obligatorio");
    }

    @Test
    void noDeberiaMostrarElHashEnElToString() {
        Usuario usuario = Usuario.nuevo("laura@udea.edu.co", "$2a$10$hash-secreto", Rol.CANDIDATO);

        assertThat(usuario.toString()).doesNotContain("hash-secreto");
    }
}
