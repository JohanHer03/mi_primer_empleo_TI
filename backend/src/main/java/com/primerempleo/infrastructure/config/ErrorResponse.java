package com.primerempleo.infrastructure.config;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Formato uniforme de error de toda la API: { "status", "error", "mensaje", "timestamp" }.
 */
public record ErrorResponse(int status, String error, String mensaje, Instant timestamp) {

    public static ErrorResponse de(HttpStatusCode estado, String mensaje) {
        HttpStatus conocido = HttpStatus.resolve(estado.value());
        String error = conocido != null ? conocido.getReasonPhrase() : "Error";
        return new ErrorResponse(estado.value(), error, mensaje, Instant.now());
    }

    public static ErrorResponse de(HttpStatusCode estado) {
        return de(estado, mensajePorDefecto(estado));
    }

    /** Mensaje en español para los errores que no traen uno propio. */
    public static String mensajePorDefecto(HttpStatusCode estado) {
        return switch (estado.value()) {
            case 400 -> "La petición no es válida";
            case 401 -> "Se requiere autenticación";
            case 403 -> "No tienes permiso para realizar esta acción";
            case 404 -> "La ruta solicitada no existe";
            case 405 -> "Método no permitido en esta ruta";
            case 406 -> "Formato de respuesta no soportado";
            case 409 -> "La petición entra en conflicto con el estado actual";
            case 413 -> "La petición es demasiado grande";
            case 415 -> "Tipo de contenido no soportado; usa application/json";
            default -> estado.is5xxServerError()
                    ? "Ocurrió un error inesperado"
                    : "No se pudo procesar la petición";
        };
    }
}
