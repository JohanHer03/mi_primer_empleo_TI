package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.PostulacionDeCandidato.VacantePostulada;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PostulacionPersistenceAdapter implements PostulacionRepositoryPort {

    /** HU-08: mayor compatibilidad guardada primero; con empate, quien se postuló antes (el id deja el orden estable). */
    private static final Sort ORDEN_POSTULANTES = Sort.by(Sort.Order.desc("compatibilidad"), Sort.Order.asc("fecha"),
            Sort.Order.asc("id"));

    private final PostulacionJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public PostulacionPersistenceAdapter(PostulacionJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public Set<UUID> idsVacantesPostuladas(UUID candidatoId) {
        return new HashSet<>(repositorio.idsVacantesDe(candidatoId));
    }

    @Override
    public boolean existe(UUID candidatoId, UUID vacanteId) {
        return repositorio.existsByCandidatoIdAndVacanteId(candidatoId, vacanteId);
    }

    @Override
    public Optional<Postulacion> buscarPorId(UUID id) {
        return repositorio.findById(id).map(mapper::aDominio);
    }

    @Override
    public Optional<Postulacion> buscar(UUID candidatoId, UUID vacanteId) {
        return repositorio.findByCandidatoIdAndVacanteId(candidatoId, vacanteId).map(mapper::aDominio);
    }

    @Override
    public Postulacion crear(Postulacion postulacion) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(postulacion)));
    }

    @Override
    public Pagina<PostulacionDeCandidato> listarDeCandidato(UUID candidatoId, EstadoPostulacion estado,
                                                            SolicitudPagina solicitud) {
        PageRequest pedido = PageRequest.of(solicitud.pagina(), solicitud.tamano());
        Page<PostulacionConVacanteFila> filas = estado == null
                ? repositorio.listarDeCandidato(candidatoId, pedido)
                : repositorio.listarDeCandidatoPorEstado(candidatoId, estado, pedido);
        return new Pagina<>(filas.getContent().stream().map(this::aPostulacionDeCandidato).toList(),
                filas.getNumber(), filas.getSize(), filas.getTotalElements(), filas.getTotalPages());
    }

    @Override
    public Pagina<Postulacion> listarDeVacante(UUID vacanteId, EstadoPostulacion estado, SolicitudPagina solicitud) {
        PageRequest pedido = PageRequest.of(solicitud.pagina(), solicitud.tamano(), ORDEN_POSTULANTES);
        Page<PostulacionEntity> entidades = estado == null
                ? repositorio.findByVacanteId(vacanteId, pedido)
                : repositorio.findByVacanteIdAndEstado(vacanteId, estado, pedido);
        return new Pagina<>(entidades.getContent().stream().map(mapper::aDominio).toList(),
                entidades.getNumber(), entidades.getSize(), entidades.getTotalElements(), entidades.getTotalPages());
    }

    /** Solo cambia el estado: fecha y compatibilidad son {@code updatable = false} (regla 6). */
    @Override
    public Postulacion actualizarEstado(Postulacion postulacion) {
        PostulacionEntity entidad = repositorio.findById(postulacion.id())
                .orElseThrow(RecursoNoEncontradoException::postulacion);
        entidad.setEstado(postulacion.estado());
        return mapper.aDominio(repositorio.save(entidad));
    }

    private PostulacionDeCandidato aPostulacionDeCandidato(PostulacionConVacanteFila fila) {
        Postulacion postulacion = mapper.aDominio(fila.postulacion());
        return new PostulacionDeCandidato(postulacion, new VacantePostulada(postulacion.vacanteId(), fila.titulo(),
                fila.empresaId(), fila.razonSocialEmpresa(), fila.modalidad(), fila.ciudad(), fila.estado(),
                fila.fechaCierre()));
    }
}
