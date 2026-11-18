package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.GestionPostulantesUseCase;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Postulantes de las vacantes de la empresa y cambio de estado (EMPRESA, docs/contrato-api.md 7.1 y 7.2).
 * La propiedad (regla 4) y las transiciones (regla 5) las valida el caso de uso.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('EMPRESA')")
public class GestionPostulantesController {

    private final GestionPostulantesUseCase postulantes;
    private final PostulanteWebMapper mapper;

    public GestionPostulantesController(GestionPostulantesUseCase postulantes, PostulanteWebMapper mapper) {
        this.postulantes = postulantes;
        this.mapper = mapper;
    }

    @GetMapping("/vacantes/{id}/postulaciones")
    public PaginaDto<PostulanteDto> postulantes(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable UUID id,
            @RequestParam(required = false) EstadoPostulacion estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + SolicitudPagina.TAMANO_POR_DEFECTO) int size) {
        return PaginaDto.de(postulantes.listarDeVacante(usuario.id(), id, estado, new SolicitudPagina(page, size)),
                mapper::aDto);
    }

    @PatchMapping("/postulaciones/{id}/estado")
    public PostulanteDto cambiarEstado(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id,
                                       @Valid @RequestBody CambiarEstadoPeticion peticion) {
        return mapper.aDto(postulantes.cambiarEstado(usuario.id(), id, peticion.estado()));
    }
}
