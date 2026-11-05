package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.EstadoAcademico;
import java.util.List;
import java.util.UUID;

/** Perfil del candidato autenticado (`CandidatoPerfil` de docs/contrato-api.md, 5.1). */
public record CandidatoPerfilDto(
        UUID id,
        String email,
        String nombres,
        String apellidos,
        EstadoAcademico estadoAcademico,
        Integer semestre,
        String universidad,
        String programa,
        String ciudad,
        String githubUrl,
        String resumen,
        List<CandidatoHabilidadDto> habilidades) {
}
