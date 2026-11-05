package com.primerempleo.application.service;

import com.primerempleo.application.port.in.ConsultaVacantesUseCase;
import com.primerempleo.application.port.in.GestionVacantesUseCase;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.application.port.out.VacanteRepositoryPort;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import com.primerempleo.domain.model.VacanteDetalle;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vacantes de las empresas (HU-07). Aquí se validan la regla 3 (solo empresas verificadas publican)
 * y la regla 4 (cada empresa solo ve y modifica sus vacantes).
 * Orden de los errores: 404 si no existe, 403 si es ajena, 409 si está cerrada y por último 400 por los datos.
 */
@Service
public class VacanteService implements GestionVacantesUseCase, ConsultaVacantesUseCase {

    private final VacanteRepositoryPort vacantes;
    private final EmpresaRepositoryPort empresas;
    private final HabilidadRepositoryPort habilidades;
    private final Clock reloj;

    public VacanteService(VacanteRepositoryPort vacantes, EmpresaRepositoryPort empresas,
                          HabilidadRepositoryPort habilidades, Clock reloj) {
        this.vacantes = vacantes;
        this.empresas = empresas;
        this.habilidades = habilidades;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public VacanteDetalle publicar(UUID usuarioId, DatosVacante datos) {
        Empresa empresa = empresaDelUsuario(usuarioId);
        if (!empresa.verificada()) {
            throw AccesoDenegadoException.empresaSinVerificar();
        }
        Vacante vacante = Vacante.nueva(empresa.id(), datos, Instant.now(reloj));
        VacanteDetalle detalle = detallar(vacante, empresa);
        vacantes.crear(vacante);
        return detalle;
    }

    @Override
    @Transactional
    public VacanteDetalle editar(UUID usuarioId, UUID vacanteId, DatosVacante datos) {
        Empresa empresa = empresaDelUsuario(usuarioId);
        Vacante actual = vacanteDe(empresa, vacanteId);
        if (actual.estaCerrada()) {
            throw OperacionNoPermitidaException.vacanteCerrada();
        }
        Vacante editada = actual.editar(datos, Instant.now(reloj));
        VacanteDetalle detalle = detallar(editada, empresa);
        vacantes.actualizar(editada);
        return detalle;
    }

    @Override
    @Transactional
    public VacanteDetalle cerrar(UUID usuarioId, UUID vacanteId) {
        Empresa empresa = empresaDelUsuario(usuarioId);
        Vacante actual = vacanteDe(empresa, vacanteId);
        if (actual.estaCerrada()) {
            return detallar(actual, empresa);
        }
        return detallar(vacantes.actualizar(actual.cerrar()), empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public VacanteDetalle consultar(UUID usuarioId, Rol rol, UUID vacanteId) {
        if (rol == Rol.EMPRESA) {
            Empresa empresa = empresaDelUsuario(usuarioId);
            return detallar(vacanteDe(empresa, vacanteId), empresa);
        }
        Vacante vacante = buscarVacante(vacanteId);
        Empresa empresa = empresas.buscarConCuentaPorId(vacante.empresaId())
                .map(EmpresaConCuenta::empresa)
                .orElseThrow(RecursoNoEncontradoException::empresa);
        return detallar(vacante, empresa);
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<VacanteDeEmpresaResumen> listarDeMiEmpresa(UUID usuarioId, EstadoVacante estado,
                                                             SolicitudPagina solicitud) {
        return vacantes.listarDeEmpresa(empresaDelUsuario(usuarioId).id(), estado, solicitud);
    }

    private Empresa empresaDelUsuario(UUID usuarioId) {
        return empresas.buscarConCuentaPorUsuarioId(usuarioId)
                .map(EmpresaConCuenta::empresa)
                .orElseThrow(RecursoNoEncontradoException::empresa);
    }

    /** La vacante, si existe (404) y es de la empresa (403, regla 4). */
    private Vacante vacanteDe(Empresa empresa, UUID vacanteId) {
        Vacante vacante = buscarVacante(vacanteId);
        if (!vacante.esDe(empresa.id())) {
            throw AccesoDenegadoException.vacanteAjena();
        }
        return vacante;
    }

    private Vacante buscarVacante(UUID vacanteId) {
        return vacantes.buscarPorId(vacanteId).orElseThrow(RecursoNoEncontradoException::vacante);
    }

    /** También valida que cada habilidad exista en el catálogo (400). */
    private VacanteDetalle detallar(Vacante vacante, Empresa empresa) {
        return VacanteDetalle.de(vacante, empresa, habilidades.buscarPorIds(vacante.idsHabilidades()));
    }
}
