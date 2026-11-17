package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.FiltroVacantes;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.OrdenVacantes;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.TipoVacante;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteHabilidad;
import com.primerempleo.domain.model.VacanteResumen;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusquedaVacantesServiceTest {

    /** 16 de noviembre a las 9 p. m. en Colombia: en UTC ya es 17. */
    private static final Instant AHORA = Instant.parse("2026-11-17T02:00:00Z");
    private static final UUID USUARIO = UUID.randomUUID();
    private static final UUID JAVA = UUID.randomUUID();
    private static final UUID GIT = UUID.randomUUID();
    private static final Empresa EMPRESA = Empresa.nueva(UUID.randomUUID(), "Software Medellín S.A.S.", "9001234567",
            null, null, null).verificar();

    @Mock
    private VacanteRepositoryPort vacantes;

    @Mock
    private CandidatoRepositoryPort candidatos;

    @Mock
    private EmpresaRepositoryPort empresas;

    @Mock
    private PostulacionRepositoryPort postulaciones;

    private BusquedaVacantesService servicio;
    private Candidato candidato;

    @BeforeEach
    void preparar() {
        servicio = new BusquedaVacantesService(vacantes, candidatos, empresas, postulaciones,
                Clock.fixed(AHORA, ZoneOffset.UTC));
        // Sabe Java intermedio y no sabe Git
        candidato = Candidato.nuevo(USUARIO, "Laura", "Gómez", null, null, EstadoAcademico.EGRESADO, null, null)
                .conHabilidades(List.of(new CandidatoHabilidad(JAVA, NivelHabilidad.INTERMEDIO)));
    }

    @Test
    void deberiaOrdenarPorCompatibilidadYMarcarLasPostuladas() {
        candidatoDelUsuario();
        Vacante soloGit = vacante("Soporte con Git", 1, obligatoria(GIT));
        Vacante soloJava = vacante("Desarrollador Java", 2, obligatoria(JAVA));
        Vacante mitad = vacante("Java y Git", 3, obligatoria(JAVA), obligatoria(GIT));
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(List.of(soloGit, soloJava, mitad));
        when(empresas.buscarPorIds(any())).thenReturn(List.of(EMPRESA));
        when(postulaciones.idsVacantesPostuladas(candidato.id())).thenReturn(Set.of(soloJava.id()));

        Pagina<VacanteResumen> pagina = servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(),
                OrdenVacantes.COMPATIBILIDAD, new SolicitudPagina(0, 10));

        assertThat(pagina.contenido()).extracting(r -> r.vacante().titulo())
                .containsExactly("Desarrollador Java", "Java y Git", "Soporte con Git");
        assertThat(pagina.contenido()).extracting(VacanteResumen::compatibilidad)
                .containsExactly(new BigDecimal("100.0"), new BigDecimal("50.0"), new BigDecimal("0.0"));
        assertThat(pagina.contenido()).extracting(VacanteResumen::postulado).containsExactly(true, false, false);
        assertThat(pagina.contenido().getFirst().empresa()).isEqualTo(EMPRESA);
    }

    @Test
    void deberiaDesempatarLaCompatibilidadConLaMasReciente() {
        candidatoDelUsuario();
        Vacante antigua = vacante("Antigua", 10, obligatoria(JAVA));
        Vacante reciente = vacante("Reciente", 1, obligatoria(JAVA));
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(List.of(antigua, reciente));
        when(empresas.buscarPorIds(any())).thenReturn(List.of(EMPRESA));

        Pagina<VacanteResumen> pagina = servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(),
                OrdenVacantes.COMPATIBILIDAD, new SolicitudPagina(0, 10));

        assertThat(pagina.contenido()).extracting(r -> r.vacante().titulo()).containsExactly("Reciente", "Antigua");
    }

    @Test
    void deberiaOrdenarPorFechaDePublicacionSiSeElige() {
        candidatoDelUsuario();
        Vacante compatibleAntigua = vacante("Compatible y antigua", 10, obligatoria(JAVA));
        Vacante incompatibleReciente = vacante("Incompatible y reciente", 1, obligatoria(GIT));
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(List.of(compatibleAntigua, incompatibleReciente));
        when(empresas.buscarPorIds(any())).thenReturn(List.of(EMPRESA));

        Pagina<VacanteResumen> pagina = servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(),
                OrdenVacantes.FECHA_PUBLICACION, new SolicitudPagina(0, 10));

        assertThat(pagina.contenido()).extracting(r -> r.vacante().titulo())
                .containsExactly("Incompatible y reciente", "Compatible y antigua");
    }

    @Test
    void deberiaPasarLosFiltrosYElDiaDeHoyEnColombia() {
        candidatoDelUsuario();
        FiltroVacantes filtro = new FiltroVacantes(Modalidad.REMOTA, TipoVacante.PRACTICA, JAVA, "Medellín");
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(List.of());

        servicio.buscar(USUARIO, filtro, OrdenVacantes.COMPATIBILIDAD, new SolicitudPagina(0, 10));

        verify(vacantes).buscarAbiertas(filtro, LocalDate.of(2026, 11, 16));
    }

    @Test
    void deberiaResponderUnaPaginaVaciaSinConsultarEmpresasNiPostulaciones() {
        candidatoDelUsuario();
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(List.of());

        Pagina<VacanteResumen> pagina = servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(),
                OrdenVacantes.COMPATIBILIDAD, new SolicitudPagina(0, 10));

        assertThat(pagina.contenido()).isEmpty();
        assertThat(pagina.totalElementos()).isZero();
        verifyNoInteractions(empresas, postulaciones);
    }

    @Test
    void deberiaPaginarDespuesDeOrdenar() {
        candidatoDelUsuario();
        List<Vacante> cinco = List.of(vacante("A", 5, obligatoria(JAVA)), vacante("B", 4, obligatoria(JAVA)),
                vacante("C", 3, obligatoria(JAVA)), vacante("D", 2, obligatoria(JAVA)), vacante("E", 1, obligatoria(JAVA)));
        when(vacantes.buscarAbiertas(any(), any())).thenReturn(cinco);
        when(empresas.buscarPorIds(any())).thenReturn(List.of(EMPRESA));

        Pagina<VacanteResumen> segunda = servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(),
                OrdenVacantes.FECHA_PUBLICACION, new SolicitudPagina(1, 2));

        assertThat(segunda.contenido()).extracting(r -> r.vacante().titulo()).containsExactly("C", "B");
        assertThat(segunda.totalElementos()).isEqualTo(5);
        assertThat(segunda.totalPaginas()).isEqualTo(3);
    }

    @Test
    void deberiaResponderNoEncontradoSiElUsuarioNoEsCandidato() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> servicio.buscar(USUARIO, FiltroVacantes.sinFiltros(), OrdenVacantes.COMPATIBILIDAD,
                new SolicitudPagina(0, 10)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private void candidatoDelUsuario() {
        when(candidatos.buscarConCuentaPorUsuarioId(USUARIO))
                .thenReturn(Optional.of(new CandidatoConCuenta(candidato, "laura@udea.edu.co")));
    }

    /** Vacante publicada hace {@code diasAtras} días. */
    private static Vacante vacante(String titulo, int diasAtras, VacanteHabilidad... habilidades) {
        Instant publicacion = AHORA.minusSeconds(diasAtras * 86_400L);
        return Vacante.nueva(EMPRESA.id(), new DatosVacante(titulo, "Descripción", TipoVacante.JUNIOR,
                Modalidad.HIBRIDA, "Medellín", null, null, LocalDate.of(2026, 12, 31), List.of(habilidades)), publicacion);
    }

    private static VacanteHabilidad obligatoria(UUID habilidadId) {
        return new VacanteHabilidad(habilidadId, NivelHabilidad.INTERMEDIO, true);
    }
}
