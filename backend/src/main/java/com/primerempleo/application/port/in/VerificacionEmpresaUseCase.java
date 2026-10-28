package com.primerempleo.application.port.in;

import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.UUID;

/**
 * HU-10: el administrador revisa las empresas registradas y verifica las legítimas (docs/contrato-api.md, 4.3).
 */
public interface VerificacionEmpresaUseCase {

    /** Las más antiguas primero. {@code verificada} null lista todas. */
    Pagina<EmpresaConCuenta> listar(Boolean verificada, SolicitudPagina solicitud);

    /** Idempotente: verificar una empresa ya verificada no cambia nada. */
    EmpresaConCuenta verificar(UUID empresaId);
}
