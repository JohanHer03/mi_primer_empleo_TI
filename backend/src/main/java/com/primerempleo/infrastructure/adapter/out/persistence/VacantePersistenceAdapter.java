package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class VacantePersistenceAdapter implements VacanteRepositoryPort {

    private static final Sort MAS_RECIENTES_PRIMERO = Sort.by(
            Sort.Order.desc("fechaPublicacion"), Sort.Order.desc("id"));

    private final VacanteJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public VacantePersistenceAdapter(VacanteJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Vacante crear(Vacante vacante) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(vacante)));
    }

    @Override
    public Vacante actualizar(Vacante vacante) {
        VacanteEntity entidad = repositorio.findById(vacante.id())
                .orElseThrow(RecursoNoEncontradoException::vacante);
        mapper.copiar(vacante, entidad);
        return mapper.aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<Vacante> buscarPorId(UUID vacanteId) {
        return repositorio.findById(vacanteId).map(mapper::aDominio);
    }

    @Override
    public Pagina<VacanteDeEmpresaResumen> listarDeEmpresa(UUID empresaId, EstadoVacante estado,
                                                           SolicitudPagina solicitud) {
        PageRequest pedido = PageRequest.of(solicitud.pagina(), solicitud.tamano(), MAS_RECIENTES_PRIMERO);
        Page<VacanteEntity> filas = estado == null
                ? repositorio.findByEmpresaId(empresaId, pedido)
                : repositorio.findByEmpresaIdAndEstado(empresaId, estado, pedido);
        Map<UUID, Long> postulaciones = contarPostulaciones(filas.getContent());
        return new Pagina<>(
                filas.getContent().stream()
                        .map(fila -> mapper.aResumen(fila, postulaciones.getOrDefault(fila.getId(), 0L)))
                        .toList(),
                filas.getNumber(), filas.getSize(), filas.getTotalElements(), filas.getTotalPages());
    }

    /** Una sola consulta para toda la página; las vacantes sin postulaciones no aparecen en el resultado. */
    private Map<UUID, Long> contarPostulaciones(List<VacanteEntity> vacantes) {
        Map<UUID, Long> totales = new HashMap<>();
        if (vacantes.isEmpty()) {
            return totales;
        }
        List<UUID> ids = vacantes.stream().map(VacanteEntity::getId).toList();
        for (Object[] fila : repositorio.contarPostulaciones(ids)) {
            totales.put((UUID) fila[0], ((Number) fila[1]).longValue());
        }
        return totales;
    }
}
