package com.primerempleo.infrastructure.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicación. Los casos de uso lo reciben inyectado para que las pruebas fijen la fecha;
 * las reglas que dependen del día (fechas "vigentes") convierten a hora de Colombia en el dominio.
 */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemUTC();
    }
}
