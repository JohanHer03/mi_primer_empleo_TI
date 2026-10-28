package com.primerempleo.infrastructure.config;

import com.primerempleo.domain.exception.CredencialesInvalidasException;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Traduce las excepciones a respuestas con el formato {@link ErrorResponse}.
 * Hereda de ResponseEntityExceptionHandler para que todas las excepciones estándar de Spring MVC
 * (415, 405, 406, parámetros faltantes, ResponseStatusException, ...) conserven su código HTTP;
 * aquí solo se cambia el cuerpo y algunos mensajes.
 * Las excepciones de negocio de domain.exception se traducen aquí a su código HTTP.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> manejarAccesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, ErrorResponse.mensajePorDefecto(HttpStatus.FORBIDDEN));
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<Object> manejarDatosInvalidos(DatosInvalidosException ex) {
        return responder(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Object> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return responder(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(CuentaDesactivadaException.class)
    public ResponseEntity<Object> manejarCuentaDesactivada(CuentaDesactivadaException ex) {
        return responder(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<Object> manejarRecursoDuplicado(RecursoDuplicadoException ex) {
        return responder(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Respaldo de las validaciones del caso de uso: si dos peticiones registran el mismo correo o NIT
     * a la vez, ambas pasan la comprobación y la base rechaza la segunda con su restricción.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> manejarIntegridad(DataIntegrityViolationException ex) {
        String detalle = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detalle.contains("uq_usuarios_email")) {
            return responder(HttpStatus.CONFLICT, RecursoDuplicadoException.correoRepetido().getMessage());
        }
        if (detalle.contains("uq_empresas_nit")) {
            return responder(HttpStatus.CONFLICT, RecursoDuplicadoException.nitRepetido().getMessage());
        }
        if (detalle.contains("uq_habilidades_nombre")) {
            return responder(HttpStatus.CONFLICT, RecursoDuplicadoException.habilidadRepetida().getMessage());
        }
        log.warn("Restricción de la base de datos violada: {}", detalle);
        return responder(HttpStatus.CONFLICT, ErrorResponse.mensajePorDefecto(HttpStatus.CONFLICT));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> manejarInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, ErrorResponse.mensajePorDefecto(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(ex, mensaje, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, "El cuerpo de la petición no es válido", headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
            TypeMismatchException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, "Valor inválido para el parámetro " + ex.getPropertyName(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, "Falta el parámetro obligatorio " + ex.getParameterName(), headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return handleExceptionInternal(ex, "Método " + ex.getMethod() + " no permitido en esta ruta", headers, status, request);
    }

    /** Punto común de ResponseEntityExceptionHandler: aquí se arma siempre el ErrorResponse. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensaje;
        if (body instanceof String texto) {
            mensaje = texto;
        } else if (ex instanceof ResponseStatusException conEstado && conEstado.getReason() != null) {
            mensaje = conEstado.getReason();
        } else {
            mensaje = ErrorResponse.mensajePorDefecto(status);
        }
        if (status.is5xxServerError()) {
            log.error("Error del servidor", ex);
        }
        return ResponseEntity.status(status).headers(headers).body(ErrorResponse.de(status, mensaje));
    }

    private ResponseEntity<Object> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(ErrorResponse.de(estado, mensaje));
    }
}
