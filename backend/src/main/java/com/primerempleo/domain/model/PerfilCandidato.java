package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Perfil del candidato con los datos del catálogo de cada habilidad (docs/contrato-api.md, 5.1).
 */
public record PerfilCandidato(CandidatoConCuenta cuenta, List<HabilidadDeCandidato> habilidades) {

    /** Habilidad declarada por el candidato, con su nombre y categoría del catálogo. */
    public record HabilidadDeCandidato(Habilidad habilidad, NivelHabilidad nivel) {

        /** Mismo orden que el catálogo (4.1): por categoría y luego por nombre. */
        public static final Comparator<HabilidadDeCandidato> ORDEN_CATALOGO =
                Comparator.comparing(HabilidadDeCandidato::habilidad, Habilidad.ORDEN_CATALOGO);

        public HabilidadDeCandidato {
            Objects.requireNonNull(habilidad, "habilidad");
            Objects.requireNonNull(nivel, "nivel");
        }
    }

    public PerfilCandidato {
        Objects.requireNonNull(cuenta, "cuenta");
        habilidades = List.copyOf(habilidades);
    }

    /**
     * Une cada habilidad del candidato con su elemento del catálogo y las ordena como el catálogo.
     *
     * @throws DatosInvalidosException si alguna habilidad no está en el catálogo
     */
    public static PerfilCandidato de(CandidatoConCuenta cuenta, Collection<Habilidad> catalogo) {
        Map<UUID, Habilidad> porId = catalogo.stream()
                .collect(Collectors.toMap(Habilidad::id, Function.identity(), (a, b) -> a));
        List<HabilidadDeCandidato> habilidades = cuenta.candidato().habilidades().stream()
                .map(declarada -> {
                    Habilidad habilidad = porId.get(declarada.habilidadId());
                    if (habilidad == null) {
                        throw new DatosInvalidosException(
                                "habilidades: la habilidad " + declarada.habilidadId() + " no existe");
                    }
                    return new HabilidadDeCandidato(habilidad, declarada.nivel());
                })
                .sorted(HabilidadDeCandidato.ORDEN_CATALOGO)
                .toList();
        return new PerfilCandidato(cuenta, habilidades);
    }
}
