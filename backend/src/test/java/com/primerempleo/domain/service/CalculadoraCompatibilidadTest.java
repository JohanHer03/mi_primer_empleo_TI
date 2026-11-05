package com.primerempleo.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.VacanteHabilidad;
import com.primerempleo.domain.service.CalculadoraCompatibilidad.HabilidadFaltante;
import com.primerempleo.domain.service.CalculadoraCompatibilidad.Resultado;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CalculadoraCompatibilidadTest {

    private static final UUID JAVA = UUID.randomUUID();
    private static final UUID SPRING = UUID.randomUUID();
    private static final UUID GIT = UUID.randomUUID();
    private static final UUID DOCKER = UUID.randomUUID();

    @Test
    void deberiaDar60EnElEjemploDelContrato() {
        // docs/contrato-api.md, 5.3
        List<VacanteHabilidad> vacante = List.of(
                obligatoria(JAVA, NivelHabilidad.INTERMEDIO),
                obligatoria(SPRING, NivelHabilidad.BASICO),
                deseable(GIT, NivelHabilidad.BASICO));
        List<CandidatoHabilidad> candidato = List.of(
                tiene(JAVA, NivelHabilidad.BASICO),
                tiene(SPRING, NivelHabilidad.AVANZADO));

        Resultado resultado = CalculadoraCompatibilidad.calcular(vacante, candidato);

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("60.0"));
        assertThat(resultado.faltantes()).containsExactly(
                new HabilidadFaltante(JAVA, NivelHabilidad.INTERMEDIO, NivelHabilidad.BASICO, true),
                new HabilidadFaltante(GIT, NivelHabilidad.BASICO, null, false));
    }

    @Test
    void deberiaDar100SiCumpleTodoYNoFaltarNada() {
        Resultado resultado = CalculadoraCompatibilidad.calcular(
                List.of(obligatoria(JAVA, NivelHabilidad.INTERMEDIO), deseable(GIT, NivelHabilidad.BASICO)),
                List.of(tiene(JAVA, NivelHabilidad.INTERMEDIO), tiene(GIT, NivelHabilidad.BASICO)));

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("100.0"));
        assertThat(resultado.faltantes()).isEmpty();
    }

    @Test
    void deberiaDar0SiElCandidatoNoTieneHabilidades() {
        Resultado resultado = CalculadoraCompatibilidad.calcular(
                List.of(obligatoria(JAVA, NivelHabilidad.BASICO), deseable(GIT, NivelHabilidad.BASICO)), List.of());

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("0.0"));
        assertThat(resultado.faltantes()).extracting(HabilidadFaltante::habilidadId).containsExactly(JAVA, GIT);
    }

    @Test
    void noDeberiaPasarDe1PorTenerUnNivelSuperior() {
        // AVANZADO para algo que pide BASICO cuenta 1, no 3: no compensa la habilidad que falta
        Resultado resultado = CalculadoraCompatibilidad.calcular(
                List.of(obligatoria(JAVA, NivelHabilidad.BASICO), obligatoria(SPRING, NivelHabilidad.BASICO)),
                List.of(tiene(JAVA, NivelHabilidad.AVANZADO)));

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("50.0"));
    }

    @Test
    void deberiaPesarElDobleLasObligatorias() {
        List<VacanteHabilidad> vacante = List.of(obligatoria(JAVA, NivelHabilidad.BASICO), deseable(GIT, NivelHabilidad.BASICO));

        // Solo la obligatoria: 2 de 3; solo la deseable: 1 de 3
        assertThat(CalculadoraCompatibilidad.calcular(vacante, List.of(tiene(JAVA, NivelHabilidad.BASICO))).porcentaje())
                .isEqualTo(new BigDecimal("66.7"));
        assertThat(CalculadoraCompatibilidad.calcular(vacante, List.of(tiene(GIT, NivelHabilidad.BASICO))).porcentaje())
                .isEqualTo(new BigDecimal("33.3"));
    }

    @Test
    void deberiaRedondearAUnDecimalHaciaArribaEnLaMitad() {
        // INTERMEDIO de AVANZADO = 2/3 → 66.666… → 66.7
        assertThat(CalculadoraCompatibilidad.calcular(List.of(obligatoria(JAVA, NivelHabilidad.AVANZADO)),
                List.of(tiene(JAVA, NivelHabilidad.INTERMEDIO))).porcentaje()).isEqualTo(new BigDecimal("66.7"));

        // 4 obligatorias y 4 deseables (pesos 12): solo la mitad de una deseable → 0.5 / 12 = 4.1666… → 4.2
        List<VacanteHabilidad> ocho = List.of(
                obligatoria(UUID.randomUUID(), NivelHabilidad.BASICO), obligatoria(UUID.randomUUID(), NivelHabilidad.BASICO),
                obligatoria(UUID.randomUUID(), NivelHabilidad.BASICO), obligatoria(UUID.randomUUID(), NivelHabilidad.BASICO),
                deseable(UUID.randomUUID(), NivelHabilidad.BASICO), deseable(UUID.randomUUID(), NivelHabilidad.BASICO),
                deseable(UUID.randomUUID(), NivelHabilidad.BASICO), deseable(DOCKER, NivelHabilidad.INTERMEDIO));
        assertThat(CalculadoraCompatibilidad.calcular(ocho, List.of(tiene(DOCKER, NivelHabilidad.BASICO))).porcentaje())
                .isEqualTo(new BigDecimal("4.2"));

        // Mitad exacta: deseable con la mitad del nivel (0.5) y obligatoria cumplida, pesos 1 + 2 + 1 = 4:
        // (0.5 + 2 + 0) / 4 = 62.5 exacto, sin redondeo
        assertThat(CalculadoraCompatibilidad.calcular(
                List.of(deseable(JAVA, NivelHabilidad.INTERMEDIO), obligatoria(SPRING, NivelHabilidad.BASICO),
                        deseable(GIT, NivelHabilidad.BASICO)),
                List.of(tiene(JAVA, NivelHabilidad.BASICO), tiene(SPRING, NivelHabilidad.BASICO))).porcentaje())
                .isEqualTo(new BigDecimal("62.5"));
    }

    @Test
    void deberiaIgnorarLasHabilidadesDelCandidatoQueLaVacanteNoPide() {
        Resultado resultado = CalculadoraCompatibilidad.calcular(
                List.of(obligatoria(JAVA, NivelHabilidad.BASICO)),
                List.of(tiene(JAVA, NivelHabilidad.BASICO), tiene(DOCKER, NivelHabilidad.AVANZADO)));

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("100.0"));
    }

    @Test
    void deberiaContarComoFaltanteUnNivelInsuficienteAunqueSumePuntos() {
        Resultado resultado = CalculadoraCompatibilidad.calcular(
                List.of(obligatoria(JAVA, NivelHabilidad.AVANZADO)), List.of(tiene(JAVA, NivelHabilidad.BASICO)));

        assertThat(resultado.porcentaje()).isEqualTo(new BigDecimal("33.3"));
        assertThat(resultado.faltantes()).containsExactly(
                new HabilidadFaltante(JAVA, NivelHabilidad.AVANZADO, NivelHabilidad.BASICO, true));
    }

    private static VacanteHabilidad obligatoria(UUID id, NivelHabilidad nivel) {
        return new VacanteHabilidad(id, nivel, true);
    }

    private static VacanteHabilidad deseable(UUID id, NivelHabilidad nivel) {
        return new VacanteHabilidad(id, nivel, false);
    }

    private static CandidatoHabilidad tiene(UUID id, NivelHabilidad nivel) {
        return new CandidatoHabilidad(id, nivel);
    }
}
