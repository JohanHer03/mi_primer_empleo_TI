package com.primerempleo.infrastructure.adapter.out.persistence;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Transient;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

/**
 * Base de las entidades cuyo UUID lo genera el dominio.
 * Con el id ya asignado, Spring Data creería que la fila existe y haría un SELECT antes de cada INSERT;
 * {@link Persistable#isNew()} le indica que es nueva hasta que se guarda o se carga.
 * Por eso una entidad creada desde el mapper solo sirve para insertar: para actualizar, cargarla con findById.
 */
@MappedSuperclass
public abstract class EntidadConIdAsignado implements Persistable<UUID> {

    @Id
    private UUID id;

    @Transient
    private boolean nueva = true;

    @Override
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public boolean isNew() {
        return nueva;
    }

    @PostLoad
    @PostPersist
    void marcarGuardada() {
        nueva = false;
    }
}
