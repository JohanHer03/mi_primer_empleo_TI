package com.primerempleo.application.service;

import com.primerempleo.application.port.in.PerfilCandidatoUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.PerfilCandidato;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Perfil y habilidades del candidato autenticado (HU-02). Las reglas de los datos las valida el dominio
 * y que cada habilidad exista en el catálogo, {@link PerfilCandidato#de}, antes de guardar.
 */
@Service
public class PerfilCandidatoService implements PerfilCandidatoUseCase {

    private final CandidatoRepositoryPort candidatos;
    private final HabilidadRepositoryPort habilidades;

    public PerfilCandidatoService(CandidatoRepositoryPort candidatos, HabilidadRepositoryPort habilidades) {
        this.candidatos = candidatos;
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilCandidato consultar(UUID usuarioId) {
        return detallar(buscar(usuarioId));
    }

    @Override
    @Transactional
    public PerfilCandidato actualizar(ActualizarPerfilCandidatoComando comando) {
        CandidatoConCuenta actual = buscar(comando.usuarioId());
        Candidato editado = actual.candidato().conPerfil(comando.nombres(), comando.apellidos(), comando.universidad(),
                comando.programa(), comando.estadoAcademico(), comando.semestre(), comando.ciudad(),
                comando.githubUrl(), comando.resumen());
        return guardar(actual.conCandidato(editado));
    }

    @Override
    @Transactional
    public PerfilCandidato definirHabilidades(UUID usuarioId, List<CandidatoHabilidad> nuevas) {
        CandidatoConCuenta actual = buscar(usuarioId);
        return guardar(actual.conCandidato(actual.candidato().conHabilidades(nuevas)));
    }

    private CandidatoConCuenta buscar(UUID usuarioId) {
        return candidatos.buscarConCuentaPorUsuarioId(usuarioId)
                .orElseThrow(RecursoNoEncontradoException::candidato);
    }

    /** Valida contra el catálogo (400) antes de guardar. */
    private PerfilCandidato guardar(CandidatoConCuenta editado) {
        PerfilCandidato perfil = detallar(editado);
        candidatos.actualizar(editado.candidato());
        return perfil;
    }

    private PerfilCandidato detallar(CandidatoConCuenta cuenta) {
        List<UUID> ids = cuenta.candidato().idsHabilidades();
        return PerfilCandidato.de(cuenta, ids.isEmpty() ? List.of() : habilidades.buscarPorIds(ids));
    }
}
