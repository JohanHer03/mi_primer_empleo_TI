package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.IniciarSesionUseCase;
import com.primerempleo.application.port.in.RegistrarCandidatoUseCase;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro e inicio de sesión (docs/contrato-api.md, sección 2). Rutas públicas: /api/auth/**.
 * Los errores (400, 401, 403, 409) salen por ManejadorGlobalErrores.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacionController {

    private final RegistrarCandidatoUseCase registrarCandidato;
    private final RegistrarEmpresaUseCase registrarEmpresa;
    private final IniciarSesionUseCase iniciarSesion;
    private final AutenticacionWebMapper mapper;

    public AutenticacionController(RegistrarCandidatoUseCase registrarCandidato,
                                   RegistrarEmpresaUseCase registrarEmpresa,
                                   IniciarSesionUseCase iniciarSesion,
                                   AutenticacionWebMapper mapper) {
        this.registrarCandidato = registrarCandidato;
        this.registrarEmpresa = registrarEmpresa;
        this.iniciarSesion = iniciarSesion;
        this.mapper = mapper;
    }

    @PostMapping("/registro/candidato")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRespuesta registrarCandidato(@Valid @RequestBody RegistroCandidatoPeticion peticion) {
        return new AuthRespuesta(registrarCandidato.registrar(mapper.aComando(peticion)));
    }

    @PostMapping("/registro/empresa")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthRespuesta registrarEmpresa(@Valid @RequestBody RegistroEmpresaPeticion peticion) {
        return new AuthRespuesta(registrarEmpresa.registrar(mapper.aComando(peticion)));
    }

    @PostMapping("/login")
    public AuthRespuesta iniciarSesion(@Valid @RequestBody LoginPeticion peticion) {
        return new AuthRespuesta(iniciarSesion.iniciarSesion(mapper.aComando(peticion)));
    }
}
