package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.PerfilEmpresaUseCase.ActualizarPerfilEmpresaComando;
import com.primerempleo.domain.model.EmpresaConCuenta;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Traduce entre los DTOs de empresas, los comandos y el modelo de dominio. */
@Mapper
public interface EmpresaWebMapper {

    @Mapping(target = ".", source = "empresa")
    EmpresaPerfilDto aPerfil(EmpresaConCuenta empresa);

    @Mapping(target = ".", source = "empresa")
    EmpresaResumenDto aResumen(EmpresaConCuenta empresa);

    ActualizarPerfilEmpresaComando aComando(UUID usuarioId, ActualizarEmpresaPeticion peticion);
}
