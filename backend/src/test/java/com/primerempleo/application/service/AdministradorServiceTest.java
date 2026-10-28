package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarios;
    @Mock
    private CifradorContrasenaPort cifrador;

    private AdministradorService servicio;

    @BeforeEach
    void preparar() {
        servicio = new AdministradorService(usuarios, cifrador);
    }

    @Test
    void deberiaCrearElAdministradorSiNoExiste() {
        when(cifrador.cifrar("ClaveDelAdmin2026")).thenReturn("$2a$10$hash");

        boolean creado = servicio.crearSiNoExiste("Admin@PrimerEmpleo.co", "ClaveDelAdmin2026");

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).guardar(usuario.capture());
        assertThat(creado).isTrue();
        assertThat(usuario.getValue().email()).isEqualTo("admin@primerempleo.co");
        assertThat(usuario.getValue().rol()).isEqualTo(Rol.ADMIN);
        assertThat(usuario.getValue().passwordHash()).isEqualTo("$2a$10$hash");
    }

    @Test
    void noDeberiaHacerNadaSiElCorreoYaExiste() {
        when(usuarios.existePorEmail("admin@primerempleo.co")).thenReturn(true);

        boolean creado = servicio.crearSiNoExiste("admin@primerempleo.co", "ClaveDelAdmin2026");

        assertThat(creado).isFalse();
        verify(usuarios, never()).guardar(any());
        verify(cifrador, never()).cifrar(any());
    }

    @Test
    void deberiaRechazarUnaContrasenaCorta() {
        assertThatThrownBy(() -> servicio.crearSiNoExiste("admin@primerempleo.co", "corta"))
                .isInstanceOf(DatosInvalidosException.class);
        verify(usuarios, never()).guardar(any());
    }
}
