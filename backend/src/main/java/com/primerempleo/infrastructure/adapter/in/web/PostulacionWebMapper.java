package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.PostulacionDeCandidato.VacantePostulada;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce las postulaciones del dominio a sus DTOs. */
@Mapper
public interface PostulacionWebMapper {

    @Mapping(target = ".", source = "postulacion")
    @Mapping(target = "vacante", source = "vacante")
    PostulacionDeCandidatoDto aDto(PostulacionDeCandidato postulacion);

    @Mapping(target = "empresa.id", source = "empresaId")
    @Mapping(target = "empresa.razonSocial", source = "razonSocialEmpresa")
    PostulacionDeCandidatoDto.VacanteDto aVacante(VacantePostulada vacante);
}
