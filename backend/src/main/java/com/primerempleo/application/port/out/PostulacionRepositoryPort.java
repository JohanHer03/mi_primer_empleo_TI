package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.Set;
import java.util.UUID;

public interface PostulacionRepositoryPort {

    /** Ids de las vacantes a las que el candidato ya se postuló (regla 1). */
    Set<UUID> idsVacantesPostuladas(UUID candidatoId);

    boolean existe(UUID candidatoId, UUID vacanteId);

    /** Inserta una postulación nueva. Si ya existía (dos peticiones a la vez), la base la rechaza con su restricción. */
    Postulacion crear(Postulacion postulacion);

    /**
     * Postulaciones del candidato con los datos de su vacante, las más recientes primero.
     * {@code estado} null lista todas.
     */
    Pagina<PostulacionDeCandidato> listarDeCandidato(UUID candidatoId, EstadoPostulacion estado,
                                                     SolicitudPagina solicitud);
}
