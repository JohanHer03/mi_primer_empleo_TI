package com.primerempleo.application.service;

import com.primerempleo.application.port.in.GestionPostulantesUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.PerfilCandidato;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.Postulante;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestión de postulantes de la empresa (HU-08 y HU-09). La propiedad (regla 4) se comprueba aquí: la vacante,
 * o la vacante de la postulación, debe ser de la empresa del usuario. Las transiciones (regla 5) son del dominio.
 */
@Service
public class GestionPostulantesService implements GestionPostulantesUseCase {

    private final PostulacionRepositoryPort postulaciones;
    private final VacanteRepositoryPort vacantes;
    private final EmpresaRepositoryPort empresas;
    private final CandidatoRepositoryPort candidatos;
    private final HabilidadRepositoryPort habilidades;

    public GestionPostulantesService(PostulacionRepositoryPort postulaciones, VacanteRepositoryPort vacantes,
                                     EmpresaRepositoryPort empresas, CandidatoRepositoryPort candidatos,
                                     HabilidadRepositoryPort habilidades) {
        this.postulaciones = postulaciones;
        this.vacantes = vacantes;
        this.empresas = empresas;
        this.candidatos = candidatos;
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<Postulante> listarDeVacante(UUID usuarioId, UUID vacanteId, EstadoPostulacion estado,
                                              SolicitudPagina solicitud) {
        Empresa empresa = empresaDelUsuario(usuarioId);
        Vacante vacante = vacantes.buscarPorId(vacanteId).orElseThrow(RecursoNoEncontradoException::vacante);
        if (!vacante.esDe(empresa.id())) {
            throw AccesoDenegadoException.vacanteAjena();
        }
        Pagina<Postulacion> pagina = postulaciones.listarDeVacante(vacanteId, estado, solicitud);
        return new Pagina<>(conCandidatos(pagina.contenido()), pagina.pagina(), pagina.tamano(),
                pagina.totalElementos(), pagina.totalPaginas());
    }

    @Override
    @Transactional
    public Postulante cambiarEstado(UUID usuarioId, UUID postulacionId, EstadoPostulacion nuevo) {
        Empresa empresa = empresaDelUsuario(usuarioId);
        Postulacion postulacion = postulaciones.buscarPorId(postulacionId)
                .orElseThrow(RecursoNoEncontradoException::postulacion);
        Vacante vacante = vacantes.buscarPorId(postulacion.vacanteId())
                .orElseThrow(RecursoNoEncontradoException::postulacion);
        if (!vacante.esDe(empresa.id())) {
            throw AccesoDenegadoException.postulacionAjena();
        }
        Postulacion actualizada = postulaciones.actualizarEstado(postulacion.cambiarEstado(nuevo));
        return conCandidatos(List.of(actualizada)).getFirst();
    }

    private Empresa empresaDelUsuario(UUID usuarioId) {
        return empresas.buscarConCuentaPorUsuarioId(usuarioId)
                .map(EmpresaConCuenta::empresa)
                .orElseThrow(RecursoNoEncontradoException::empresa);
    }

    /** Une cada postulación con el perfil de su candidato; una sola consulta de candidatos y otra del catálogo. */
    private List<Postulante> conCandidatos(List<Postulacion> lista) {
        if (lista.isEmpty()) {
            return List.of();
        }
        Map<UUID, CandidatoConCuenta> porId = candidatos.buscarConCuentaPorIds(
                        lista.stream().map(Postulacion::candidatoId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(cuenta -> cuenta.candidato().id(), Function.identity()));
        Set<UUID> idsHabilidades = porId.values().stream()
                .flatMap(cuenta -> cuenta.candidato().habilidades().stream())
                .map(CandidatoHabilidad::habilidadId)
                .collect(Collectors.toSet());
        List<Habilidad> catalogo = idsHabilidades.isEmpty() ? List.of() : habilidades.buscarPorIds(idsHabilidades);
        return lista.stream()
                .map(postulacion -> {
                    CandidatoConCuenta cuenta = porId.get(postulacion.candidatoId());
                    if (cuenta == null) {
                        throw RecursoNoEncontradoException.candidato();
                    }
                    return new Postulante(postulacion, PerfilCandidato.de(cuenta, catalogo));
                })
                .toList();
    }
}
