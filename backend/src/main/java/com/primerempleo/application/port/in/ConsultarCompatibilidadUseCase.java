package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.CompatibilidadDetalle;
import java.util.UUID;

/** HU-04: compatibilidad actual del candidato con una vacante y lo que le falta (docs/contrato-api.md 6.1). */
public interface ConsultarCompatibilidadUseCase {

    /** También para vacantes cerradas o vencidas; 404 si la vacante o el candidato no existen. */
    CompatibilidadDetalle consultar(UUID usuarioId, UUID vacanteId);
}
