package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VerificacionEmpresaServiceTest {

    @Mock
    private EmpresaRepositoryPort empresas;

    private VerificacionEmpresaService servicio;

    @BeforeEach
    void preparar() {
        servicio = new VerificacionEmpresaService(empresas);
    }

    @Test
    void deberiaListarConElFiltroYLaPaginaPedidos() {
        SolicitudPagina solicitud = new SolicitudPagina(0, 10);
        Pagina<EmpresaConCuenta> pagina = new Pagina<>(List.of(empresa(false)), 0, 10, 1, 1);
        when(empresas.listar(false, solicitud)).thenReturn(pagina);

        assertThat(servicio.listar(false, solicitud)).isEqualTo(pagina);
    }

    @Test
    void deberiaVerificarUnaEmpresaPendiente() {
        EmpresaConCuenta pendiente = empresa(false);
        when(empresas.buscarConCuentaPorId(pendiente.empresa().id())).thenReturn(Optional.of(pendiente));
        when(empresas.actualizar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        EmpresaConCuenta verificada = servicio.verificar(pendiente.empresa().id());

        assertThat(verificada.empresa().verificada()).isTrue();
        assertThat(verificada.email()).isEqualTo(pendiente.email());
    }

    @Test
    void deberiaSerIdempotenteConUnaEmpresaYaVerificada() {
        EmpresaConCuenta yaVerificada = empresa(true);
        when(empresas.buscarConCuentaPorId(yaVerificada.empresa().id())).thenReturn(Optional.of(yaVerificada));

        assertThat(servicio.verificar(yaVerificada.empresa().id())).isEqualTo(yaVerificada);
        verify(empresas, never()).actualizar(any());
    }

    @Test
    void deberiaResponderNoEncontradoSiLaEmpresaNoExiste() {
        UUID inexistente = UUID.randomUUID();
        when(empresas.buscarConCuentaPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.verificar(inexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La empresa no existe");
    }

    private static EmpresaConCuenta empresa(boolean verificada) {
        Empresa empresa = Empresa.nueva(UUID.randomUUID(), "Software Medellín S.A.S.", "9001234567", null, null, null);
        return new EmpresaConCuenta(verificada ? empresa.verificar() : empresa, "talento@softwaremedellin.co",
                Instant.parse("2026-10-27T15:00:00Z"));
    }
}
