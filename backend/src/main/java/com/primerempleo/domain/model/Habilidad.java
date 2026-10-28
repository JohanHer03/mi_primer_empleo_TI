package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Elemento del catálogo que usan candidatos y vacantes. El nombre es único sin distinguir mayúsculas
 * (índice uq_habilidades_nombre de V4).
 */
public record Habilidad(UUID id, String nombre, CategoriaHabilidad categoria) {

    private static final Collator ESPANOL = Collator.getInstance(Locale.forLanguageTag("es-CO"));

    /** Orden del catálogo: por categoría (orden del enum) y luego por nombre en español. */
    public static final Comparator<Habilidad> ORDEN_CATALOGO = Comparator
            .comparing(Habilidad::categoria)
            .thenComparing(Habilidad::nombre, ESPANOL);

    public Habilidad {
        Objects.requireNonNull(id, "id");
        nombre = Textos.obligatorio(nombre, "nombre");
        if (categoria == null) {
            throw new DatosInvalidosException("categoria: es obligatoria");
        }
    }

    public static Habilidad nueva(String nombre, CategoriaHabilidad categoria) {
        return new Habilidad(UUID.randomUUID(), nombre, categoria);
    }
}
