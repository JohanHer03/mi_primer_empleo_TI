package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.PerfilCandidatoUseCase.ActualizarPerfilCandidatoComando;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.PerfilCandidato;
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
class PerfilCandidatoServiceTest {

    private static final UUID USUARIO = UUID.randomUUID();
    private static final Habilidad JAVA = new Habilidad(UUID.randomUUID(), "Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad GIT = new Habilidad(UUID.randomUUID(), "Git", CategoriaHabilidad.HERRAMIENTA);

    @Mock
    private CandidatoRepositoryPort candidatos;

    @Mock
    private HabilidadRepositoryPort habilidades;

    private PerfilCandidatoService servicio;

    @BeforeEach
    void preparar() {
        servicio = new PerfilCandidatoService(candidatos, habilidades);
    }

    @Test
    void deberiaDevolverElPerfilSinHabilidadesSinConsultarElCatalogo() {
        candidatoDelUsuario(List.of());

        PerfilCandidato perfil = servicio.consultar(USUARIO);

        assertThat(perfil.cuenta().email()).isEqualTo("laura@udea.edu.co");
        assertThat(perfil.habilidades()).isEmpty();
        verifyNoInteractions(habilidades);
    }

    @Test
    void deberiaResponderNoEncontradoSiElUsuarioNoEsCandidato() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.consultar(USUARIO))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessage("El candidato no existe");
    }

    @Test
    void deberiaActualizarElPerfilConservandoSusHabilidades() {
        candidatoDelUsuario(List.of(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO)));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(JAVA));

        PerfilCandidato perfil = servicio.actualizar(new ActualizarPerfilCandidatoComando(USUARIO, "Laura",
                "Gómez Restrepo", EstadoAcademico.EGRESADO, null, "UdeA", "Ingeniería de Sistemas", "Medellín",
                "https://github.com/lauragomez", "Backend con Java."));

        ArgumentCaptor<Candidato> guardado = ArgumentCaptor.forClass(Candidato.class);
        verify(candidatos).actualizar(guardado.capture());
        assertThat(guardado.getValue().estadoAcademico()).isEqualTo(EstadoAcademico.EGRESADO);
        assertThat(guardado.getValue().githubUrl()).isEqualTo("https://github.com/lauragomez");
        assertThat(perfil.habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Java");
    }

    @Test
    void deberiaRechazarUnPerfilIncoherenteSinGuardar() {
        candidatoDelUsuario(List.of());

        assertThatThrownBy(() -> servicio.actualizar(new ActualizarPerfilCandidatoComando(USUARIO, "Laura", "Gómez",
                EstadoAcademico.ESTUDIANTE, null, null, null, null, null, null)))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: es obligatorio para estudiantes");
        verify(candidatos, never()).actualizar(any());
    }

    @Test
    void deberiaReemplazarLasHabilidadesYDevolverlasEnOrdenDeCatalogo() {
        candidatoDelUsuario(List.of(new CandidatoHabilidad(UUID.randomUUID(), NivelHabilidad.BASICO)));
        when(habilidades.buscarPorIds(any())).thenReturn(List.of(GIT, JAVA));

        PerfilCandidato perfil = servicio.definirHabilidades(USUARIO, List.of(
                new CandidatoHabilidad(GIT.id(), NivelHabilidad.BASICO),
                new CandidatoHabilidad(JAVA.id(), NivelHabilidad.AVANZADO)));

        ArgumentCaptor<Candidato> guardado = ArgumentCaptor.forClass(Candidato.class);
        verify(candidatos).actualizar(guardado.capture());
        assertThat(guardado.getValue().idsHabilidades()).containsExactly(GIT.id(), JAVA.id());
        assertThat(perfil.habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Java", "Git");
    }

    @Test
    void deberiaRechazarUnaHabilidadQueNoEstaEnElCatalogoSinGuardar() {
        candidatoDelUsuario(List.of());
        UUID inexistente = UUID.randomUUID();
        when(habilidades.buscarPorIds(any())).thenReturn(List.of());

        assertThatThrownBy(() -> servicio.definirHabilidades(USUARIO,
                List.of(new CandidatoHabilidad(inexistente, NivelHabilidad.BASICO))))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: la habilidad " + inexistente + " no existe");
        verify(candidatos, never()).actualizar(any());
    }

    @Test
    void deberiaPermitirBorrarTodasLasHabilidades() {
        candidatoDelUsuario(List.of(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.BASICO)));

        PerfilCandidato perfil = servicio.definirHabilidades(USUARIO, List.of());

        assertThat(perfil.habilidades()).isEmpty();
        verify(candidatos).actualizar(any());
    }

    private void candidatoDelUsuario(List<CandidatoHabilidad> declaradas) {
        Candidato candidato = Candidato.nuevo(USUARIO, "Laura", "Gómez", "UdeA", "Ingeniería de Sistemas",
                EstadoAcademico.ESTUDIANTE, 9, "Medellín").conHabilidades(declaradas);
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO))
                .thenReturn(Optional.of(new CandidatoConCuenta(candidato, "laura@udea.edu.co")));
    }
}
