package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.application.port.out.EmpresaRepositoryPort;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.SolicitudPagina;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class EmpresaPersistenceAdapter implements EmpresaRepositoryPort {

    private final EmpresaJpaRepository repositorio;
    private final PersistenciaMapper mapper;

    public EmpresaPersistenceAdapter(EmpresaJpaRepository repositorio, PersistenciaMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public boolean existePorNit(String nit) {
        return repositorio.existsByNit(nit);
    }

    @Override
    public Empresa crear(Empresa empresa) {
        return mapper.aDominio(repositorio.save(mapper.aEntidad(empresa)));
    }

    @Override
    public Empresa actualizar(Empresa empresa) {
        EmpresaEntity entidad = repositorio.findById(empresa.id())
                .orElseThrow(RecursoNoEncontradoException::empresa);
        mapper.copiar(empresa, entidad);
        return mapper.aDominio(repositorio.save(entidad));
    }

    @Override
    public Optional<EmpresaConCuenta> buscarConCuentaPorId(UUID empresaId) {
        return repositorio.buscarConCorreoPorId(empresaId).map(this::aEmpresaConCuenta);
    }

    @Override
    public Optional<EmpresaConCuenta> buscarConCuentaPorUsuarioId(UUID usuarioId) {
        return repositorio.buscarConCorreoPorUsuarioId(usuarioId).map(this::aEmpresaConCuenta);
    }

    @Override
    public Pagina<EmpresaConCuenta> listar(Boolean verificada, SolicitudPagina solicitud) {
        PageRequest pedido = PageRequest.of(solicitud.pagina(), solicitud.tamano());
        Page<EmpresaConCorreoFila> filas = verificada == null
                ? repositorio.listarConCorreo(pedido)
                : repositorio.listarConCorreoPorVerificada(verificada, pedido);
        return new Pagina<>(filas.getContent().stream().map(this::aEmpresaConCuenta).toList(),
                filas.getNumber(), filas.getSize(), filas.getTotalElements(), filas.getTotalPages());
    }

    private EmpresaConCuenta aEmpresaConCuenta(EmpresaConCorreoFila fila) {
        return new EmpresaConCuenta(mapper.aDominio(fila.empresa()), fila.email(), fila.empresa().getFechaCreacion());
    }
}
