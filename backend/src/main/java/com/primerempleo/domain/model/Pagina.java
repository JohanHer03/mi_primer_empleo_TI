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

    /** Pagina en memoria una lista ya filtrada y ordenada (la búsqueda ordena por un valor calculado). */
    public static <T> Pagina<T> de(List<T> todos, SolicitudPagina solicitud) {
        int desde = (int) Math.min((long) solicitud.pagina() * solicitud.tamano(), todos.size());
        int hasta = Math.min(desde + solicitud.tamano(), todos.size());
        int totalPaginas = (todos.size() + solicitud.tamano() - 1) / solicitud.tamano();
        return new Pagina<>(todos.subList(desde, hasta), solicitud.pagina(), solicitud.tamano(), todos.size(),
                totalPaginas);
    }

    public <R> Pagina<R> map(Function<? super T, ? extends R> transformacion) {
        return new Pagina<>(contenido.stream().<R>map(transformacion).toList(),
                pagina, tamano, totalElementos, totalPaginas);
    }
}
