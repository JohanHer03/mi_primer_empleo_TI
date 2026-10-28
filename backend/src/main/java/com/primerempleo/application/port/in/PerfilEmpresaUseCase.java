package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EmpresaConCuenta;
import java.util.UUID;

/**
 * HU-06: la empresa consulta y completa su perfil (docs/contrato-api.md, 4.2).
 * Siempre trabaja sobre su propia empresa: el id de usuario sale del token.
 */
public interface PerfilEmpresaUseCase {

    EmpresaConCuenta consultar(UUID usuarioId);

    EmpresaConCuenta actualizar(ActualizarPerfilEmpresaComando comando);

    record ActualizarPerfilEmpresaComando(
            UUID usuarioId,
            String razonSocial,
            String sector,
            String ciudad,
            String sitioWeb) {
    }
}
