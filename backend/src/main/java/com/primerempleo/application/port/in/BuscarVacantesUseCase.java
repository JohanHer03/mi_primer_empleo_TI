package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.FiltroVacantes;
import com.primerempleo.domain.model.OrdenVacantes;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.VacanteResumen;
import java.util.UUID;

/**
 * HU-03: el candidato busca vacantes abiertas y vigentes con filtros, ordenadas por compatibilidad
 * (docs/contrato-api.md, 3.3 y 5.4).
 */
public interface BuscarVacantesUseCase {

    Pagina<VacanteResumen> buscar(UUID usuarioId, FiltroVacantes filtro, OrdenVacantes orden, SolicitudPagina solicitud);
}
