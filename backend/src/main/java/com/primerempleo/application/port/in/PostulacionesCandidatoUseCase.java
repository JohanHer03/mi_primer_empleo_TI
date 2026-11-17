package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.UUID;

/**
 * HU-05: el candidato se postula y sigue el estado de sus postulaciones (docs/contrato-api.md, 5.5 y 5.6).
 * El candidato sale del usuario del token.
 */
public interface PostulacionesCandidatoUseCase {

    /**
     * Reglas 1 (no se postula dos veces, 409), 2 (solo vacantes abiertas y vigentes, 409)
     * y 6 (la compatibilidad se calcula ahora y se guarda).
     */
    PostulacionDeCandidato postularse(UUID usuarioId, UUID vacanteId);

    /** Las más recientes primero. {@code estado} null lista todas. */
    Pagina<PostulacionDeCandidato> listarMias(UUID usuarioId, EstadoPostulacion estado, SolicitudPagina solicitud);
}
