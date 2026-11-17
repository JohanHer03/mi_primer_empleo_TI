package com.primerempleo.application.service;

import com.primerempleo.application.port.in.BuscarVacantesUseCase;
import com.primerempleo.application.port.out.CandidatoRepositoryPort;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.PostulacionRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.FiltroVacantes;
import com.primerempleo.domain.model.OrdenVacantes;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteResumen;
import com.primerempleo.domain.service.CalculadoraCompatibilidad;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Búsqueda de vacantes del candidato (HU-03). Como el orden por defecto es un valor calculado (la compatibilidad,
 * que no se guarda), filtra en la base, calcula y ordena en memoria y después pagina (docs/contrato-api.md, 3.3):
 * suficiente para el volumen del MVP.
 */
@Service
public class BusquedaVacantesService implements BuscarVacantesUseCase {

    private final VacanteRepositoryPort vacantes;
    private final CandidatoRepositoryPort candidatos;
    private final EmpresaRepositoryPort empresas;
    private final PostulacionRepositoryPort postulaciones;
    private final Clock reloj;

    public BusquedaVacantesService(VacanteRepositoryPort vacantes, CandidatoRepositoryPort candidatos,
                                   EmpresaRepositoryPort empresas, PostulacionRepositoryPort postulaciones,
                                   Clock reloj) {
        this.vacantes = vacantes;
        this.candidatos = candidatos;
        this.empresas = empresas;
        this.postulaciones = postulaciones;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<VacanteResumen> buscar(UUID usuarioId, FiltroVacantes filtro, OrdenVacantes orden,
                                         SolicitudPagina solicitud) {
        Candidato candidato = candidatos.buscarConCuentaPorUsuarioId(usuarioId)
                .map(CandidatoConCuenta::candidato)
                .orElseThrow(RecursoNoEncontradoException::candidato);
        LocalDate hoy = LocalDate.ofInstant(Instant.now(reloj), Vacante.ZONA_COLOMBIA);
        List<Vacante> abiertas = vacantes.buscarAbiertas(filtro, hoy);
        if (abiertas.isEmpty()) {
            return Pagina.de(List.of(), solicitud);
        }

        Map<UUID, Empresa> empresasPorId = empresas.buscarPorIds(
                        abiertas.stream().map(Vacante::empresaId).collect(Collectors.toSet())).stream()
                .collect(Collectors.toMap(Empresa::id, Function.identity()));
        Set<UUID> postuladas = postulaciones.idsVacantesPostuladas(candidato.id());

        List<VacanteResumen> resultados = abiertas.stream()
                .map(vacante -> new VacanteResumen(vacante, empresasPorId.get(vacante.empresaId()),
                        CalculadoraCompatibilidad.calcular(vacante, candidato).porcentaje(),
                        postuladas.contains(vacante.id())))
                .sorted(orden.comparador())
                .toList();
        return Pagina.de(resultados, solicitud);
    }
}
