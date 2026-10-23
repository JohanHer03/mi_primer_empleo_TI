package com.primerempleo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Sin usuario en memoria con contraseña generada: la autenticación es propia con JWT
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class PrimerEmpleoApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrimerEmpleoApplication.class, args);
    }
}
