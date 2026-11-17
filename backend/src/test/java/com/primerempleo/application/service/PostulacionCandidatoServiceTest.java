package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.TipoVacante;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteHabilidad;
import java.math.BigDecimal;
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
class PostulacionCandidatoServiceTest {

    /** 30 de noviembre a las 9 p. m. en Colombia: en UTC ya es 1 de diciembre. */
    private static final Instant AHORA = Instant.parse("2026-12-01T02:00:00Z");
    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID JAVA = UUID.randomUUID();
    private static final UUID GIT = UUID.randomUUID();
    private static final Empresa EMPRESA = Empresa.nueva(UUID.randomUUID(), "Software Medellín S.A.S.", "9001234567",
            null, null, null).verificar();

    @Mock
    private PostulacionRepositoryPort postulaciones;

    @Mock
    private CandidatoRepositoryPort candidatos;

    @Mock
    private VacanteRepositoryPort vacantes;

    @Mock
    private EmpresaRepositoryPort empresas;

    private PostulacionCandidatoService servicio;
    private Candidato candidato;

    @BeforeEach
    void preparar() {
        servicio = new PostulacionCandidatoService(postulaciones, candidatos, vacantes, empresas,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        // Java básico y Git intermedio
        candidato = Candidato.nuevo(USUARIO, "Laura", "Gómez", null, null, EstadoAcademico.EGRESADO, null, null)
                .conHabilidades(List.of(new CandidatoHabilidad(JAVA, NivelHabilidad.BASICO),
                        new CandidatoHabilidad(GIT, NivelHabilidad.INTERMEDIO)));
    }

    @Test
    void deberiaPostularseEnviadaYConLaCompatibilidadDeEseMomento() {
        candidatoDelUsuario();
        Vacante vacante = vacante(LocalDate.of(2026, 12, 15));
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(postulaciones.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(empresas.buscarConCuentaPorId(EMPRESA.id()))
                .thenReturn(Optional.of(new EmpresaConCuenta(EMPRESA, "talento@softwaremedellin.co", AHORA)));

        PostulacionDeCandidato resultado = servicio.postularse(USUARIO, vacante.id());

        ArgumentCaptor<Postulacion> guardada = ArgumentCaptor.forClass(Postulacion.class);
        verify(postulaciones).crear(guardada.capture());
        // Java intermedio obligatoria (0,5 × 2) y Git básico deseable (1 × 1): 2 / 3
        assertThat(guardada.getValue().compatibilidad()).isEqualTo(new BigDecimal("66.7"));
        assertThat(guardada.getValue().estado()).isEqualTo(EstadoPostulacion.ENVIADA);
        assertThat(guardada.getValue().candidatoId()).isEqualTo(candidato.id());
        assertThat(guardada.getValue().fecha()).isEqualTo(AHORA);
        assertThat(resultado.vacante().razonSocialEmpresa()).isEqualTo("Software Medellín S.A.S.");
        assertThat(resultado.vacante().titulo()).isEqualTo("Desarrollador Java junior");
    }

    @Test
    void deberiaImpedirPostularseDosVecesALaMismaVacante() {
        candidatoDelUsuario();
        Vacante vacante = vacante(LocalDate.of(2026, 12, 15));
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(postulaciones.existe(candidato.id(), vacante.id())).thenReturn(true);

        assertThatThrownBy(() -> servicio.postularse(USUARIO, vacante.id()))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya te postulaste a esta vacante");
        verify(postulaciones, never()).crear(any());
    }

    @Test
    void deberiaImpedirPostularseAUnaVacanteCerrada() {
        candidatoDelUsuario();
        Vacante cerrada = vacante(LocalDate.of(2026, 12, 15)).cerrar();
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));

        assertThatThrownBy(() -> servicio.postularse(USUARIO, cerrada.id()))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessage("Esta vacante ya no recibe postulaciones");
        verify(postulaciones, never()).crear(any());
    }

    @Test
    void deberiaImpedirPostularseAUnaVacanteVencidaAunqueSigaAbierta() {
        candidatoDelUsuario();
        Vacante vencida = vacante(LocalDate.of(2026, 11, 29));
        when(vacantes.buscarPorId(vencida.id())).thenReturn(Optional.of(vencida));

        assertThatThrownBy(() -> servicio.postularse(USUARIO, vencida.id()))
                .isInstanceOf(OperacionNoPermitidaException.class);
    }

    @Test
    void deberiaAceptarPostulacionesElUltimoDiaEnHoraDeColombia() {
        candidatoDelUsuario();
        // En UTC ya es 1 de diciembre, pero en Colombia sigue siendo 30 de noviembre
        Vacante ultimoDia = vacante(LocalDate.of(2026, 11, 30));
        when(vacantes.buscarPorId(ultimoDia.id())).thenReturn(Optional.of(ultimoDia));
        when(postulaciones.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(empresas.buscarConCuentaPorId(EMPRESA.id()))
                .thenReturn(Optional.of(new EmpresaConCuenta(EMPRESA, "talento@softwaremedellin.co", AHORA)));

        assertThat(servicio.postularse(USUARIO, ultimoDia.id()).postulacion().estado())
                .isEqualTo(EstadoPostulacion.ENVIADA);
    }

    @Test
    void deberiaResponderNoEncontradaSiLaVacanteNoExiste() {
        candidatoDelUsuario();
        UUID inexistente = UUID.randomUUID();
        when(vacantes.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.postularse(USUARIO, inexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La vacante no existe");
    }

    @Test
    void deberiaListarSoloLasPostulacionesDelCandidatoDelUsuario() {
        candidatoDelUsuario();
        SolicitudPagina solicitud = new SolicitudPagina(0, 10);
        Pagina<PostulacionDeCandidato> pagina = new Pagina<>(List.of(), 0, 10, 0, 0);
        when(postulaciones.listarDeCandidato(candidato.id(), EstadoPostulacion.ENVIADA, solicitud)).thenReturn(pagina);

        assertThat(servicio.listarMias(USUARIO, EstadoPostulacion.ENVIADA, solicitud)).isEqualTo(pagina);
    }

    private void candidatoDelUsuario() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO))
                .thenReturn(Optional.of(new CandidatoConCuenta(candidato, "laura@udea.edu.co")));
    }

    private static Vacante vacante(LocalDate fechaCierre) {
        return Vacante.nueva(EMPRESA.id(), new DatosVacante("Desarrollador Java junior", "Descripción",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", null, null, fechaCierre, List.of(
                new VacanteHabilidad(JAVA, NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT, NivelHabilidad.BASICO, false))), Instant.parse("2026-11-01T15:00:00Z"));
    }
}
