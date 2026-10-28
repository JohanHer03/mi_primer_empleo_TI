package com.primerempleo.application.service;

import com.primerempleo.application.port.in.VerificacionEmpresaUseCase;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Verificación de empresas por el administrador (HU-10). Solo las verificadas publican vacantes (regla 3).
 */
@Service
public class VerificacionEmpresaService implements VerificacionEmpresaUseCase {

    private final EmpresaRepositoryPort empresas;

    public VerificacionEmpresaService(EmpresaRepositoryPort empresas) {
        this.empresas = empresas;
    }

    @Override
    @Transactional(readOnly = true)
    public Pagina<EmpresaConCuenta> listar(Boolean verificada, SolicitudPagina solicitud) {
        return empresas.listar(verificada, solicitud);
    }

    @Override
    @Transactional
    public EmpresaConCuenta verificar(UUID empresaId) {
        EmpresaConCuenta actual = empresas.buscarConCuentaPorId(empresaId)
                .orElseThrow(RecursoNoEncontradoException::empresa);
        if (actual.empresa().verificada()) {
            return actual;
        }
        return actual.conEmpresa(empresas.actualizar(actual.empresa().verificar()));
    }
}
