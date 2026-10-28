package com.primerempleo.infrastructure.adapter.in.web;

/** Respuesta de login y registro: el JWT de la sesión (docs/contrato-api.md, 2.2). */
public record AuthRespuesta(String token) {
}
