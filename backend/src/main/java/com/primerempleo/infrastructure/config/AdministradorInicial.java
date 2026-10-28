package com.primerempleo.infrastructure.config;

import com.primerempleo.application.port.in.CrearAdministradorUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Al arrancar, crea la cuenta ADMIN con ADMIN_EMAIL y ADMIN_PASSWORD si todavía no existe.
 * El hash se calcula aquí, así ningún secreto queda en las migraciones del repositorio.
 * Sin esas variables no hace nada. Un error no detiene la API: solo queda en el log.
 */
@Component
public class AdministradorInicial implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdministradorInicial.class);

    private final CrearAdministradorUseCase crearAdministrador;
    private final String email;
    private final String password;

    public AdministradorInicial(CrearAdministradorUseCase crearAdministrador,
                                @Value("${app.admin.email:}") String email,
                                @Value("${app.admin.password:}") String password) {
        this.crearAdministrador = crearAdministrador;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        if (email.isBlank() || password.isBlank()) {
            log.info("ADMIN_EMAIL o ADMIN_PASSWORD sin definir: no se crea el administrador inicial");
            return;
        }
        try {
            if (crearAdministrador.crearSiNoExiste(email, password)) {
                log.info("Administrador inicial creado");
            }
        } catch (RuntimeException e) {
            log.error("No se pudo crear el administrador inicial: {}", e.getMessage());
        }
    }
}
