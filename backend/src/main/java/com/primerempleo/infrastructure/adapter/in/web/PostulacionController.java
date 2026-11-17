package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.PostulacionesCandidatoUseCase;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Postulaciones del candidato (CANDIDATO, docs/contrato-api.md 5.5 y 5.6). Las reglas 1, 2 y 6 las valida el caso de uso.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('CANDIDATO')")
public class PostulacionController {

    private final PostulacionesCandidatoUseCase postulaciones;
    private final PostulacionWebMapper mapper;

    public PostulacionController(PostulacionesCandidatoUseCase postulaciones, PostulacionWebMapper mapper) {
        this.postulaciones = postulaciones;
        this.mapper = mapper;
    }

    /** Sin cuerpo: la compatibilidad se calcula con las habilidades del candidato y se guarda. */
    @PostMapping("/vacantes/{id}/postulaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PostulacionDeCandidatoDto postularse(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                @PathVariable UUID id) {
        return mapper.aDto(postulaciones.postularse(usuario.id(), id));
    }

    @GetMapping("/postulaciones/me")
    public PaginaDto<PostulacionDeCandidatoDto> misPostulaciones(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestParam(required = false) EstadoPostulacion estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + SolicitudPagina.TAMANO_POR_DEFECTO) int size) {
        return PaginaDto.de(postulaciones.listarMias(usuario.id(), estado, new SolicitudPagina(page, size)), mapper::aDto);
    }
}
