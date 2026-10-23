package com.primerempleo.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.primerempleo.infrastructure.config.ErrorResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Seguridad sin estado. Públicas solo /api/auth/** y la documentación de la API.
 * El filtro JWT (JwtAuthFilter) se agrega en la semana 2 con la autenticación.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final ObjectMapper objectMapper;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // La API no usa cookies de sesión: el token viaja en el encabezado Authorization
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sesion -> sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rutas -> rutas
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/auth/**", "/api/docs/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errores -> errores
                        .authenticationEntryPoint((peticion, respuesta, ex) ->
                                escribirError(respuesta, HttpStatus.UNAUTHORIZED, "Se requiere autenticación"))
                        .accessDeniedHandler((peticion, respuesta, ex) ->
                                escribirError(respuesta, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción")));
        return http.build();
    }

    private void escribirError(HttpServletResponse respuesta, HttpStatus estado, String mensaje) throws IOException {
        respuesta.setStatus(estado.value());
        respuesta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        respuesta.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(respuesta.getOutputStream(), ErrorResponse.de(estado, mensaje));
    }
}
