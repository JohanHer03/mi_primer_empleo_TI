package com.primerempleo.infrastructure.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.infrastructure.config.CorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * En Vercel el navegador manda Origin con el dominio público y el contenedor recibe la petición desde un proxy.
 * Con server.forward-headers-strategy=framework (application.yml), Spring toma el host de X-Forwarded-Host
 * y la petición cuenta como del mismo origen. ServletWebServerFactoryAutoConfiguration registra ese filtro.
 */
@WebMvcTest
@ImportAutoConfiguration(ServletWebServerFactoryAutoConfiguration.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class ProxyVercelTest {

    private static final String DOMINIO = "mi-primer-empleo-ti.vercel.app";

    private final MockMvc mockMvc;

    @Autowired
    ProxyVercelTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void deberiaAceptarElOrigenPublicoCuandoElProxyEnviaLosEncabezadosForwarded() throws Exception {
        // Sin controlador de login en este slice: llegar al 404 significa que CORS no la bloqueó
        mockMvc.perform(post("/api/auth/no-existe")
                        .header("Origin", "https://" + DOMINIO)
                        .header("X-Forwarded-Host", DOMINIO)
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deberiaSeguirRechazandoUnOrigenAjeno() throws Exception {
        mockMvc.perform(post("/api/auth/no-existe")
                        .header("Origin", "https://otro-sitio.com")
                        .header("X-Forwarded-Host", DOMINIO)
                        .header("X-Forwarded-Proto", "https"))
                .andExpect(status().isForbidden());
    }
}
