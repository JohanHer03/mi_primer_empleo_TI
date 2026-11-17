package com.primerempleo.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Postulación con los datos de la vacante que ve el candidato en "Mis postulaciones" (docs/contrato-api.md, 5.6).
 */
public record PostulacionDeCandidato(Postulacion postulacion, VacantePostulada vacante) {

    /** Lo que se muestra de la vacante: sin descripción ni habilidades. */
    public record VacantePostulada(
            UUID id,
            String titulo,
            UUID empresaId,
            String razonSocialEmpresa,
            Modalidad modalidad,
            String ciudad,
            EstadoVacante estado,
            LocalDate fechaCierre) {

        public static VacantePostulada de(Vacante vacante, Empresa empresa) {
            return new VacantePostulada(vacante.id(), vacante.titulo(), empresa.id(), empresa.razonSocial(),
                    vacante.modalidad(), vacante.ciudad(), vacante.estado(), vacante.fechaCierre());
        }
    }

    public PostulacionDeCandidato {
        Objects.requireNonNull(postulacion, "postulacion");
        Objects.requireNonNull(vacante, "vacante");
    }
}
