package com.primerempleo.infrastructure.security;

import com.primerempleo.application.port.out.CifradorContrasenaPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Adaptador del puerto de cifrado con el PasswordEncoder (BCrypt) de SecurityConfig. */
@Component
public class CifradorContrasenaBcrypt implements CifradorContrasenaPort {

    private final PasswordEncoder passwordEncoder;

    public CifradorContrasenaBcrypt(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String cifrar(String contrasena) {
        return passwordEncoder.encode(contrasena);
    }

    @Override
    public boolean coincide(String contrasena, String hash) {
        return passwordEncoder.matches(contrasena, hash);
    }
}
