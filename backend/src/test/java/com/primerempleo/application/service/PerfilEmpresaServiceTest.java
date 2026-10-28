package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.PerfilEmpresaUseCase.ActualizarPerfilEmpresaComando;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
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
class PerfilEmpresaServiceTest {

    private static final UUID USUARIO = UUID.randomUUID();

    @Mock
    private EmpresaRepositoryPort empresas;

    private PerfilEmpresaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PerfilEmpresaService(empresas);
    }

    @Test
    void deberiaDevolverElPerfilDeLaEmpresaDelUsuario() {
        EmpresaConCuenta perfil = perfil();
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.of(perfil));

        assertThat(servicio.consultar(USUARIO)).isEqualTo(perfil);
    }

    @Test
    void deberiaResponderNoEncontradoSiElUsuarioNoTieneEmpresa() {
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar(USUARIO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La empresa no existe");
    }

    @Test
    void deberiaActualizarLosDatosEditablesYConservarCorreoYNit() {
        EmpresaConCuenta actual = perfil();
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.of(actual));
        when(empresas.actualizar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        EmpresaConCuenta editada = servicio.actualizar(new ActualizarPerfilEmpresaComando(
                USUARIO, "Software Medellín S.A.S. BIC", "Fintech", "Envigado", "https://smbic.co"));

        ArgumentCaptor<Empresa> guardada = ArgumentCaptor.forClass(Empresa.class);
        verify(empresas).actualizar(guardada.capture());
        assertThat(guardada.getValue().razonSocial()).isEqualTo("Software Medellín S.A.S. BIC");
        assertThat(guardada.getValue().nit()).isEqualTo("9001234567");
        assertThat(editada.email()).isEqualTo("talento@softwaremedellin.co");
        assertThat(editada.empresa().ciudad()).isEqualTo("Envigado");
    }

    @Test
    void deberiaRechazarUnaRazonSocialVaciaSinGuardar() {
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.of(perfil()));

        assertThatThrownBy(() -> servicio.actualizar(new ActualizarPerfilEmpresaComando(USUARIO, " ", null, null, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("razonSocial: es obligatorio");
        verify(empresas, never()).actualizar(any());
    }

    private static EmpresaConCuenta perfil() {
        Empresa empresa = Empresa.nueva(USUARIO, "Software Medellín S.A.S.", "9001234567",
                "Desarrollo de software", "Medellín", "https://softwaremedellin.co");
        return new EmpresaConCuenta(empresa, "talento@softwaremedellin.co", Instant.parse("2026-10-27T15:00:00Z"));
    }
}
