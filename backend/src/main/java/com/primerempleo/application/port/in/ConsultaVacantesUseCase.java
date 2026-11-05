package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import com.primerempleo.domain.model.VacanteDetalle;
import java.util.UUID;

/**
 * Lectura de vacantes (docs/contrato-api.md, 3.2 y 4.4).
 */
public interface ConsultaVacantesUseCase {

    /**
     * Candidatos y administrador ven cualquier vacante, también las cerradas;
     * una empresa solo las suyas (regla 4).
     */
    VacanteDetalle consultar(UUID usuarioId, Rol rol, UUID vacanteId);

    /** Vacantes de la empresa del usuario, las más recientes primero. {@code estado} null lista todas. */
    Pagina<VacanteDeEmpresaResumen> listarDeMiEmpresa(UUID usuarioId, EstadoVacante estado, SolicitudPagina solicitud);
}
