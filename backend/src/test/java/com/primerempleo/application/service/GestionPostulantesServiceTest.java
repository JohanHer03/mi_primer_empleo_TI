package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.Postulante;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.TipoVacante;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteHabilidad;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
class GestionPostulantesServiceTest {

    private static final UUID USUARIO_EMPRESA = UUID.randomUUID();
    private static final Habilidad JAVA = Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE);
    private static final Empresa EMPRESA = Empresa.nueva(USUARIO_EMPRESA, "Software Medellín S.A.S.", "9001234567",
            null, null, null).verificar();
    private static final Empresa OTRA = Empresa.nueva(UUID.randomUUID(), "Otra S.A.S.", "8001234567",
            null, null, null).verificar();

    @Mock
    private PostulacionRepositoryPort postulaciones;

    @Mock
    private VacanteRepositoryPort vacantes;

    @Mock
    private EmpresaRepositoryPort empresas;

    @Mock
    private CandidatoRepositoryPort candidatos;

    @Mock
    private HabilidadRepositoryPort habilidades;

    private GestionPostulantesService servicio;
    private CandidatoConCuenta laura;

    @BeforeEach
    void preparar() {
        servicio = new GestionPostulantesService(postulaciones, vacantes, empresas, candidatos, habilidades);
        Candidato candidato = Candidato.nuevo(UUID.randomUUID(), "Laura", "Gómez", null, null, EstadoAcademico.EGRESADO,
                        null, null)
                .conHabilidades(List.of(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.BASICO)));
        laura = new CandidatoConCuenta(candidato, "laura@udea.edu.co");
        when(empresas.buscarConCuentaPorUsuarioId(USUARIO_EMPRESA))
                .thenReturn(Optional.of(new EmpresaConCuenta(EMPRESA, "talento@softwaremedellin.co", Instant.now())));
    }

    @Test
    void deberiaListarLosPostulantesDeSuVacanteConElPerfilDelCandidato() {
        Vacante vacante = vacanteDe(EMPRESA);
        SolicitudPagina solicitud = new SolicitudPagina(0, 10);
        Postulacion postulacion = postulacion(vacante, EstadoPostulacion.ENVIADA);
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(postulaciones.listarDeVacante(vacante.id(), EstadoPostulacion.ENVIADA, solicitud))
                .thenReturn(new Pagina<>(List.of(postulacion), 0, 10, 1, 1));
        when(candidatos.buscarConCuentaPorIds(any())).thenReturn(List.of(laura));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA));

        Pagina<Postulante> pagina = servicio.listarDeVacante(USUARIO_EMPRESA, vacante.id(), EstadoPostulacion.ENVIADA,
                solicitud);

        assertThat(pagina.totalElementos()).isEqualTo(1);
        Postulante postulante = pagina.contenido().getFirst();
        assertThat(postulante.postulacion()).isEqualTo(postulacion);
        assertThat(postulante.candidato().cuenta().email()).isEqualTo("laura@udea.edu.co");
        assertThat(postulante.candidato().habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Java");
    }

    @Test
    void deberiaResponderVacioSinConsultarCandidatosSiNoHayPostulantes() {
        Vacante vacante = vacanteDe(EMPRESA);
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(postulaciones.listarDeVacante(any(), any(), any())).thenReturn(new Pagina<>(List.of(), 0, 10, 0, 0));

        assertThat(servicio.listarDeVacante(USUARIO_EMPRESA, vacante.id(), null, new SolicitudPagina(0, 10)).contenido())
                .isEmpty();
        verify(candidatos, never()).buscarConCuentaPorIds(any());
    }

    @Test
    void deberiaImpedirVerLosPostulantesDeUnaVacanteAjena() {
        Vacante ajena = vacanteDe(OTRA);
        when(vacantes.buscarPorId(ajena.id())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.listarDeVacante(USUARIO_EMPRESA, ajena.id(), null, new SolicitudPagina(0, 10)))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("La vacante pertenece a otra empresa");
        verify(postulaciones, never()).listarDeVacante(any(), any(), any());
    }

    @Test
    void deberiaResponderNoEncontradaSiLaVacanteNoExiste() {
        UUID inexistente = UUID.randomUUID();
        when(vacantes.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.listarDeVacante(USUARIO_EMPRESA, inexistente, null, new SolicitudPagina(0, 10)))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La vacante no existe");
    }

    @Test
    void deberiaAvanzarElEstadoYGuardarSoloElEstado() {
        Vacante vacante = vacanteDe(EMPRESA);
        Postulacion postulacion = postulacion(vacante, EstadoPostulacion.ENVIADA);
        when(postulaciones.buscarPorId(postulacion.id())).thenReturn(Optional.of(postulacion));
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(postulaciones.actualizarEstado(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(candidatos.buscarConCuentaPorIds(any())).thenReturn(List.of(laura));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA));

        Postulante postulante = servicio.cambiarEstado(USUARIO_EMPRESA, postulacion.id(), EstadoPostulacion.EN_REVISION);

        ArgumentCaptor<Postulacion> guardada = ArgumentCaptor.forClass(Postulacion.class);
        verify(postulaciones).actualizarEstado(guardada.capture());
        assertThat(guardada.getValue().estado()).isEqualTo(EstadoPostulacion.EN_REVISION);
        assertThat(guardada.getValue().compatibilidad()).isEqualTo(postulacion.compatibilidad());
        assertThat(postulante.postulacion().estado()).isEqualTo(EstadoPostulacion.EN_REVISION);
    }

    @Test
    void deberiaPermitirCambiarElEstadoAunqueLaVacanteEsteCerrada() {
        Vacante cerrada = vacanteDe(EMPRESA).cerrar();
        Postulacion postulacion = postulacion(cerrada, EstadoPostulacion.EN_REVISION);
        when(postulaciones.buscarPorId(postulacion.id())).thenReturn(Optional.of(postulacion));
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));
        when(postulaciones.actualizarEstado(any())).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(candidatos.buscarConCuentaPorIds(any())).thenReturn(List.of(laura));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA));

        assertThat(servicio.cambiarEstado(USUARIO_EMPRESA, postulacion.id(), EstadoPostulacion.ENTREVISTA)
                .postulacion().estado()).isEqualTo(EstadoPostulacion.ENTREVISTA);
    }

    @Test
    void deberiaImpedirCambiarElEstadoDeUnaPostulacionAjena() {
        Vacante ajena = vacanteDe(OTRA);
        Postulacion postulacion = postulacion(ajena, EstadoPostulacion.ENVIADA);
        when(postulaciones.buscarPorId(postulacion.id())).thenReturn(Optional.of(postulacion));
        when(vacantes.buscarPorId(ajena.id())).thenReturn(Optional.of(ajena));

        assertThatThrownBy(() -> servicio.cambiarEstado(USUARIO_EMPRESA, postulacion.id(), EstadoPostulacion.EN_REVISION))
                .isInstanceOf(AccesoDenegadoException.class)
                .hasMessage("La postulación pertenece a otra empresa");
        verify(postulaciones, never()).actualizarEstado(any());
    }

    @Test
    void deberiaRechazarUnaTransicionNoPermitidaSinGuardar() {
        Vacante vacante = vacanteDe(EMPRESA);
        Postulacion postulacion = postulacion(vacante, EstadoPostulacion.EN_REVISION);
        when(postulaciones.buscarPorId(postulacion.id())).thenReturn(Optional.of(postulacion));
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));

        assertThatThrownBy(() -> servicio.cambiarEstado(USUARIO_EMPRESA, postulacion.id(), EstadoPostulacion.CONTRATADO))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessage("Una postulación EN_REVISION solo puede pasar a ENTREVISTA o RECHAZADA");
        verify(postulaciones, never()).actualizarEstado(any());
    }

    @Test
    void deberiaResponderNoEncontradaSiLaPostulacionNoExiste() {
        UUID inexistente = UUID.randomUUID();
        when(postulaciones.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.cambiarEstado(USUARIO_EMPRESA, inexistente, EstadoPostulacion.EN_REVISION))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La postulación no existe");
    }

    private Postulacion postulacion(Vacante vacante, EstadoPostulacion estado) {
        return new Postulacion(UUID.randomUUID(), laura.candidato().id(), vacante.id(),
                Instant.parse("2026-11-10T14:05:00Z"), estado, new BigDecimal("50.0"));
    }

    private static Vacante vacanteDe(Empresa empresa) {
        return Vacante.nueva(empresa.id(), new DatosVacante("Desarrollador Java junior", "Descripción",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", null, null, LocalDate.of(2026, 12, 15), List.of(
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true))), Instant.parse("2026-11-01T15:00:00Z"));
    }
}
