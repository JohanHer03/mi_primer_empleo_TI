package com.primerempleo.infrastructure.adapter.out.persistence;

import com.primerempleo.domain.model.CategoriaHabilidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Tabla habilidades (V1; nombre único sin distinguir mayúsculas desde V4).
 * fecha_creacion no se mapea: la pone la base con su valor por defecto.
 */
@Entity
@Table(name = "habilidades")
public class HabilidadEntity extends EntidadConIdAsignado {

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaHabilidad categoria;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public CategoriaHabilidad getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaHabilidad categoria) {
        this.categoria = categoria;
    }
}
