package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.CrearHabilidadUseCase;
import com.primerempleo.application.port.in.ListarHabilidadesUseCase;
import com.primerempleo.domain.model.CategoriaHabilidad;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Catálogo de habilidades (docs/contrato-api.md, 4.1). Listar: cualquier usuario autenticado; crear: ADMIN.
 */
@RestController
@RequestMapping("/api/habilidades")
public class HabilidadController {

    private final ListarHabilidadesUseCase listarHabilidades;
    private final CrearHabilidadUseCase crearHabilidad;
    private final HabilidadWebMapper mapper;

    public HabilidadController(ListarHabilidadesUseCase listarHabilidades, CrearHabilidadUseCase crearHabilidad,
                               HabilidadWebMapper mapper) {
        this.listarHabilidades = listarHabilidades;
        this.crearHabilidad = crearHabilidad;
        this.mapper = mapper;
    }

    /** Lista completa, sin paginar: alimenta los selectores de los formularios. */
    @GetMapping
    public List<HabilidadDto> listar(@RequestParam(required = false) CategoriaHabilidad categoria) {
        return mapper.aDtos(listarHabilidades.listar(categoria));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public HabilidadDto crear(@Valid @RequestBody CrearHabilidadPeticion peticion) {
        return mapper.aDto(crearHabilidad.crear(mapper.aComando(peticion)));
    }
}
