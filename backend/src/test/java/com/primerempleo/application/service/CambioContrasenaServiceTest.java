package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.CambiarContrasenaUseCase.CambiarContrasenaComando;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CambioContrasenaServiceTest {

    private static final String HASH_ACTUAL = "$2a$10$actual";
    private static final String HASH_NUEVO = "$2a$10$nuevo";
    private static final Usuario USUARIO = new Usuario(UUID.randomUUID(), "laura@udea.edu.co", HASH_ACTUAL,
            Rol.CANDIDATO, true, Instant.parse("2026-10-27T00:00:00Z"));

    @Mock
    private UsuarioRepositoryPort usuarios;
    @Mock
    private CifradorContrasenaPort cifrador;

    private CambioContrasenaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new CambioContrasenaService(usuarios, cifrador);
    }

    @Test
    void deberiaGuardarElHashDeLaNuevaContrasena() {
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.of(USUARIO));
        when(cifrador.coincide("Medellin2026", HASH_ACTUAL)).thenReturn(true);
        when(cifrador.cifrar("Envigado2027")).thenReturn(HASH_NUEVO);

        servicio.cambiarContrasena(comando("Medellin2026", "Envigado2027"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).actualizarContrasena(guardado.capture());
        assertThat(guardado.getValue().passwordHash()).isEqualTo(HASH_NUEVO);
        assertThat(guardado.getValue()).usingRecursiveComparison().ignoringFields("passwordHash").isEqualTo(USUARIO);
    }

    @Test
    void deberiaRechazarSiLaContrasenaActualNoEsCorrecta() {
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.of(USUARIO));
        when(cifrador.coincide("otraClave1", HASH_ACTUAL)).thenReturn(false);

        assertThatThrownBy(() -> servicio.cambiarContrasena(comando("otraClave1", "Envigado2027")))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("contrasenaActual: no es correcta");
        verify(usuarios, never()).actualizarContrasena(any());
    }

    @Test
    void deberiaRevisarLaActualAntesQueLaPoliticaDeLaNueva() {
        // Quien no conoce la actual no debe aprender nada de la nueva
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.of(USUARIO));
        when(cifrador.coincide("otraClave1", HASH_ACTUAL)).thenReturn(false);

        assertThatThrownBy(() -> servicio.cambiarContrasena(comando("otraClave1", "corta")))
                .hasMessage("contrasenaActual: no es correcta");
    }

    @Test
    void deberiaAplicarLaPoliticaALaNuevaEnSuCampo() {
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.of(USUARIO));
        when(cifrador.coincide("Medellin2026", HASH_ACTUAL)).thenReturn(true);

        // 40 eñes son 80 bytes: pasan el @Size del DTO pero no BCrypt
        assertThatThrownBy(() -> servicio.cambiarContrasena(comando("Medellin2026", "ñ".repeat(40))))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("contrasenaNueva: es demasiado larga");
        verify(cifrador, never()).cifrar(anyString());
    }

    @Test
    void deberiaRechazarUnaNuevaIgualALaActual() {
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.of(USUARIO));
        when(cifrador.coincide("Medellin2026", HASH_ACTUAL)).thenReturn(true);

        assertThatThrownBy(() -> servicio.cambiarContrasena(comando("Medellin2026", "Medellin2026")))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("contrasenaNueva: debe ser distinta de la actual");
        verify(usuarios, never()).actualizarContrasena(any());
    }

    @Test
    void deberiaResponder404SiElUsuarioDelTokenYaNoExiste() {
        when(usuarios.buscarPorId(USUARIO.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiarContrasena(comando("Medellin2026", "Envigado2027")))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El usuario no existe");
    }

    @Test
    void elComandoNoMuestraLasContrasenasEnElLog() {
        assertThat(comando("Medellin2026", "Envigado2027").toString())
                .doesNotContain("Medellin2026", "Envigado2027");
    }

    private static CambiarContrasenaComando comando(String actual, String nueva) {
        return new CambiarContrasenaComando(USUARIO.id(), actual, nueva);
    }
}
