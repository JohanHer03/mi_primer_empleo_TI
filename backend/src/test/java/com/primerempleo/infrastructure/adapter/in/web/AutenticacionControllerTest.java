package com.primerempleo.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.IniciarSesionUseCase;
import com.primerempleo.application.port.in.RegistrarCandidatoUseCase;
import com.primerempleo.application.port.in.RegistrarCandidatoUseCase.RegistrarCandidatoComando;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase;
import com.primerempleo.application.port.in.RegistrarEmpresaUseCase.RegistrarEmpresaComando;
import com.primerempleo.domain.exception.CredencialesInvalidasException;
import com.primerempleo.domain.exception.CuentaDesactivadaException;
import com.primerempleo.domain.exception.DatosInvalidosException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AutenticacionController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, AutenticacionWebMapperImpl.class})
@MockitoBean(types = {RegistrarCandidatoUseCase.class, RegistrarEmpresaUseCase.class, IniciarSesionUseCase.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class AutenticacionControllerTest {

    private static final String CANDIDATO_VALIDO = """
            {
              "email": "laura.gomez@udea.edu.co",
              "password": "Medellin2026",
              "nombres": "Laura",
              "apellidos": "Gómez Restrepo",
              "estadoAcademico": "ESTUDIANTE",
              "semestre": 9,
              "universidad": "Universidad de Antioquia",
              "programa": "Ingeniería de Sistemas",
              "ciudad": "Medellín"
            }
            """;

    private static final String EMPRESA_VALIDA = """
            {
              "email": "talento@softwaremedellin.co",
              "password": "Medellin2026",
              "razonSocial": "Software Medellín S.A.S.",
              "nit": "900.123.456-7",
              "sector": "Desarrollo de software",
              "ciudad": "Medellín",
              "sitioWeb": "https://softwaremedellin.co"
            }
            """;

    private static final String LOGIN_VALIDO = """
            { "email": "laura.gomez@udea.edu.co", "password": "Medellin2026" }
            """;

    private final MockMvc mockMvc;
    private final RegistrarCandidatoUseCase registrarCandidato;
    private final RegistrarEmpresaUseCase registrarEmpresa;
    private final IniciarSesionUseCase iniciarSesion;

    @Autowired
    AutenticacionControllerTest(MockMvc mockMvc, RegistrarCandidatoUseCase registrarCandidato,
                                RegistrarEmpresaUseCase registrarEmpresa, IniciarSesionUseCase iniciarSesion) {
        this.mockMvc = mockMvc;
        this.registrarCandidato = registrarCandidato;
        this.registrarEmpresa = registrarEmpresa;
        this.iniciarSesion = iniciarSesion;
    }

    @Test
    void deberiaRegistrarAlCandidatoSinTokenYResponder201() throws Exception {
        when(registrarCandidato.registrar(any(RegistrarCandidatoComando.class))).thenReturn("token-jwt");

        mockMvc.perform(post("/api/auth/registro/candidato").contentType(MediaType.APPLICATION_JSON).content(CANDIDATO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    void deberiaResponder400ConElCampoSiLaPeticionNoEsValida() throws Exception {
        String sinCorreoYContrasenaCorta = CANDIDATO_VALIDO
                .replace("\"laura.gomez@udea.edu.co\"", "\"no-es-correo\"")
                .replace("\"Medellin2026\"", "\"corta\"");

        mockMvc.perform(post("/api/auth/registro/candidato").contentType(MediaType.APPLICATION_JSON)
                        .content(sinCorreoYContrasenaCorta))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("email: escribe un correo válido")))
                .andExpect(jsonPath("$.mensaje").value(containsString("password: debe tener entre 8 y 72 caracteres")));
        verify(registrarCandidato, never()).registrar(any(RegistrarCandidatoComando.class));
    }

    @Test
    void deberiaResponder400ConElMensajeDelDominio() throws Exception {
        when(registrarCandidato.registrar(any(RegistrarCandidatoComando.class)))
                .thenThrow(new DatosInvalidosException("semestre: es obligatorio para estudiantes"));

        mockMvc.perform(post("/api/auth/registro/candidato").contentType(MediaType.APPLICATION_JSON).content(CANDIDATO_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("semestre: es obligatorio para estudiantes"));
    }

    @Test
    void deberiaResponder409SiElCorreoYaExiste() throws Exception {
        when(registrarCandidato.registrar(any(RegistrarCandidatoComando.class)))
                .thenThrow(new RecursoDuplicadoException("Ya existe una cuenta con ese correo"));

        mockMvc.perform(post("/api/auth/registro/candidato").contentType(MediaType.APPLICATION_JSON).content(CANDIDATO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje").value("Ya existe una cuenta con ese correo"));
    }

    @Test
    void deberiaRegistrarLaEmpresaYResponder201() throws Exception {
        when(registrarEmpresa.registrar(any(RegistrarEmpresaComando.class))).thenReturn("token-jwt");

        mockMvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON).content(EMPRESA_VALIDA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    void deberiaRechazarUnSitioWebSinHttp() throws Exception {
        String sitioInvalido = EMPRESA_VALIDA.replace("https://softwaremedellin.co", "softwaremedellin.co");

        mockMvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON).content(sitioInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(startsWith("sitioWeb:")));
    }

    @Test
    void deberiaResponder409SiLaBaseRechazaUnNitRepetido() throws Exception {
        when(registrarEmpresa.registrar(any(RegistrarEmpresaComando.class))).thenThrow(new DataIntegrityViolationException(
                "could not execute statement",
                new SQLException("duplicate key value violates unique constraint \"uq_empresas_nit\"")));

        mockMvc.perform(post("/api/auth/registro/empresa").contentType(MediaType.APPLICATION_JSON).content(EMPRESA_VALIDA))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una empresa registrada con ese NIT"));
    }

    @Test
    void deberiaIniciarSesionYResponder200() throws Exception {
        when(iniciarSesion.iniciarSesion(any())).thenReturn("token-jwt");

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    void deberiaResponder401ConCredencialesIncorrectas() throws Exception {
        when(iniciarSesion.iniciarSesion(any())).thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Correo o contraseña incorrectos"));
    }

    @Test
    void deberiaResponder403SiLaCuentaEstaDesactivada() throws Exception {
        when(iniciarSesion.iniciarSesion(any())).thenThrow(new CuentaDesactivadaException());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Tu cuenta está desactivada"));
    }

    @Test
    void deberiaResponder400SiElLoginNoTraeContrasena() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"email\": \"laura@udea.edu.co\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("password: es obligatoria"));
    }
}
