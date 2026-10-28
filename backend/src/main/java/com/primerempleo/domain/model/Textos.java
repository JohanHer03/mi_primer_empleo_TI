package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;

/**
 * Limpieza común de los textos que llegan a los modelos.
 */
final class Textos {

    private Textos() {
    }

    /** Quita espacios a los lados; un texto vacío o en blanco queda como null. */
    static String opcional(String texto) {
        if (texto == null) {
            return null;
        }
        String limpio = texto.strip();
        return limpio.isEmpty() ? null : limpio;
    }

    /** Quita espacios a los lados y exige que quede algo. */
    static String obligatorio(String texto, String campo) {
        String limpio = opcional(texto);
        if (limpio == null) {
            throw new DatosInvalidosException(campo + ": es obligatorio");
        }
        return limpio;
    }
}
