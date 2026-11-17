package com.primerempleo.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.model.Candidato;
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
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Detalle de la HU-04 (docs/contrato-api.md 6.1): porcentaje y cada habilidad con el nivel del candidato. */
class CalculadoraCompatibilidadDetalleTest {

    private static final Habilidad JAVA = Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad SPRING = Habilidad.nueva("Spring Boot", CategoriaHabilidad.FRAMEWORK);
    private static final Habilidad GIT = Habilidad.nueva("Git", CategoriaHabilidad.HERRAMIENTA);
    private static final List<Habilidad> CATALOGO = List.of(JAVA, SPRING, GIT);

    @Test
    void deberiaDetallarElEjemploDelContratoConLoQueCumpleYLoQueLeFalta() {
        // Git deseable va de primero en la lista de la vacante: el detalle pone primero las obligatorias
        Vacante vacante = vacante(
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false),
                new VacanteHabilidad(SPRING.id(), NivelHabilidad.BASICO, true),
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true));
        Candidato candidato = candidato(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.BASICO),
                new CandidatoHabilidad(SPRING.id(), NivelHabilidad.AVANZADO));

        CompatibilidadDetalle detalle = CalculadoraCompatibilidad.detallar(vacante, candidato, CATALOGO, null);

        assertThat(detalle.vacanteId()).isEqualTo(vacante.id());
        assertThat(detalle.porcentaje()).isEqualTo(new BigDecimal("60.0"));
        assertThat(detalle.postulacion()).isNull();
        assertThat(detalle.habilidades()).extracting(h -> h.requerida().habilidad().nombre())
                .containsExactly("Java", "Spring Boot", "Git");
        assertThat(detalle.habilidades()).extracting(HabilidadEvaluada::nivelCandidato)
                .containsExactly(NivelHabilidad.BASICO, NivelHabilidad.AVANZADO, null);
        assertThat(detalle.habilidades()).extracting(HabilidadEvaluada::cumple).containsExactly(false, true, false);
    }

    @Test
    void deberiaMarcarTodoSinCumplirSiElCandidatoNoTieneHabilidades() {
        Vacante vacante = vacante(new VacanteHabilidad(JAVA.id(), NivelHabilidad.BASICO, true),
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false));

        CompatibilidadDetalle detalle = CalculadoraCompatibilidad.detallar(vacante, candidato(), CATALOGO, null);

        assertThat(detalle.porcentaje()).isEqualTo(new BigDecimal("0.0"));
        assertThat(detalle.habilidades()).extracting(HabilidadEvaluada::cumple).containsOnly(false);
        assertThat(detalle.habilidades()).extracting(HabilidadEvaluada::nivelCandidato).containsOnlyNulls();
    }

    @Test
    void deberiaAdjuntarLaPostulacionSinCambiarSuCompatibilidadGuardada() {
        Vacante vacante = vacante(new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true));
        // Se postuló con Java básico (50.0) y después subió a intermedio
        Postulacion postulacion = Postulacion.nueva(UUID.randomUUID(), vacante.id(), new BigDecimal("50.0"),
                Instant.parse("2026-11-10T15:00:00Z"));
        Candidato candidato = candidato(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO));

        CompatibilidadDetalle detalle = CalculadoraCompatibilidad.detallar(vacante, candidato, CATALOGO, postulacion);

        assertThat(detalle.porcentaje()).isEqualTo(new BigDecimal("100.0"));
        assertThat(detalle.postulacion().compatibilidad()).isEqualTo(new BigDecimal("50.0"));
    }

    @Test
    void deberiaRechazarUnaHabilidadQueNoEstaEnElCatalogo() {
        Vacante vacante = vacante(new VacanteHabilidad(UUID.randomUUID(), NivelHabilidad.BASICO, true));

        assertThatThrownBy(() -> CalculadoraCompatibilidad.detallar(vacante, candidato(), CATALOGO, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageStartingWith("habilidades: la habilidad");
    }

    private static Vacante vacante(VacanteHabilidad... habilidades) {
        return Vacante.nueva(UUID.randomUUID(), new DatosVacante("Desarrollador Java junior", "Descripción",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", null, null, LocalDate.of(2026, 12, 15),
                List.of(habilidades)), Instant.parse("2026-11-01T15:00:00Z"));
    }

    private static Candidato candidato(CandidatoHabilidad... habilidades) {
        return Candidato.nuevo(UUID.randomUUID(), "Laura", "Gómez", null, null, EstadoAcademico.EGRESADO, null, null)
                .conHabilidades(List.of(habilidades));
    }
}
