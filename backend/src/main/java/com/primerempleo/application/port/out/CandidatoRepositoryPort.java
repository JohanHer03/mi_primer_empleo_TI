package com.primerempleo.application.port.out;

import com.primerempleo.domain.model.Candidato;

public interface CandidatoRepositoryPort {

    Candidato guardar(Candidato candidato);
}
