package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.TipoVacante;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import com.primerempleo.domain.model.VacanteDetalle;
import com.primerempleo.domain.model.VacanteHabilidad;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VacanteServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-04T15:00:00Z");
    private static final UUID USUARIO = UUID.randomUUID();
    private static final Habilidad JAVA = new Habilidad(UUID.randomUUID(), "Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad GIT = new Habilidad(UUID.randomUUID(), "Git", CategoriaHabilidad.HERRAMIENTA);

    @Mock
    private VacanteRepositoryPort vacantes;

    @Mock
    private EmpresaRepositoryPort empresas;

    @Mock
    private HabilidadRepositoryPort habilidades;

    private VacanteService servicio;

    @BeforeEach
    void preparar() {
        servicio = new VacanteService(vacantes, empresas, habilidades, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    void deberiaPublicarLaVacanteDeUnaEmpresaVerificada() {
        Empresa empresa = empresaDelUsuario(true);
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA, GIT));

        VacanteDetalle detalle = servicio.publicar(USUARIO, datos(LocalDate.of(2026, 11, 30)));

        ArgumentCaptor<Vacante> guardada = ArgumentCaptor.forClass(Vacante.class);
        verify(vacantes).crear(guardada.capture());
        assertThat(guardada.getValue().empresaId()).isEqualTo(empresa.id());
        assertThat(guardada.getValue().estado()).isEqualTo(EstadoVacante.ABIERTA);
        assertThat(guardada.getValue().fechaPublicacion()).isEqualTo(AHORA);
        assertThat(detalle.empresa()).isEqualTo(empresa);
        assertThat(detalle.habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Java", "Git");
    }

    @Test
    void deberiaImpedirQueUnaEmpresaSinVerificarPublique() {
        empresaDelUsuario(false);

        assertThatThrownBy(() -> servicio.publicar(USUARIO, datos(LocalDate.of(2026, 11, 30))))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("Tu empresa debe estar verificada para publicar vacantes");
        verify(vacantes, never()).crear(any());
    }

    @Test
    void deberiaRechazarUnaHabilidadQueNoEstaEnElCatalogo() {
        empresaDelUsuario(true);
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA));

        assertThatThrownBy(() -> servicio.publicar(USUARIO, datos(LocalDate.of(2026, 11, 30))))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: la habilidad " + GIT.id() + " no existe");
        verify(vacantes, never()).crear(any());
    }

    @Test
    void deberiaRechazarUnaFechaDeCierrePasadaSinGuardar() {
        empresaDelUsuario(true);

        assertThatThrownBy(() -> servicio.publicar(USUARIO, datos(LocalDate.of(2026, 11, 3))))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("fechaCierre:");
        verify(vacantes, never()).crear(any());
    }

    @Test
    void deberiaEditarUnaVacantePropiaAbierta() {
        Empresa empresa = empresaDelUsuario(true);
        Vacante actual = vacanteDe(empresa.id());
        when(vacantes.buscarPorId(actual.id())).thenReturn(Optional.of(actual));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA, GIT));

        DatosVacante nuevos = new DatosVacante("Desarrollador backend junior", "Otra descripción", TipoVacante.JUNIOR,
                Modalidad.REMOTA, "Envigado", null, null, LocalDate.of(2026, 12, 15),
                List.of(new VacanteHabilidad(GIT.id(), NivelHabilidad.AVANZADO, true)));
        VacanteDetalle detalle = servicio.editar(USUARIO, actual.id(), nuevos);

        ArgumentCaptor<Vacante> guardada = ArgumentCaptor.forClass(Vacante.class);
        verify(vacantes).actualizar(guardada.capture());
        assertThat(guardada.getValue().id()).isEqualTo(actual.id());
        assertThat(guardada.getValue().titulo()).isEqualTo("Desarrollador backend junior");
        assertThat(detalle.habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Git");
    }

    @Test
    void deberiaImpedirEditarLaVacanteDeOtraEmpresa() {
        empresaDelUsuario(true);
        Vacante ajena = vacanteDe(UUID.randomUUID());
        when(vacantes.buscarPorId(ajena.id())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.editar(USUARIO, ajena.id(), datos(LocalDate.of(2026, 11, 30))))
                .isInstanceOf(AccesoDenegadoException.class);
        verify(vacantes, never()).actualizar(any());
    }

    @Test
    void deberiaImpedirEditarUnaVacanteCerrada() {
        Empresa empresa = empresaDelUsuario(true);
        Vacante cerrada = vacanteDe(empresa.id()).cerrar();
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));

        assertThatThrownBy(() -> servicio.editar(USUARIO, cerrada.id(), datos(LocalDate.of(2026, 11, 30))))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessage("No se puede editar una vacante cerrada");
        verify(vacantes, never()).actualizar(any());
    }

    @Test
    void deberiaResponderNoEncontradaSiLaVacanteNoExiste() {
        empresaDelUsuario(true);
        UUID inexistente = UUID.randomUUID();
        when(vacantes.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cerrar(USUARIO, inexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La vacante no existe");
    }

    @Test
    void deberiaCerrarUnaVacanteAbierta() {
        Empresa empresa = empresaDelUsuario(true);
        Vacante abierta = vacanteDe(empresa.id());
        when(vacantes.buscarPorId(abierta.id())).thenReturn(Optional.of(abierta));
        when(vacantes.actualizar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA, GIT));

        VacanteDetalle detalle = servicio.cerrar(USUARIO, abierta.id());

        assertThat(detalle.vacante().estado()).isEqualTo(EstadoVacante.CERRADA);
    }

    @Test
    void deberiaCerrarDeFormaIdempotente() {
        Empresa empresa = empresaDelUsuario(true);
        Vacante cerrada = vacanteDe(empresa.id()).cerrar();
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA, GIT));

        VacanteDetalle detalle = servicio.cerrar(USUARIO, cerrada.id());

        assertThat(detalle.vacante().estado()).isEqualTo(EstadoVacante.CERRADA);
        verify(vacantes, never()).actualizar(any());
    }

    @Test
    void deberiaMostrarACandidatosCualquierVacanteAunqueEsteCerrada() {
        Empresa duena = empresa(UUID.randomUUID(), true);
        Vacante cerrada = vacanteDe(duena.id()).cerrar();
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));
        when(empresas.buscarConCuentaPorId(duena.id()))
                .thenReturn(Optional.of(new EmpresaConCuenta(duena, "talento@softwaremedellin.co", AHORA)));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA, GIT));

        VacanteDetalle detalle = servicio.consultar(UUID.randomUUID(), Rol.CANDIDATO, cerrada.id());

        assertThat(detalle.empresa()).isEqualTo(duena);
    }

    @Test
    void deberiaImpedirQueUnaEmpresaVeaLaVacanteDeOtra() {
        empresaDelUsuario(true);
        Vacante ajena = vacanteDe(UUID.randomUUID());
        when(vacantes.buscarPorId(ajena.id())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.consultar(USUARIO, Rol.EMPRESA, ajena.id()))
                .isInstanceOf(AccesoDenegadoException.class);
    }

    @Test
    void deberiaListarSoloLasVacantesDeLaEmpresaDelUsuario() {
        Empresa empresa = empresaDelUsuario(false);
        SolicitudPagina solicitud = new SolicitudPagina(0, 10);
        Pagina<VacanteDeEmpresaResumen> pagina = new Pagina<>(List.of(), 0, 10, 0, 0);
        when(vacantes.listarDeEmpresa(empresa.id(), EstadoVacante.ABIERTA, solicitud)).thenReturn(pagina);

        assertThat(servicio.listarDeMiEmpresa(USUARIO, EstadoVacante.ABIERTA, solicitud)).isEqualTo(pagina);
    }

    private Empresa empresaDelUsuario(boolean verificada) {
        Empresa empresa = empresa(USUARIO, verificada);
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO))
                .thenReturn(Optional.of(new EmpresaConCuenta(empresa, "talento@softwaremedellin.co", AHORA)));
        return empresa;
    }

    private static Empresa empresa(UUID usuarioId, boolean verificada) {
        Empresa empresa = Empresa.nueva(usuarioId, "Software Medellín S.A.S.", "9001234567", null, "Medellín", null);
        return verificada ? empresa.verificar() : empresa;
    }

    private static Vacante vacanteDe(UUID empresaId) {
        return Vacante.nueva(empresaId, datos(LocalDate.of(2026, 11, 30)), AHORA.minusSeconds(86_400));
    }

    private static DatosVacante datos(LocalDate fechaCierre) {
        return new DatosVacante("Desarrollador Java junior", "Apoyarás el desarrollo de APIs REST.", TipoVacante.JUNIOR,
                Modalidad.HIBRIDA, "Medellín", 2_800_000L, 3_500_000L, fechaCierre, List.of(
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false)));
    }
}
