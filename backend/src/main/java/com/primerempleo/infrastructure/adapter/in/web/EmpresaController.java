package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.PerfilEmpresaUseCase;
import com.primerempleo.application.port.in.VerificacionEmpresaUseCase;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil de empresa (EMPRESA, docs/contrato-api.md 4.2) y verificación de empresas (ADMIN, 4.3).
 */
@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    private final PerfilEmpresaUseCase perfilEmpresa;
    private final VerificacionEmpresaUseCase verificacionEmpresa;
    private final EmpresaWebMapper mapper;

    public EmpresaController(PerfilEmpresaUseCase perfilEmpresa, VerificacionEmpresaUseCase verificacionEmpresa,
                             EmpresaWebMapper mapper) {
        this.perfilEmpresa = perfilEmpresa;
        this.verificacionEmpresa = verificacionEmpresa;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('EMPRESA')")
    public EmpresaPerfilDto miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mapper.aPerfil(perfilEmpresa.consultar(usuario.id()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('EMPRESA')")
    public EmpresaPerfilDto actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                               @Valid @RequestBody ActualizarEmpresaPeticion peticion) {
        return mapper.aPerfil(perfilEmpresa.actualizar(mapper.aComando(usuario.id(), peticion)));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PaginaDto<EmpresaResumenDto> listar(
            @RequestParam(required = false) Boolean verificada,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + SolicitudPagina.TAMANO_POR_DEFECTO) int size) {
        return PaginaDto.de(verificacionEmpresa.listar(verificada, new SolicitudPagina(page, size)), mapper::aResumen);
    }

    @PatchMapping("/{id}/verificar")
    @PreAuthorize("hasRole('ADMIN')")
    public EmpresaResumenDto verificar(@PathVariable UUID id) {
        return mapper.aResumen(verificacionEmpresa.verificar(id));
    }
}
