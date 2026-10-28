package com.primerempleo.infrastructure.adapter.in.web;

import java.util.UUID;

/** Perfil de la empresa autenticada (docs/contrato-api.md, 4.2). */
public record EmpresaPerfilDto(
        UUID id,
        String razonSocial,
        String nit,
        String email,
        String sector,
        String ciudad,
        String sitioWeb,
        boolean verificada) {
}
