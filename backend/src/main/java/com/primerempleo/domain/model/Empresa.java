package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Empresa que publica vacantes. Solo puede publicar cuando un administrador la verifica (regla 3).
 * El NIT se guarda solo con dígitos, incluido el de verificación (restricción ck_empresas_nit_digitos de V3).
 */
public record Empresa(
        UUID id,
        UUID usuarioId,
        String razonSocial,
        String nit,
        String sector,
        String ciudad,
        String sitioWeb,
        boolean verificada) {

    private static final Pattern NIT_VALIDO = Pattern.compile("^[0-9]{6,15}$");
    private static final Pattern SEPARADORES_NIT = Pattern.compile("[.\\-\\s]");

    public Empresa {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(usuarioId, "usuarioId");
        razonSocial = Textos.obligatorio(razonSocial, "razonSocial");
        nit = normalizarNit(nit);
        sector = Textos.opcional(sector);
        ciudad = Textos.opcional(ciudad);
        sitioWeb = Textos.opcional(sitioWeb);
    }

    /** Empresa recién registrada: queda sin verificar hasta que la apruebe un administrador. */
    public static Empresa nueva(UUID usuarioId, String razonSocial, String nit, String sector,
                                String ciudad, String sitioWeb) {
        return new Empresa(UUID.randomUUID(), usuarioId, razonSocial, nit, sector, ciudad, sitioWeb, false);
    }

    /** Quita puntos, guiones y espacios: 900.123.456-7 y 9001234567 son el mismo NIT. */
    public static String normalizarNit(String nit) {
        String digitos = SEPARADORES_NIT.matcher(nit == null ? "" : nit).replaceAll("");
        if (!NIT_VALIDO.matcher(digitos).matches()) {
            throw new DatosInvalidosException("nit: debe tener entre 6 y 15 dígitos, incluido el de verificación");
        }
        return digitos;
    }
}
