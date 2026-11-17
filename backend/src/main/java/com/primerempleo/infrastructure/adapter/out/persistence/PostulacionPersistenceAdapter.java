package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.PostulacionDeCandidato.VacantePostulada;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class PostulacionPersistenceAdapter implements PostulacionRepositoryPort {

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

    private PostulacionDeCandidato aPostulacionDeCandidato(PostulacionConVacanteFila fila) {
        Postulacion postulacion = mapper.aDominio(fila.postulacion());
        return new PostulacionDeCandidato(postulacion, new VacantePostulada(postulacion.vacanteId(), fila.titulo(),
                fila.empresaId(), fila.razonSocialEmpresa(), fila.modalidad(), fila.ciudad(), fila.estado(),
                fila.fechaCierre()));
    }
}
