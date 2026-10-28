package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;

/**
 * Página pedida en un listado (docs/contrato-api.md, sección 1): `page` desde 0 y `size` de 1 a 50.
 */
public record SolicitudPagina(int pagina, int tamano) {

    public static final int TAMANO_POR_DEFECTO = 10;
    public static final int TAMANO_MAXIMO = 50;

    public SolicitudPagina {
        if (pagina < 0) {
            throw new DatosInvalidosException("page: debe ser 0 o mayor");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO) {
            throw new DatosInvalidosException("size: debe estar entre 1 y " + TAMANO_MAXIMO);
        }
    }
}
