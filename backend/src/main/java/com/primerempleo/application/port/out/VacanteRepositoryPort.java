package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.FiltroVacantes;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VacanteRepositoryPort {

    /**
     * Inserta una vacante nueva con sus habilidades. No sirve para actualizar: la entidad se marca como nueva
     * y daría clave duplicada. Para editar, usar {@link #actualizar(Vacante)}.
     */
    Vacante crear(Vacante vacante);

    /** Guarda los cambios de una vacante que ya existe; reemplaza su lista de habilidades. */
    Vacante actualizar(Vacante vacante);

    Optional<Vacante> buscarPorId(UUID vacanteId);

    /**
     * Vacantes ABIERTA con fecha de cierre desde {@code hoy} que cumplen los filtros, con sus habilidades.
     * Sin orden ni paginación: la búsqueda ordena por la compatibilidad, que se calcula después.
     */
    List<Vacante> buscarAbiertas(FiltroVacantes filtro, LocalDate hoy);

    /**
     * Vacantes de una empresa con su número de postulaciones, las más recientes primero.
     * {@code estado} null lista todas.
     */
    Pagina<VacanteDeEmpresaResumen> listarDeEmpresa(UUID empresaId, EstadoVacante estado, SolicitudPagina solicitud);
}
