package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.CrearHabilidadUseCase.CrearHabilidadComando;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;
import org.mapstruct.Mapper;

/** Traduce entre los DTOs del catálogo, los comandos y el modelo de dominio. */
@Mapper
public interface HabilidadWebMapper {

    CrearHabilidadComando aComando(CrearHabilidadPeticion peticion);

    HabilidadDto aDto(Habilidad habilidad);

    List<HabilidadDto> aDtos(List<Habilidad> habilidades);
}
