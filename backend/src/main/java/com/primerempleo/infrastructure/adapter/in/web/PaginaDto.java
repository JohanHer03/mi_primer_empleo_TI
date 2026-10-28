package com.primerempleo.infrastructure.adapter.in.web;

import com.primerempleo.domain.model.Pagina;
import java.util.List;
import java.util.function.Function;

/**
 * Formato de los listados paginados (docs/contrato-api.md, sección 1). Propio, para no exponer la Page de Spring.
 */
public record PaginaDto<T>(List<T> contenido, int pagina, int tamano, long totalElementos, int totalPaginas) {

    public static <D, T> PaginaDto<T> de(Pagina<D> pagina, Function<? super D, ? extends T> aDto) {
        Pagina<T> convertida = pagina.map(aDto);
        return new PaginaDto<>(convertida.contenido(), convertida.pagina(), convertida.tamano(),
                convertida.totalElementos(), convertida.totalPaginas());
    }
}
