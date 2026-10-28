package com.primerempleo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lee el encabezado {@code Authorization: Bearer <token>} y, si el token es válido,
 * autentica la petición con el rol como autoridad {@code ROLE_<rol>}.
 * Si falta o no es válido no hace nada: las rutas protegidas responden 401 desde SecurityConfig.
 * No es un @Component para que Spring Boot no lo registre también como filtro del servlet.
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String PREFIJO_BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion, HttpServletResponse respuesta, FilterChain cadena)
            throws ServletException, IOException {
        String encabezado = peticion.getHeader(HttpHeaders.AUTHORIZATION);
        if (encabezado != null && encabezado.startsWith(PREFIJO_BEARER)) {
            jwtService.validarToken(encabezado.substring(PREFIJO_BEARER.length()).trim())
                    .ifPresent(usuario -> autenticar(peticion, usuario));
        }
        cadena.doFilter(peticion, respuesta);
    }

    private void autenticar(HttpServletRequest peticion, UsuarioAutenticado usuario) {
        UsernamePasswordAuthenticationToken autenticacion = UsernamePasswordAuthenticationToken.authenticated(
                usuario, null, List.of(new SimpleGrantedAuthority("ROLE_" + usuario.rol().name())));
        autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(peticion));
        SecurityContextHolder.getContext().setAuthentication(autenticacion);
    }
}
