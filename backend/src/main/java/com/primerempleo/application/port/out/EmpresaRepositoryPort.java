package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Empresa;

public interface EmpresaRepositoryPort {

    /** El NIT debe venir normalizado (solo dígitos), como se guarda. */
    boolean existePorNit(String nit);

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     * Para editar, agregar un método que cargue la entidad con findById y le copie los cambios.
     */
    Empresa crear(Empresa empresa);
}
