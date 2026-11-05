package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.HabilidadDeVacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import com.primerempleo.domain.model.VacanteDetalle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce entre los DTOs de vacantes y el modelo de dominio. */
@Mapper
public interface VacanteWebMapper {

    DatosVacante aDatos(VacantePeticion peticion);

    @Mapping(target = ".", source = "vacante")
    @Mapping(target = "empresa", source = "empresa")
    @Mapping(target = "habilidades", source = "habilidades")
    VacanteDetalleDto aDetalle(VacanteDetalle detalle);

    @Mapping(target = "habilidadId", source = "habilidad.id")
    @Mapping(target = "nombre", source = "habilidad.nombre")
    @Mapping(target = "categoria", source = "habilidad.categoria")
    VacanteDetalleDto.HabilidadRequeridaDto aHabilidad(HabilidadDeVacante habilidad);

    VacanteDeEmpresaResumenDto aResumen(VacanteDeEmpresaResumen resumen);
}
