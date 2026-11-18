package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.Postulante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce el postulante del dominio a su DTO; el candidato usa el mismo formato que su perfil (5.1). */
@Mapper(uses = CandidatoWebMapper.class)
public interface PostulanteWebMapper {

    @Mapping(target = ".", source = "postulacion")
    @Mapping(target = "candidato", source = "candidato")
    PostulanteDto aDto(Postulante postulante);
}
