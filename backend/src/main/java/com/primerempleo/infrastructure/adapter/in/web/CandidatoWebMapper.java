package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.PerfilCandidatoUseCase.ActualizarPerfilCandidatoComando;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.PerfilCandidato;
import com.primerempleo.domain.model.PerfilCandidato.HabilidadDeCandidato;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce entre los DTOs del candidato, los comandos y el modelo de dominio. */
@Mapper
public interface CandidatoWebMapper {

    @Mapping(target = ".", source = "cuenta.candidato")
    @Mapping(target = "email", source = "cuenta.email")
    @Mapping(target = "habilidades", source = "habilidades")
    CandidatoPerfilDto aPerfil(PerfilCandidato perfil);

    @Mapping(target = "habilidadId", source = "habilidad.id")
    @Mapping(target = "nombre", source = "habilidad.nombre")
    @Mapping(target = "categoria", source = "habilidad.categoria")
    CandidatoHabilidadDto aHabilidad(HabilidadDeCandidato habilidad);

    List<CandidatoHabilidadDto> aHabilidades(List<HabilidadDeCandidato> habilidades);

    ActualizarPerfilCandidatoComando aComando(UUID usuarioId, ActualizarCandidatoPeticion peticion);

    List<CandidatoHabilidad> aDominio(List<CandidatoHabilidadPeticion> peticion);
}
