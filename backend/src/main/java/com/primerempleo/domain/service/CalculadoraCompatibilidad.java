package com.primerempleo.domain.service;

import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteHabilidad;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Compatibilidad de un candidato con una vacante (CLAUDE.md y docs/contrato-api.md, 5.3).
 * Por cada habilidad de la vacante el puntaje es {@code min(1, nivelCandidato / nivelRequerido)} (0 si no la tiene),
 * con peso 2 si es obligatoria y 1 si es deseable. Resultado = suma ponderada / suma de pesos × 100,
 * redondeado a un decimal (mitad hacia arriba). Las habilidades con puntaje menor que 1 son "lo que te falta".
 */
public final class CalculadoraCompatibilidad {

    private static final int PESO_OBLIGATORIA = 2;
    private static final int PESO_DESEABLE = 1;
    /**
     * Mínimo común múltiplo de los niveles (1, 2 y 3): el puntaje multiplicado por 6 siempre es entero,
     * así la suma es exacta y el redondeo no depende de errores de coma flotante.
     */
    private static final int ESCALA_PUNTAJE = 6;

    private CalculadoraCompatibilidad() {
    }

    /** Habilidad de la vacante en la que el candidato no llega al nivel pedido; {@code nivelCandidato} null si no la tiene. */
    public record HabilidadFaltante(UUID habilidadId, NivelHabilidad nivelRequerido, NivelHabilidad nivelCandidato,
                                    boolean obligatoria) {
    }

    /** {@code porcentaje} de 0.0 a 100.0 con un decimal; {@code faltantes} en el orden de la vacante. */
    public record Resultado(BigDecimal porcentaje, List<HabilidadFaltante> faltantes) {

        public Resultado {
            faltantes = List.copyOf(faltantes);
        }
    }

    public static Resultado calcular(Vacante vacante, Candidato candidato) {
        return calcular(vacante.habilidades(), candidato.habilidades());
    }

    public static Resultado calcular(List<VacanteHabilidad> requeridas, List<CandidatoHabilidad> delCandidato) {
        Map<UUID, NivelHabilidad> nivelesCandidato = delCandidato.stream()
                .collect(Collectors.toMap(CandidatoHabilidad::habilidadId, CandidatoHabilidad::nivel, (a, b) -> a));
        long puntos = 0;
        long pesos = 0;
        List<HabilidadFaltante> faltantes = new ArrayList<>();
        for (VacanteHabilidad requerida : requeridas) {
            int peso = requerida.obligatoria() ? PESO_OBLIGATORIA : PESO_DESEABLE;
            NivelHabilidad nivelCandidato = nivelesCandidato.get(requerida.habilidadId());
            int puntaje = puntajeEscalado(nivelCandidato, requerida.nivelRequerido());
            puntos += (long) peso * puntaje;
            pesos += peso;
            if (puntaje < ESCALA_PUNTAJE) {
                faltantes.add(new HabilidadFaltante(requerida.habilidadId(), requerida.nivelRequerido(), nivelCandidato,
                        requerida.obligatoria()));
            }
        }
        if (pesos == 0) {
            return new Resultado(BigDecimal.ZERO.setScale(1), faltantes);
        }
        BigDecimal porcentaje = BigDecimal.valueOf(puntos * 100)
                .divide(BigDecimal.valueOf(pesos * ESCALA_PUNTAJE), 1, RoundingMode.HALF_UP);
        return new Resultado(porcentaje, faltantes);
    }

    /** {@code min(1, nivelCandidato / nivelRequerido) × 6}; 0 si el candidato no tiene la habilidad. */
    private static int puntajeEscalado(NivelHabilidad nivelCandidato, NivelHabilidad nivelRequerido) {
        if (nivelCandidato == null) {
            return 0;
        }
        int candidato = nivelCandidato.valor();
        int requerido = nivelRequerido.valor();
        return candidato >= requerido ? ESCALA_PUNTAJE : candidato * ESCALA_PUNTAJE / requerido;
    }
}
