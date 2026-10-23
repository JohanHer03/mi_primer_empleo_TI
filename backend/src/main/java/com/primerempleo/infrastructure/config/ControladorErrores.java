package com.primerempleo.infrastructure.config;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reemplaza el BasicErrorController de Spring Boot para que los errores que ocurren fuera de los
 * controladores (por ejemplo, en un filtro como JwtAuthFilter) también salgan con el formato
 * {@link ErrorResponse}. Es infraestructura: no llama casos de uso.
 */
@RestController
public class ControladorErrores implements ErrorController {

    @RequestMapping("${server.error.path:/error}")
    public ResponseEntity<ErrorResponse> manejarError(HttpServletRequest peticion) {
        Object codigo = peticion.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        HttpStatusCode estado = codigo instanceof Integer valor
                ? HttpStatusCode.valueOf(valor)
                : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado));
    }
}
