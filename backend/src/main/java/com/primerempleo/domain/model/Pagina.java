package com.primerempleo.domain.model;

import java.util.List;
import java.util.function.Function;

/**
 * Resultado de un listado paginado, sin depender de la Page de Spring Data.
 */
public record Pagina<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public Pagina {
        contenido = List.copyOf(contenido);
    }

    public <R> Pagina<R> map(Function<? super T, ? extends R> transformacion) {
        return new Pagina<>(contenido.stream().<R>map(transformacion).toList(),
                pagina, tamano, totalElementos, totalPaginas);
    }
}
