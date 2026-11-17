package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.ConsultarCompatibilidadUseCase;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Compatibilidad del candidato con una vacante y "lo que te falta" (CANDIDATO, docs/contrato-api.md 6.1, HU-04). */
@RestController
@RequestMapping("/api/vacantes")
@PreAuthorize("hasRole('CANDIDATO')")
public class CompatibilidadController {

    private final ConsultarCompatibilidadUseCase compatibilidad;
    private final CompatibilidadWebMapper mapper;

    public CompatibilidadController(ConsultarCompatibilidadUseCase compatibilidad, CompatibilidadWebMapper mapper) {
        this.compatibilidad = compatibilidad;
        this.mapper = mapper;
    }

    @GetMapping("/{id}/compatibilidad")
    public CompatibilidadDetalleDto consultar(@AuthenticationPrincipal UsuarioAutenticado usuario, @PathVariable UUID id) {
        return mapper.aDto(compatibilidad.consultar(usuario.id(), id));
    }
}
