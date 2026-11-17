package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.CompatibilidadDetalle;
import com.primerempleo.domain.model.CompatibilidadDetalle.HabilidadEvaluada;
import com.primerempleo.domain.model.Postulacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce el detalle de compatibilidad del dominio a su DTO. */
@Mapper
public interface CompatibilidadWebMapper {

    CompatibilidadDetalleDto aDto(CompatibilidadDetalle detalle);

    @Mapping(target = "habilidadId", source = "requerida.habilidad.id")
    @Mapping(target = "nombre", source = "requerida.habilidad.nombre")
    @Mapping(target = "categoria", source = "requerida.habilidad.categoria")
    @Mapping(target = "nivelRequerido", source = "requerida.nivelRequerido")
    @Mapping(target = "obligatoria", source = "requerida.obligatoria")
    CompatibilidadDetalleDto.HabilidadEvaluadaDto aHabilidad(HabilidadEvaluada habilidad);

    CompatibilidadDetalleDto.PostulacionDto aPostulacion(Postulacion postulacion);
}
