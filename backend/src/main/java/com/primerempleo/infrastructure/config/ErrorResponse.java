package com.primerempleo.infrastructure.config;

import java.time.Instant;
import org.springframework.http.HttpStatus;

/**
 * Formato uniforme de error de toda la API: { "status", "error", "mensaje", "timestamp" }.
 */
public record ErrorResponse(int status, String error, String mensaje, Instant timestamp) {

    public static ErrorResponse de(HttpStatus estado, String mensaje) {
        return new ErrorResponse(estado.value(), estado.getReasonPhrase(), mensaje, Instant.now());
    }
}
