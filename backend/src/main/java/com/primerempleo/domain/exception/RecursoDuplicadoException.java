package com.primerempleo.domain.exception;

/**
 * El recurso ya existe: correo o NIT repetido, postulación duplicada (responde 409).
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }

    /** Correo ya registrado, sin distinguir mayúsculas (docs/contrato-api.md, 2.3 y 2.4). */
    public static RecursoDuplicadoException correoRepetido() {
        return new RecursoDuplicadoException("Ya existe una cuenta con ese correo");
    }

    /** NIT ya registrado por otra empresa (docs/contrato-api.md, 2.4). */
    public static RecursoDuplicadoException nitRepetido() {
        return new RecursoDuplicadoException("Ya existe una empresa registrada con ese NIT");
    }

    /** Habilidad ya en el catálogo, sin distinguir mayúsculas (docs/contrato-api.md, 4.1). */
    public static RecursoDuplicadoException habilidadRepetida() {
        return new RecursoDuplicadoException("Ya existe una habilidad con ese nombre");
    }
}
