package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Candidato;

public interface CandidatoRepositoryPort {

    /**
     * Inserta un registro nuevo. No sirve para actualizar: la entidad se marca como nueva y daría clave duplicada.
     * Para editar, agregar un método que cargue la entidad con findById y le copie los cambios.
     */
    Candidato crear(Candidato candidato);
}
