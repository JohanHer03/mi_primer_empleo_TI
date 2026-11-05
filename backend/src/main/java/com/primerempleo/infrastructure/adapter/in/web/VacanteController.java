package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.ConsultaVacantesUseCase;
import com.primerempleo.application.port.in.GestionVacantesUseCase;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vacantes (docs/contrato-api.md, 3.2 y 4.4). Publicar, editar, cerrar y listar las propias: EMPRESA;
 * ver el detalle: cualquier usuario autenticado. La propiedad (regla 4) la valida el caso de uso.
 */
@RestController
@RequestMapping("/api")
public class VacanteController {

    private final GestionVacantesUseCase gestionVacantes;
    private final ConsultaVacantesUseCase consultaVacantes;
    private final VacanteWebMapper mapper;

    public VacanteController(GestionVacantesUseCase gestionVacantes, ConsultaVacantesUseCase consultaVacantes,
                             VacanteWebMapper mapper) {
        this.gestionVacantes = gestionVacantes;
        this.consultaVacantes = consultaVacantes;
        this.mapper = mapper;
    }

    @PostMapping("/vacantes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('EMPRESA')")
    public VacanteDetalleDto publicar(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                      @Valid @RequestBody VacantePeticion peticion) {
        return mapper.aDetalle(gestionVacantes.publicar(usuario.id(), mapper.aDatos(peticion)));
    }

    @PutMapping("/vacantes/{id}")
    @PreAuthorize("hasRole('EMPRESA')")
    public VacanteDetalleDto editar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id,
                                    @Valid @RequestBody VacantePeticion peticion) {
        return mapper.aDetalle(gestionVacantes.editar(usuario.id(), id, mapper.aDatos(peticion)));
    }

    @PatchMapping("/vacantes/{id}/cerrar")
    @PreAuthorize("hasRole('EMPRESA')")
    public VacanteDetalleDto cerrar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        return mapper.aDetalle(gestionVacantes.cerrar(usuario.id(), id));
    }

    @GetMapping("/vacantes/{id}")
    public VacanteDetalleDto consultar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        return mapper.aDetalle(consultaVacantes.consultar(usuario.id(), usuario.rol(), id));
    }

    @GetMapping("/empresas/me/vacantes")
    @PreAuthorize("hasRole('EMPRESA')")
    public PaginaDto<VacanteDeEmpresaResumenDto> listarMisVacantes(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) EstadoVacante estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + SolicitudPagina.TAMANO_POR_DEFECTO) int size) {
        return PaginaDto.de(consultaVacantes.listarDeMiEmpresa(usuario.id(), estado, new SolicitudPagina(page, size)),
                mapper::aResumen);
    }
}
