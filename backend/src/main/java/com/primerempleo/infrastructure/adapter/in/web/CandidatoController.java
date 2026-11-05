package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.application.port.in.PerfilCandidatoUseCase;
import com.primerempleo.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil y habilidades del candidato autenticado (CANDIDATO, docs/contrato-api.md 5.1 y 5.2).
 */
@RestController
@RequestMapping("/api/candidatos")
@PreAuthorize("hasRole('CANDIDATO')")
public class CandidatoController {

    private final PerfilCandidatoUseCase perfilCandidato;
    private final CandidatoWebMapper mapper;

    public CandidatoController(PerfilCandidatoUseCase perfilCandidato, CandidatoWebMapper mapper) {
        this.perfilCandidato = perfilCandidato;
        this.mapper = mapper;
    }

    @GetMapping("/me")
    public CandidatoPerfilDto miPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return mapper.aPerfil(perfilCandidato.consultar(usuario.id()));
    }

    @PutMapping("/me")
    public CandidatoPerfilDto actualizarMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario,
                                                 @Valid @RequestBody ActualizarCandidatoPeticion peticion) {
        return mapper.aPerfil(perfilCandidato.actualizar(mapper.aComando(usuario.id(), peticion)));
    }

    /** El cuerpo es un arreglo: Spring valida la lista y cada elemento con su validación de métodos. */
    @PutMapping("/me/habilidades")
    public List<CandidatoHabilidadDto> definirMisHabilidades(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @RequestBody @Size(max = 30, message = "máximo 30")
            List<@NotNull(message = "no puede ser null") @Valid CandidatoHabilidadPeticion> habilidades) {
        return mapper.aHabilidades(
                perfilCandidato.definirHabilidades(usuario.id(), mapper.aDominio(habilidades)).habilidades());
    }
}
