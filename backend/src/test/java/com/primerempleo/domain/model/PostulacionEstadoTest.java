package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

/** Regla 5: transiciones de estado de la postulación (docs/contrato-api.md, 7.2). */
class PostulacionEstadoTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "ENVIADA, EN_REVISION",
            "ENVIADA, RECHAZADA",
            "EN_REVISION, ENTREVISTA",
            "EN_REVISION, RECHAZADA",
            "ENTREVISTA, CONTRATADO",
            "ENTREVISTA, RECHAZADA"
    })
    void deberiaPermitirLasTransicionesDeLaRegla5(EstadoPostulacion desde, EstadoPostulacion hacia) {
        Postulacion postulacion = en(desde);

        Postulacion cambiada = postulacion.cambiarEstado(hacia);

        assertThat(cambiada.estado()).isEqualTo(hacia);
        // Solo cambia el estado: la compatibilidad guardada no se toca (regla 6)
        assertThat(cambiada).usingRecursiveComparison().ignoringFields("estado").isEqualTo(postulacion);
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "ENVIADA, ENTREVISTA",
            "ENVIADA, CONTRATADO",
            "ENVIADA, ENVIADA",
            "EN_REVISION, CONTRATADO",
            "EN_REVISION, ENVIADA",
            "EN_REVISION, EN_REVISION",
            "ENTREVISTA, EN_REVISION",
            "ENTREVISTA, ENVIADA",
            "ENTREVISTA, ENTREVISTA"
    })
    void deberiaRechazarSaltosRetrocesosYElMismoEstado(EstadoPostulacion desde, EstadoPostulacion hacia) {
        assertThatThrownBy(() -> en(desde).cambiarEstado(hacia))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessageStartingWith("Una postulación " + desde.name() + " solo puede pasar a");
    }

    @ParameterizedTest
    @EnumSource(EstadoPostulacion.class)
    void noDeberiaSalirDeLosEstadosFinales(EstadoPostulacion hacia) {
        assertThatThrownBy(() -> en(EstadoPostulacion.CONTRATADO).cambiarEstado(hacia))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessage("Una postulación CONTRATADO ya no cambia de estado");
        assertThatThrownBy(() -> en(EstadoPostulacion.RECHAZADA).cambiarEstado(hacia))
                .isInstanceOf(OperacionNoPermitidaException.class)
                .hasMessage("Una postulación RECHAZADA ya no cambia de estado");
    }

    @Test
    void deberiaDecirAQueEstadosPuedePasarPrimeroElAvance() {
        assertThatThrownBy(() -> en(EstadoPostulacion.ENVIADA).cambiarEstado(EstadoPostulacion.CONTRATADO))
                .hasMessage("Una postulación ENVIADA solo puede pasar a EN_REVISION o RECHAZADA");
        assertThatThrownBy(() -> en(EstadoPostulacion.ENTREVISTA).cambiarEstado(EstadoPostulacion.ENVIADA))
                .hasMessage("Una postulación ENTREVISTA solo puede pasar a CONTRATADO o RECHAZADA");
    }

    private static Postulacion en(EstadoPostulacion estado) {
        return new Postulacion(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                Instant.parse("2026-11-10T14:05:00Z"), estado, new BigDecimal("60.0"));
    }
}
