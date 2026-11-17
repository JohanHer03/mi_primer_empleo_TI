package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.CompatibilidadDetalle;
import com.primerempleo.domain.model.CompatibilidadDetalle.HabilidadEvaluada;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Postulacion;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompatibilidadServiceTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final Habilidad JAVA = Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad GIT = Habilidad.nueva("Git", CategoriaHabilidad.HERRAMIENTA);

    @Mock
    private CandidatoRepositoryPort candidatos;

    @Mock
    private VacanteRepositoryPort vacantes;

    @Mock
    private HabilidadRepositoryPort habilidades;

    @Mock
    private PostulacionRepositoryPort postulaciones;

    private CompatibilidadService servicio;
    private Candidato candidato;

    @BeforeEach
    void preparar() {
        servicio = new CompatibilidadService(candidatos, vacantes, habilidades, postulaciones);
        candidato = Candidato.nuevo(USUARIO, "Laura", "Gómez", null, null, EstadoAcademico.EGRESADO, null, null)
                .conHabilidades(List.of(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO)));
    }

    @Test
    void deberiaCalcularLaCompatibilidadDeHoyYLoQueLeFalta() {
        candidatoDelUsuario();
        Vacante vacante = vacante();
        when(vacantes.buscarPorId(vacante.id())).thenReturn(Optional.of(vacante));
        when(habilidades.buscarPorIds(vacante.idsHabilidades())).thenReturn(List.of(JAVA, GIT));
        when(postulaciones.buscar(candidato.id(), vacante.id())).thenReturn(Optional.empty());

        CompatibilidadDetalle detalle = servicio.consultar(USUARIO, vacante.id());

        // Java cumplida (1 × 2) y Git que no tiene (0 × 1): 2 / 3
        assertThat(detalle.porcentaje()).isEqualTo(new BigDecimal("66.7"));
        assertThat(detalle.habilidades()).extracting(HabilidadEvaluada::cumple).containsExactly(true, false);
        assertThat(detalle.postulacion()).isNull();
    }

    @Test
    void deberiaAdjuntarLaPostulacionAunqueLaVacanteEsteCerrada() {
        candidatoDelUsuario();
        // Desde "Mis postulaciones" el candidato puede abrir una vacante que ya se cerró
        Vacante cerrada = vacante().cerrar();
        Postulacion postulacion = Postulacion.nueva(candidato.id(), cerrada.id(), new BigDecimal("33.3"),
                Instant.parse("2026-11-05T15:00:00Z"));
        when(vacantes.buscarPorId(cerrada.id())).thenReturn(Optional.of(cerrada));
        when(habilidades.buscarPorIds(cerrada.idsHabilidades())).thenReturn(List.of(JAVA, GIT));
        when(postulaciones.buscar(candidato.id(), cerrada.id())).thenReturn(Optional.of(postulacion));

        CompatibilidadDetalle detalle = servicio.consultar(USUARIO, cerrada.id());

        assertThat(detalle.porcentaje()).isEqualTo(new BigDecimal("66.7"));
        assertThat(detalle.postulacion()).isEqualTo(postulacion);
    }

    @Test
    void deberiaResponderNoEncontradaSiLaVacanteNoExiste() {
        candidatoDelUsuario();
        UUID inexistente = UUID.randomUUID();
        when(vacantes.buscarPorId(inexistente)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar(USUARIO, inexistente))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("La vacante no existe");
        verify(postulaciones, never()).buscar(any(), any());
    }

    @Test
    void deberiaResponderNoEncontradoSiElUsuarioNoTienePerfilDeCandidato() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar(USUARIO, UUID.randomUUID()))
                .isInstanceOf(RecursoNoEncontradoException.class);
        verify(vacantes, never()).buscarPorId(any());
    }

    private void candidatoDelUsuario() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO))
                .thenReturn(Optional.of(new CandidatoConCuenta(candidato, "laura@udea.edu.co")));
    }

    private static Vacante vacante() {
        return Vacante.nueva(UUID.randomUUID(), new DatosVacante("Desarrollador Java junior", "Descripción",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", null, null, LocalDate.of(2026, 12, 15), List.of(
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false))), Instant.parse("2026-11-01T15:00:00Z"));
    }
}
