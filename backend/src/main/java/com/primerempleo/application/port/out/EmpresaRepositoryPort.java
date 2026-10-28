package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Empresa;

public interface EmpresaRepositoryPort {

    /** El NIT debe venir normalizado (solo dígitos), como se guarda. */
    boolean existePorNit(String nit);

    Empresa guardar(Empresa empresa);
}
