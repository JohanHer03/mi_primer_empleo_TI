package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Traduce entre entidades JPA y modelos de dominio (MapStruct genera la implementación).
 * Los modelos son records: MapStruct los crea con su constructor, que vuelve a validar las reglas.
 */
@Mapper
public interface PersistenciaMapper {

    UsuarioEntity aEntidad(Usuario usuario);

    Usuario aDominio(UsuarioEntity entidad);

    CandidatoEntity aEntidad(Candidato candidato);

    Candidato aDominio(CandidatoEntity entidad);

    EmpresaEntity aEntidad(Empresa empresa);

    Empresa aDominio(EmpresaEntity entidad);

    /** Copia los datos editables sobre una entidad ya cargada; el id y el usuario no cambian. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    void copiar(Empresa empresa, @MappingTarget EmpresaEntity entidad);

    HabilidadEntity aEntidad(Habilidad habilidad);

    Habilidad aDominio(HabilidadEntity entidad);
}
