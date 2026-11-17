package com.primerempleo.application.service;

import com.primerempleo.application.port.in.PostulacionesCandidatoUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.PostulacionDeCandidato.VacantePostulada;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.service.CalculadoraCompatibilidad;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Postulaciones del candidato (HU-05) con las reglas 1, 2 y 6. Orden de los errores: 404 si la vacante no existe,
 * 409 si ya se postuló y 409 si la vacante ya no recibe postulaciones.
 */
@Service
public class PostulacionCandidatoService implements PostulacionesCandidatoUseCase {

    private final PostulacionRepositoryPort postulaciones;
    private final CandidatoRepositoryPort candidatos;
    private final VacanteRepositoryPort vacantes;
    private final EmpresaRepositoryPort empresas;
    private final Clock reloj;

    public PostulacionCandidatoService(PostulacionRepositoryPort postulaciones, CandidatoRepositoryPort candidatos,
                                       VacanteRepositoryPort vacantes, EmpresaRepositoryPort empresas, Clock reloj) {
        this.postulaciones = postulaciones;
        this.candidatos = candidatos;
        this.vacantes = vacantes;
        this.empresas = empresas;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public PostulacionDeCandidato postularse(UUID usuarioId, UUID vacanteId) {
        Candidato candidato = candidatoDelUsuario(usuarioId);
        Vacante vacante = vacantes.buscarPorId(vacanteId).orElseThrow(RecursoNoEncontradoException::vacante);
        if (postulaciones.existe(candidato.id(), vacante.id())) {
            throw RecursoDuplicadoException.postulacionRepetida();
        }
        Instant ahora = Instant.now(reloj);
        if (!vacante.recibePostulaciones(ahora)) {
            throw OperacionNoPermitidaException.vacanteSinPostulaciones();
        }
        // Regla 6: se guarda la compatibilidad de este momento
        Postulacion postulacion = postulaciones.crear(Postulacion.nueva(candidato.id(), vacante.id(),
                CalculadoraCompatibilidad.calcular(vacante, candidato).porcentaje(), ahora));
        Empresa empresa = empresas.buscarConCuentaPorId(vacante.empresaId())
                .map(EmpresaConCuenta::empresa)
                .orElseThrow(RecursoNoEncontradoException::empresa);
        return new PostulacionDeCandidato(postulacion, VacantePostulada.de(vacante, empresa));
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<PostulacionDeCandidato> listarMias(UUID usuarioId, EstadoPostulacion estado,
                                                     SolicitudPagina solicitud) {
        return postulaciones.listarDeCandidato(candidatoDelUsuario(usuarioId).id(), estado, solicitud);
    }

    private Candidato candidatoDelUsuario(UUID usuarioId) {
        return candidatos.buscarConCuentaPorUsuarioId(usuarioId)
                .map(CandidatoConCuenta::candidato)
                .orElseThrow(RecursoNoEncontradoException::candidato);
    }
}
