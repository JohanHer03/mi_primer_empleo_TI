package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.IniciarSesionUseCase.IniciarSesionComando;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.GeneradorTokenPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.CredencialesInvalidasException;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InicioSesionServiceTest {

    private static final String HASH = "$2a$10$hash";

    @Mock
    private UsuarioRepositoryPort usuarios;
    @Mock
    private CifradorContrasenaPort cifrador;
    @Mock
    private GeneradorTokenPort generadorToken;

    private InicioSesionService servicio;

    @BeforeEach
    void preparar() {
        servicio = new InicioSesionService(usuarios, cifrador, generadorToken);
    }

    @Test
    void deberiaDevolverElTokenConCredencialesCorrectas() {
        Usuario usuario = usuario(true);
        when(usuarios.buscarPorEmail("laura@udea.edu.co")).thenReturn(Optional.of(usuario));
        when(cifrador.coincide("Medellin2026", HASH)).thenReturn(true);
        when(generadorToken.generarToken(usuario)).thenReturn("token-jwt");

        String token = servicio.iniciarSesion(new IniciarSesionComando(" Laura@UdeA.edu.co ", "Medellin2026"));

        assertThat(token).isEqualTo("token-jwt");
    }

    @Test
    void deberiaResponderCredencialesInvalidasSiElCorreoNoExiste() {
        when(usuarios.buscarPorEmail("nadie@udea.edu.co")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionComando("nadie@udea.edu.co", "Medellin2026")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
        // Se cifra igual para que la respuesta tarde lo mismo que con un correo existente
        verify(cifrador).cifrar("Medellin2026");
        verify(generadorToken, never()).generarToken(any());
    }

    @Test
    void deberiaResponderElMismoMensajeSiLaContrasenaEsIncorrecta() {
        when(usuarios.buscarPorEmail("laura@udea.edu.co")).thenReturn(Optional.of(usuario(true)));
        when(cifrador.coincide("otraClave", HASH)).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionComando("laura@udea.edu.co", "otraClave")))
                .isInstanceOf(CredencialesInvalidasException.class)
                .hasMessage("Correo o contraseña incorrectos");
    }

    @Test
    void deberiaRechazarUnaCuentaDesactivada() {
        when(usuarios.buscarPorEmail("laura@udea.edu.co")).thenReturn(Optional.of(usuario(false)));
        when(cifrador.coincide("Medellin2026", HASH)).thenReturn(true);

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionComando("laura@udea.edu.co", "Medellin2026")))
                .isInstanceOf(CuentaDesactivadaException.class)
                .hasMessage("Tu cuenta está desactivada");
        verify(generadorToken, never()).generarToken(any());
    }

    @Test
    void noDeberiaRevelarQueLaCuentaEstaDesactivadaSinLaContrasenaCorrecta() {
        when(usuarios.buscarPorEmail("laura@udea.edu.co")).thenReturn(Optional.of(usuario(false)));
        when(cifrador.coincide("otraClave", HASH)).thenReturn(false);

        assertThatThrownBy(() -> servicio.iniciarSesion(new IniciarSesionComando("laura@udea.edu.co", "otraClave")))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    private static Usuario usuario(boolean activo) {
        return new Usuario(UUID.randomUUID(), "laura@udea.edu.co", HASH, Rol.CANDIDATO, activo, Instant.now());
    }
}
