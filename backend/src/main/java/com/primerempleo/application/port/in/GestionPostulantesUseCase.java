package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulante;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.UUID;

/** La empresa gestiona los postulantes de sus vacantes (HU-08 y HU-09, reglas 4 y 5; docs/contrato-api.md 7). */
public interface GestionPostulantesUseCase {

    /**
     * Postulantes de una vacante de la empresa del usuario, por compatibilidad guardada (mayor primero) y fecha.
     * {@code estado} null lista todos.
     */
    Pagina<Postulante> listarDeVacante(UUID usuarioId, UUID vacanteId, EstadoPostulacion estado, SolicitudPagina solicitud);

    /** Avanza o rechaza una postulación de una vacante de la empresa del usuario. */
    Postulante cambiarEstado(UUID usuarioId, UUID postulacionId, EstadoPostulacion nuevo);
}
