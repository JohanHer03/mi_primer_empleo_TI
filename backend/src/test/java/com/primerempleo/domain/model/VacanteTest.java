package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VacanteTest {

    /** 3 de noviembre a las 9 p. m. en Colombia; en UTC ya es 4 de noviembre. */
    private static final Instant AHORA = Instant.parse("2026-11-04T02:00:00Z");
    private static final LocalDate HOY_EN_COLOMBIA = LocalDate.of(2026, 11, 3);
    private static final UUID EMPRESA = UUID.randomUUID();
    private static final UUID JAVA = UUID.randomUUID();
    private static final UUID GIT = UUID.randomUUID();

    @Test
    void deberiaPublicarlaAbiertaConLaFechaDePublicacion() {
        Vacante vacante = Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA.plusDays(20), requeridas()), AHORA);

        assertThat(vacante.estado()).isEqualTo(EstadoVacante.ABIERTA);
        assertThat(vacante.fechaPublicacion()).isEqualTo(AHORA);
        assertThat(vacante.empresaId()).isEqualTo(EMPRESA);
        assertThat(vacante.titulo()).isEqualTo("Desarrollador Java junior");
    }

    @Test
    void deberiaAceptarQueCierreHoyEnHoraDeColombia() {
        assertThatCode(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, requeridas()), AHORA))
                .doesNotThrowAnyException();
    }

    @Test
    void deberiaRechazarUnaFechaDeCierrePasada() {
        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA.minusDays(1), requeridas()), AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("fechaCierre: debe ser hoy o una fecha posterior");
    }

    @Test
    void deberiaRechazarSalarioMinimoMayorQueElMaximo() {
        DatosVacante datos = new DatosVacante("Desarrollador Java junior", "Descripción", TipoVacante.JUNIOR,
                Modalidad.HIBRIDA, "Medellín", 4_000_000L, 3_000_000L, HOY_EN_COLOMBIA, requeridas());

        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos, AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("salarioMin: no puede ser mayor que salarioMax");
    }

    @Test
    void deberiaAceptarSalariosVacios() {
        DatosVacante datos = new DatosVacante("Practicante de desarrollo", "Descripción", TipoVacante.PRACTICA,
                Modalidad.REMOTA, "Medellín", null, null, HOY_EN_COLOMBIA, requeridas());

        assertThat(Vacante.nueva(EMPRESA, datos, AHORA).salarioMin()).isNull();
    }

    @Test
    void deberiaExigirAlMenosUnaHabilidad() {
        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, List.of()), AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: debe tener entre 1 y 15");
    }

    @Test
    void deberiaRechazarMasDe15Habilidades() {
        List<VacanteHabilidad> muchas = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            muchas.add(new VacanteHabilidad(UUID.randomUUID(), NivelHabilidad.BASICO, true));
        }

        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, muchas), AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: debe tener entre 1 y 15");
    }

    @Test
    void deberiaRechazarHabilidadesRepetidas() {
        List<VacanteHabilidad> repetidas = List.of(
                new VacanteHabilidad(JAVA, NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(JAVA, NivelHabilidad.BASICO, false));

        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, repetidas), AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: no se puede repetir una habilidad");
    }

    @Test
    void deberiaExigirAlMenosUnaHabilidadObligatoria() {
        List<VacanteHabilidad> deseables = List.of(new VacanteHabilidad(GIT, NivelHabilidad.BASICO, false));

        assertThatThrownBy(() -> Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, deseables), AHORA))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: al menos una debe ser obligatoria");
    }

    @Test
    void deberiaEditarConservandoEmpresaEstadoYFechaDePublicacion() {
        Vacante vacante = Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, requeridas()), AHORA);
        DatosVacante nuevos = new DatosVacante("Desarrollador backend junior", "Otra descripción",
                TipoVacante.JUNIOR, Modalidad.REMOTA, "Envigado", null, null, HOY_EN_COLOMBIA.plusDays(5),
                List.of(new VacanteHabilidad(GIT, NivelHabilidad.AVANZADO, true)));

        Vacante editada = vacante.editar(nuevos, AHORA.plusSeconds(3600));

        assertThat(editada.id()).isEqualTo(vacante.id());
        assertThat(editada.empresaId()).isEqualTo(EMPRESA);
        assertThat(editada.fechaPublicacion()).isEqualTo(AHORA);
        assertThat(editada.titulo()).isEqualTo("Desarrollador backend junior");
        assertThat(editada.idsHabilidades()).containsExactly(GIT);
    }

    @Test
    void deberiaQuedarCerradaAlCerrarla() {
        Vacante vacante = Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, requeridas()), AHORA);

        assertThat(vacante.estaCerrada()).isFalse();
        assertThat(vacante.cerrar().estaCerrada()).isTrue();
    }

    @Test
    void deberiaOrdenarElDetalleConLasObligatoriasPrimeroYLuegoPorNombre() {
        UUID docker = UUID.randomUUID();
        Vacante vacante = Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, List.of(
                new VacanteHabilidad(GIT, NivelHabilidad.BASICO, false),
                new VacanteHabilidad(JAVA, NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(docker, NivelHabilidad.BASICO, false))), AHORA);
        List<Habilidad> catalogo = List.of(
                new Habilidad(JAVA, "Java", CategoriaHabilidad.LENGUAJE),
                new Habilidad(GIT, "Git", CategoriaHabilidad.HERRAMIENTA),
                new Habilidad(docker, "Docker", CategoriaHabilidad.HERRAMIENTA));

        VacanteDetalle detalle = VacanteDetalle.de(vacante, empresa(), catalogo);

        assertThat(detalle.habilidades()).extracting(h -> h.habilidad().nombre())
                .containsExactly("Java", "Docker", "Git");
    }

    @Test
    void deberiaRechazarElDetalleSiUnaHabilidadNoEstaEnElCatalogo() {
        Vacante vacante = Vacante.nueva(EMPRESA, datos(HOY_EN_COLOMBIA, requeridas()), AHORA);
        List<Habilidad> soloJava = List.of(new Habilidad(JAVA, "Java", CategoriaHabilidad.LENGUAJE));

        assertThatThrownBy(() -> VacanteDetalle.de(vacante, empresa(), soloJava))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: la habilidad " + GIT + " no existe");
    }

    private static List<VacanteHabilidad> requeridas() {
        return List.of(
                new VacanteHabilidad(JAVA, NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT, NivelHabilidad.BASICO, false));
    }

    private static DatosVacante datos(LocalDate fechaCierre, List<VacanteHabilidad> habilidades) {
        return new DatosVacante(" Desarrollador Java junior ", "Apoyarás el desarrollo de APIs REST.",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", 2_800_000L, 3_500_000L, fechaCierre, habilidades);
    }

    private static Empresa empresa() {
        return new Empresa(EMPRESA, UUID.randomUUID(), "Software Medellín S.A.S.", "9001234567",
                null, null, null, true);
    }
}
