package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.RegistrarCandidatoUseCase.RegistrarCandidatoComando;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaComando;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.CifradorContrasenaPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.GeneradorTokenPort;
import com.primerempleo.application.port.out.UsuarioRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistroServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarios;
    @Mock
    private CandidatoRepositoryPort candidatos;
    @Mock
    private EmpresaRepositoryPort empresas;
    @Mock
    private CifradorContrasenaPort cifrador;
    @Mock
    private GeneradorTokenPort generadorToken;

    private RegistroService servicio;

    @BeforeEach
    void preparar() {
        servicio = new RegistroService(usuarios, candidatos, empresas, cifrador, generadorToken);
    }

    @Test
    void deberiaRegistrarAlCandidatoYDevolverSuToken() {
        when(cifrador.cifrar("Medellin2026")).thenReturn("$2a$10$hash");
        when(generadorToken.generarToken(any())).thenReturn("token-jwt");

        String token = servicio.registrar(candidato("Laura.Gomez@UdeA.edu.co", EstadoAcademico.ESTUDIANTE, 9));

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        ArgumentCaptor<Candidato> candidato = ArgumentCaptor.forClass(Candidato.class);
        verify(usuarios).crear(usuario.capture());
        verify(candidatos).crear(candidato.capture());
        assertThat(token).isEqualTo("token-jwt");
        assertThat(usuario.getValue().email()).isEqualTo("laura.gomez@udea.edu.co");
        assertThat(usuario.getValue().passwordHash()).isEqualTo("$2a$10$hash");
        assertThat(usuario.getValue().rol()).isEqualTo(Rol.CANDIDATO);
        assertThat(candidato.getValue().usuarioId()).isEqualTo(usuario.getValue().id());
        assertThat(candidato.getValue().semestre()).isEqualTo(9);
    }

    @Test
    void deberiaRechazarElRegistroConUnCorreoRepetido() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");
        when(usuarios.existePorEmail("laura@udea.edu.co")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(candidato("LAURA@udea.edu.co", EstadoAcademico.EGRESADO, null)))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe una cuenta con ese correo");
        verify(usuarios, never()).crear(any());
        verify(candidatos, never()).crear(any());
    }

    @Test
    void deberiaRechazarUnEstudianteSinSemestreSinGuardarNada() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");

        assertThatThrownBy(() -> servicio.registrar(candidato("laura@udea.edu.co", EstadoAcademico.ESTUDIANTE, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: es obligatorio para estudiantes");
        verify(usuarios, never()).crear(any());
        verify(candidatos, never()).crear(any());
    }

    @Test
    void deberiaRegistrarLaEmpresaSinVerificarYConElNitNormalizado() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");
        when(generadorToken.generarToken(any())).thenReturn("token-jwt");

        String token = servicio.registrar(empresa("talento@softwaremedellin.co", "900.123.456-7"));

        ArgumentCaptor<Usuario> usuario = ArgumentCaptor.forClass(Usuario.class);
        ArgumentCaptor<Empresa> empresa = ArgumentCaptor.forClass(Empresa.class);
        verify(usuarios).crear(usuario.capture());
        verify(empresas).crear(empresa.capture());
        verify(empresas).existePorNit("9001234567");
        assertThat(token).isEqualTo("token-jwt");
        assertThat(usuario.getValue().rol()).isEqualTo(Rol.EMPRESA);
        assertThat(empresa.getValue().nit()).isEqualTo("9001234567");
        assertThat(empresa.getValue().verificada()).isFalse();
        assertThat(empresa.getValue().usuarioId()).isEqualTo(usuario.getValue().id());
    }

    @Test
    void deberiaRechazarLaEmpresaConUnNitRepetido() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");
        when(empresas.existePorNit("9001234567")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(empresa("otra@empresa.co", "9001234567")))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe una empresa registrada con ese NIT");
        verify(usuarios, never()).crear(any());
        verify(empresas, never()).crear(any());
    }

    @Test
    void deberiaRechazarLaEmpresaConUnCorreoRepetido() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");
        when(usuarios.existePorEmail("talento@softwaremedellin.co")).thenReturn(true);

        assertThatThrownBy(() -> servicio.registrar(empresa("talento@softwaremedellin.co", "9001234567")))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe una cuenta con ese correo");
        verify(empresas, never()).crear(any());
    }

    @Test
    void deberiaRechazarUnNitConFormatoInvalidoSinGuardarNada() {
        when(cifrador.cifrar(any())).thenReturn("$2a$10$hash");

        assertThatThrownBy(() -> servicio.registrar(empresa("talento@softwaremedellin.co", "NIT-123")))
                .isInstanceOf(DatosInvalidosException.class);
        verify(usuarios, never()).crear(any());
    }

    @Test
    void deberiaRechazarUnaContrasenaDeMasDe72BytesSinCifrarla() {
        RegistrarCandidatoComando base = candidato("laura@udea.edu.co", EstadoAcademico.EGRESADO, null);
        RegistrarCandidatoComando conTildes = new RegistrarCandidatoComando(base.email(), "á".repeat(40),
                base.nombres(), base.apellidos(), base.estadoAcademico(), base.semestre(), base.universidad(),
                base.programa(), base.ciudad());

        assertThatThrownBy(() -> servicio.registrar(conTildes))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("password:");
        verify(cifrador, never()).cifrar(any());
        verify(usuarios, never()).crear(any());
    }

    private static RegistrarCandidatoComando candidato(String email, EstadoAcademico estado, Integer semestre) {
        return new RegistrarCandidatoComando(email, "Medellin2026", "Laura", "Gómez Restrepo", estado, semestre,
                "Universidad de Antioquia", "Ingeniería de Sistemas", "Medellín");
    }

    private static RegistrarEmpresaComando empresa(String email, String nit) {
        return new RegistrarEmpresaComando(email, "Medellin2026", "Software Medellín S.A.S.", nit,
                "Desarrollo de software", "Medellín", "https://softwaremedellin.co");
    }
}
