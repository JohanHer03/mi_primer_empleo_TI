package com.primerempleo.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.CambiarContrasenaUseCase;
import com.primerempleo.application.port.in.CambiarContrasenaUseCase.CambiarContrasenaComando;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.util.UUID;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class})
@MockitoBean(types = CambiarContrasenaUseCase.class)
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class UsuarioControllerTest {

    private static final String RUTA = "/api/usuarios/me/contrasena";
    private static final UUID USUARIO = UUID.randomUUID();
    private static final String CUERPO = """
            { "contrasenaActual": "Medellin2026", "contrasenaNueva": "Envigado2027" }
            """;

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final CambiarContrasenaUseCase cambiarContrasena;

    @Autowired
    UsuarioControllerTest(MockMvc mockMvc, JwtService jwtService, CambiarContrasenaUseCase cambiarContrasena) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.cambiarContrasena = cambiarContrasena;
    }

    @ParameterizedTest
    @EnumSource(Rol.class)
    void deberiaCambiarLaContrasenaDelUsuarioDelTokenEnCualquierRol(Rol rol) throws Exception {
        mockMvc.perform(put(RUTA).header(HttpHeaders.AUTHORIZATION, token(rol))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isNoContent());
        verify(cambiarContrasena).cambiarContrasena(
                new CambiarContrasenaComando(USUARIO, "Medellin2026", "Envigado2027"));
    }

    @Test
    void deberiaResponder401SinToken() throws Exception {
        mockMvc.perform(put(RUTA).contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        verify(cambiarContrasena, never()).cambiarContrasena(any());
    }

    @Test
    void deberiaResponder400SiFaltaLaContrasenaNueva() throws Exception {
        mockMvc.perform(put(RUTA).header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"contrasenaActual\": \"Medellin2026\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("contrasenaNueva: es obligatoria"));
        verify(cambiarContrasena, never()).cambiarContrasena(any());
    }

    @Test
    void deberiaResponder400SiLaNuevaEsCorta() throws Exception {
        mockMvc.perform(put(RUTA).header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"contrasenaActual\": \"Medellin2026\", \"contrasenaNueva\": \"corta\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("contrasenaNueva: debe tener entre 8 y 72 caracteres"));
    }

    @Test
    void deberiaResponder400YNo401SiLaActualNoEsCorrecta() throws Exception {
        doThrow(new DatosInvalidosException("contrasenaActual: no es correcta"))
                .when(cambiarContrasena).cambiarContrasena(any());

        mockMvc.perform(put(RUTA).header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.mensaje").value("contrasenaActual: no es correcta"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deberiaResponder403SiLaCuentaEstaDesactivada() throws Exception {
        doThrow(new CuentaDesactivadaException()).when(cambiarContrasena).cambiarContrasena(any());

        mockMvc.perform(put(RUTA).header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Tu cuenta está desactivada"));
    }

    private String token(Rol rol) {
        return "Bearer " + jwtService.generarToken(USUARIO, rol.name().toLowerCase() + "@example.com", rol);
    }
}
