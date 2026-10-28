package com.primerempleo.application.port.out;

/**
 * Cifrado de contraseñas. Lo implementa infrastructure.security con BCrypt;
 * así los casos de uso no dependen de Spring Security.
 */
public interface CifradorContrasenaPort {

    String cifrar(String contrasena);

    boolean coincide(String contrasena, String hash);

    /**
     * Hace una comparación con el mismo costo que {@link #coincide}, sin resultado.
     * El login la usa cuando el correo no existe, para no revelarlo por el tiempo de respuesta.
     */
    void simularComparacion(String contrasena);
}
