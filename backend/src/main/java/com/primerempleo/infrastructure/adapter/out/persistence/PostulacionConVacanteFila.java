package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Fila de "Mis postulaciones": la postulación con los datos de su vacante y la razón social de la empresa.
 * La crea Hibernate con la expresión {@code select new ...} de {@link PostulacionJpaRepository}.
 */
public record PostulacionConVacanteFila(
        PostulacionEntity postulacion,
        String titulo,
        UUID empresaId,
        String razonSocialEmpresa,
        Modalidad modalidad,
        String ciudad,
        EstadoVacante estado,
        LocalDate fechaCierre) {
}
