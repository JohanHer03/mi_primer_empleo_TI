package com.primerempleo.domain.exception;

/**
 * El usuario tiene el rol correcto, pero una regla de negocio no le permite la operación (responde 403).
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }

    /** Regla 3: solo las empresas verificadas publican vacantes. */
    public static AccesoDenegadoException empresaSinVerificar() {
        return new AccesoDenegadoException("Tu empresa debe estar verificada para publicar vacantes");
    }

    /** Regla 4: una empresa solo ve y modifica sus propias vacantes. */
    public static AccesoDenegadoException vacanteAjena() {
        return new AccesoDenegadoException("La vacante pertenece a otra empresa");
    }
}
