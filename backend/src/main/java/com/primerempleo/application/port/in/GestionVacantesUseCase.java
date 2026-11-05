package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.VacanteDetalle;
import java.util.UUID;

/**
 * HU-07: la empresa publica, edita y cierra sus vacantes (docs/contrato-api.md, 4.4).
 * La empresa sale del usuario del token; solo las verificadas publican (regla 3)
 * y cada una solo modifica las suyas (regla 4).
 */
public interface GestionVacantesUseCase {

    VacanteDetalle publicar(UUID usuarioId, DatosVacante datos);

    /** Reemplaza todos los datos, incluida la lista de habilidades. Una vacante cerrada no se edita (409). */
    VacanteDetalle editar(UUID usuarioId, UUID vacanteId, DatosVacante datos);

    /** Idempotente: cerrar una vacante ya cerrada no cambia nada. */
    VacanteDetalle cerrar(UUID usuarioId, UUID vacanteId);
}
