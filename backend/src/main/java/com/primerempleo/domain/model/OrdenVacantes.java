package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Orden de la búsqueda de vacantes (docs/contrato-api.md, 5.4). El valor del parámetro {@code orden} es el texto
 * en camelCase: {@code compatibilidad} (por defecto) o {@code fechaPublicacion}.
 */
public enum OrdenVacantes {

    /** Mayor compatibilidad primero; con empate, la más reciente. */
    COMPATIBILIDAD("compatibilidad", Comparator
            .comparing(VacanteResumen::compatibilidad, Comparator.reverseOrder())
            .thenComparing(OrdenVacantes::masRecientePrimero)),

    /** La más reciente primero. */
    FECHA_PUBLICACION("fechaPublicacion", OrdenVacantes::masRecientePrimero);

    private final String parametro;
    private final Comparator<VacanteResumen> comparador;

    OrdenVacantes(String parametro, Comparator<VacanteResumen> comparador) {
        this.parametro = parametro;
        this.comparador = comparador;
    }

    public Comparator<VacanteResumen> comparador() {
        return comparador;
    }

    public static OrdenVacantes desde(String parametro) {
        return Arrays.stream(values())
                .filter(orden -> orden.parametro.equals(parametro))
                .findFirst()
                .orElseThrow(() -> new DatosInvalidosException("orden: debe ser compatibilidad o fechaPublicacion"));
    }

    /** Fecha de publicación descendente y, con empate, por id para que la paginación sea estable. */
    private static int masRecientePrimero(VacanteResumen a, VacanteResumen b) {
        int porFecha = b.vacante().fechaPublicacion().compareTo(a.vacante().fechaPublicacion());
        return porFecha != 0 ? porFecha : a.vacante().id().compareTo(b.vacante().id());
    }
}
