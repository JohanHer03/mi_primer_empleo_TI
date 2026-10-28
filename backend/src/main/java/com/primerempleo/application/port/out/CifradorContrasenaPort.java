package com.primerempleo.application.port.out;

/**
 * Cifrado de contraseñas. Lo implementa infrastructure.security con BCrypt;
 * así los casos de uso no dependen de Spring Security.
 */
public interface CifradorContrasenaPort {

    String cifrar(String contrasena);

    boolean coincide(String contrasena, String hash);
}
