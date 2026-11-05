package com.primerempleo.domain.model;

/**
 * Nivel de una habilidad. El valor numérico es el que usa la compatibilidad (CLAUDE.md):
 * BASICO=1, INTERMEDIO=2, AVANZADO=3.
 */
public enum NivelHabilidad {
    BASICO(1),
    INTERMEDIO(2),
    AVANZADO(3);

    private final int valor;

    NivelHabilidad(int valor) {
        this.valor = valor;
    }

    public int valor() {
        return valor;
    }
}
