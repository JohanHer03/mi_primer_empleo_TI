package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.PerfilCandidato;
import java.util.List;
import java.util.UUID;

/**
 * HU-02: el candidato consulta y completa su perfil y sus habilidades (docs/contrato-api.md, 5.1 y 5.2).
 * Siempre trabaja sobre su propio perfil: el id de usuario sale del token.
 */
public interface PerfilCandidatoUseCase {

    PerfilCandidato consultar(UUID usuarioId);

    /** Edita los datos del perfil; el correo y las habilidades no cambian. */
    PerfilCandidato actualizar(ActualizarPerfilCandidatoComando comando);

    /** Reemplaza la lista completa de habilidades. Las postulaciones enviadas conservan su compatibilidad (regla 6). */
    PerfilCandidato definirHabilidades(UUID usuarioId, List<CandidatoHabilidad> habilidades);

    record ActualizarPerfilCandidatoComando(
            UUID usuarioId,
            String nombres,
            String apellidos,
            EstadoAcademico estadoAcademico,
            Integer semestre,
            String universidad,
            String programa,
            String ciudad,
            String githubUrl,
            String resumen) {
    }
}
