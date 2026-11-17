package com.primerempleo.application.service;

import com.primerempleo.application.port.in.ConsultarCompatibilidadUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CompatibilidadDetalle;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.service.CalculadoraCompatibilidad;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Compatibilidad del candidato con una vacante (HU-04). Se calcula al consultar con las habilidades de hoy;
 * si ya se postuló, se adjunta la postulación con la compatibilidad que se guardó entonces (regla 6).
 */
@Service
public class CompatibilidadService implements ConsultarCompatibilidadUseCase {

    private final CandidatoRepositoryPort candidatos;
    private final VacanteRepositoryPort vacantes;
    private final HabilidadRepositoryPort habilidades;
    private final PostulacionRepositoryPort postulaciones;

    public CompatibilidadService(CandidatoRepositoryPort candidatos, VacanteRepositoryPort vacantes,
                                 HabilidadRepositoryPort habilidades, PostulacionRepositoryPort postulaciones) {
        this.candidatos = candidatos;
        this.vacantes = vacantes;
        this.habilidades = habilidades;
        this.postulaciones = postulaciones;
    }

    @Override
    @Transactional(readOnly = true)
    public CompatibilidadDetalle consultar(UUID usuarioId, UUID vacanteId) {
        Candidato candidato = candidatos.buscarConCuentaPorUsuarioId(usuarioId)
                .map(CandidatoConCuenta::candidato)
                .orElseThrow(RecursoNoEncontradoException::candidato);
        Vacante vacante = vacantes.buscarPorId(vacanteId).orElseThrow(RecursoNoEncontradoException::vacante);
        return CalculadoraCompatibilidad.detallar(vacante, candidato,
                habilidades.buscarPorIds(vacante.idsHabilidades()),
                postulaciones.buscar(candidato.id(), vacanteId).orElse(null));
    }
}
