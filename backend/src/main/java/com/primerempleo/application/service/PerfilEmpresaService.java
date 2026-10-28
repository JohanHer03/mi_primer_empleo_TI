package com.primerempleo.application.service;

import com.primerempleo.application.port.in.PerfilEmpresaUseCase;
import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Perfil de la empresa autenticada (HU-06). El NIT y el correo no se editan.
 */
@Service
public class PerfilEmpresaService implements PerfilEmpresaUseCase {

    private final EmpresaRepositoryPort empresas;

    public PerfilEmpresaService(EmpresaRepositoryPort empresas) {
        this.empresas = empresas;
    }

    @Override
    @Transactional(readOnly = true)
    public EmpresaConCuenta consultar(UUID usuarioId) {
        return buscar(usuarioId);
    }

    @Override
    @Transactional
    public EmpresaConCuenta actualizar(ActualizarPerfilEmpresaComando comando) {
        EmpresaConCuenta actual = buscar(comando.usuarioId());
        Empresa editada = actual.empresa().conPerfil(
                comando.razonSocial(), comando.sector(), comando.ciudad(), comando.sitioWeb());
        return actual.conEmpresa(empresas.actualizar(editada));
    }

    private EmpresaConCuenta buscar(UUID usuarioId) {
        return empresas.buscarConCuentaPorUsuarioId(usuarioId)
                .orElseThrow(RecursoNoEncontradoException::empresa);
    }
}
