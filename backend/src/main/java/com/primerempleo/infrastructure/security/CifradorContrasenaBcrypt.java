package com.primerempleo.infrastructure.security;

import com.primerempleo.application.port.out.CifradorContrasenaPort;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador del puerto de cifrado con el PasswordEncoder (BCrypt) de SecurityConfig. */
@Component
public class CifradorContrasenaBcrypt implements CifradorContrasenaPort {

    private final PasswordEncoder passwordEncoder;
    /** Hash de una contraseña que nadie usa, para simularComparacion. */
    private final String hashFicticio;

    public CifradorContrasenaBcrypt(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.hashFicticio = passwordEncoder.encode(UUID.randomUUID().toString());
    }

    @Override
    public String cifrar(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    @Override
    public boolean coincide(String contrasena, String hash) {
        return passwordEncoder.matches(contrasena, hash);
    }

    @Override
    public void simularComparacion(String contrasena) {
        passwordEncoder.matches(contrasena, hashFicticio);
    }
}
