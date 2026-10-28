package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.Optional;
import java.util.UUID;

public interface EmpresaRepositoryPort {

    /** El NIT debe venir normalizado (solo dígitos), como se guarda. */
    boolean existePorNit(String nit);

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     * Para editar, usar {@link #actualizar(Empresa)}.
     */
    Empresa crear(Empresa empresa);

    /** Guarda los cambios de una empresa que ya existe (la carga y le copia los datos). */
    Empresa actualizar(Empresa empresa);

    Optional<EmpresaConCuenta> buscarConCuentaPorId(UUID empresaId);

    Optional<EmpresaConCuenta> buscarConCuentaPorUsuarioId(UUID usuarioId);

    /** Ordenadas por fecha de registro, las más antiguas primero. {@code verificada} null lista todas. */
    Pagina<EmpresaConCuenta> listar(Boolean verificada, SolicitudPagina solicitud);
}
