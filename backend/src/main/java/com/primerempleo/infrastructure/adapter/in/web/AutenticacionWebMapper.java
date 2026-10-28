package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.IniciarSesionUseCase.IniciarSesionComando;
import com.primerempleo.application.port.in.RegistrarCandidatoUseCase.RegistrarCandidatoComando;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaComando;
import org.mapstruct.Mapper;

/** Traduce los DTOs de autenticación a los comandos de los puertos de entrada. */
@Mapper
public interface AutenticacionWebMapper {

    RegistrarCandidatoComando aComando(RegistroCandidatoPeticion peticion);

    RegistrarEmpresaComando aComando(RegistroEmpresaPeticion peticion);

    IniciarSesionComando aComando(LoginPeticion peticion);
}
