package com.primerempleo.application.service;

import com.primerempleo.application.port.in.CrearHabilidadUseCase;
import com.primerempleo.application.port.in.ListarHabilidadesUseCase;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de habilidades (docs/contrato-api.md, 4.1). La lista no se pagina: alimenta selectores
 * y tiene decenas de elementos.
 */
@Service
public class HabilidadService implements ListarHabilidadesUseCase, CrearHabilidadUseCase {

    private final HabilidadRepositoryPort habilidades;

    public HabilidadService(HabilidadRepositoryPort habilidades) {
        this.habilidades = habilidades;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Habilidad> listar(CategoriaHabilidad categoria) {
        return habilidades.listar(categoria).stream()
                .sorted(Habilidad.ORDEN_CATALOGO)
                .toList();
    }

    @Override
    @Transactional
    public Habilidad crear(CrearHabilidadComando comando) {
        Habilidad habilidad = Habilidad.nueva(comando.nombre(), comando.categoria());
        if (habilidades.existePorNombre(habilidad.nombre())) {
            throw RecursoDuplicadoException.habilidadRepetida();
        }
        return habilidades.crear(habilidad);
    }
}
