package com.primerempleo.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.domain.model.Rol;
import com.primerempleo.infrastructure.config.CorsConfig;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, SecurityConfigTest.ControladorPrueba.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class SecurityConfigTest {

    private static final UUID ID = UUID.fromString("7f3c2a1e-5b4d-4c6e-9f8a-1b2c3d4e5f60");

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    SecurityConfigTest(MockMvc mockMvc, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Rutas solo para estas pruebas: una para cualquier usuario autenticado y otra solo para ADMIN. */
    @RestController
    static class ControladorPrueba {

        @GetMapping("/api/prueba/yo")
        Map<String, String> yo(@AuthenticationPrincipal UsuarioAutenticado usuario) {
            return Map.of("id", usuario.id().toString(), "email", usuario.email(), "rol", usuario.rol().name());
        }

        @GetMapping("/api/prueba/admin")
        @PreAuthorize("hasRole('ADMIN')")
        Map<String, String> soloAdmin() {
            return Map.of("ok", "si");
        }
    }

    @Test
    void deberiaAutenticarConUnTokenValidoYExponerElUsuario() throws Exception {
        String token = jwtService.generarToken(ID, "laura@udea.edu.co", Rol.CANDIDATO);

        mockMvc.perform(get("/api/prueba/yo").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.email").value("laura@udea.edu.co"))
                .andExpect(jsonPath("$.rol").value("CANDIDATO"));
    }

    @Test
    void deberiaResponder401ConUnTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/prueba/yo").header(HttpHeaders.AUTHORIZATION, "Bearer token-alterado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Se requiere autenticación"));
    }

    @Test
    void deberiaIgnorarUnEncabezadoQueNoEsBearer() throws Exception {
        String token = jwtService.generarToken(ID, "laura@udea.edu.co", Rol.CANDIDATO);

        mockMvc.perform(get("/api/prueba/yo").header(HttpHeaders.AUTHORIZATION, "Basic " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberiaResponder403SiElRolNoTienePermiso() throws Exception {
        String token = jwtService.generarToken(ID, "laura@udea.edu.co", Rol.CANDIDATO);

        mockMvc.perform(get("/api/prueba/admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para realizar esta acción"));
    }

    @Test
    void deberiaPermitirLaRutaDeAdminConRolAdmin() throws Exception {
        String token = jwtService.generarToken(ID, "admin@primerempleo.co", Rol.ADMIN);

        mockMvc.perform(get("/api/prueba/admin").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void deberiaCifrarContrasenasConBcrypt() {
        String hash = passwordEncoder.encode("Medellin2026");

        assertThat(hash).startsWith("$2a$10$").hasSize(60);
        assertThat(passwordEncoder.matches("Medellin2026", hash)).isTrue();
        assertThat(passwordEncoder.matches("otraClave", hash)).isFalse();
    }

    @Test
    void deberiaResponder401ConFormatoUniformeSinToken() throws Exception {
        mockMvc.perform(get("/api/vacantes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.mensaje").value("Se requiere autenticación"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deberiaPermitirRutasDeAuthSinToken() throws Exception {
        // Sin controladores aún: llega al manejador de errores (404) en vez de quedarse en el 401
        mockMvc.perform(get("/api/auth/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensaje").value("La ruta solicitada no existe"));
    }

    @Test
    void deberiaAceptarPreflightDelOrigenConfigurado() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void deberiaRechazarPreflightDeOrigenNoPermitido() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "https://otro-sitio.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
