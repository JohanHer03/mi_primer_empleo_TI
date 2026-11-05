package com.primerempleo.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.PerfilCandidatoUseCase;
import com.primerempleo.application.port.in.PerfilCandidatoUseCase.ActualizarPerfilCandidatoComando;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CandidatoHabilidad;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.PerfilCandidato;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CandidatoController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, CandidatoWebMapperImpl.class})
@MockitoBean(types = PerfilCandidatoUseCase.class)
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class CandidatoControllerTest {

    private static final UUID USUARIO_CANDIDATO = UUID.randomUUID();
    private static final Habilidad JAVA = new Habilidad(UUID.randomUUID(), "Java", CategoriaHabilidad.LENGUAJE);
    private static final String PERFIL_EDITADO = """
            {
              "nombres": "Laura",
              "apellidos": "Gómez Restrepo",
              "estadoAcademico": "ESTUDIANTE",
              "semestre": 10,
              "universidad": "Universidad de Antioquia",
              "programa": "Ingeniería de Sistemas",
              "ciudad": "Medellín",
              "githubUrl": "https://github.com/lauragomez",
              "resumen": "Me interesa el desarrollo backend con Java."
            }
            """;

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final PerfilCandidatoUseCase perfilCandidato;

    @Autowired
    CandidatoControllerTest(MockMvc mockMvc, JwtService jwtService, PerfilCandidatoUseCase perfilCandidato) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.perfilCandidato = perfilCandidato;
    }

    @Test
    void deberiaDevolverElPerfilDelCandidatoDelToken() throws Exception {
        when(perfilCandidato.consultar(USUARIO_CANDIDATO)).thenReturn(perfil());

        mockMvc.perform(get("/api/candidatos/me").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("laura@udea.edu.co"))
                .andExpect(jsonPath("$.nombres").value("Laura"))
                .andExpect(jsonPath("$.semestre").value(9))
                .andExpect(jsonPath("$.usuarioId").doesNotExist())
                .andExpect(jsonPath("$.habilidades[0].nombre").value("Java"))
                .andExpect(jsonPath("$.habilidades[0].categoria").value("LENGUAJE"))
                .andExpect(jsonPath("$.habilidades[0].nivel").value("INTERMEDIO"));
    }

    @Test
    void deberiaResponder403SiUnaEmpresaPideElPerfilDeCandidato() throws Exception {
        mockMvc.perform(get("/api/candidatos/me").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaActualizarElPerfilConElUsuarioDelToken() throws Exception {
        when(perfilCandidato.actualizar(any())).thenReturn(perfil());

        mockMvc.perform(put("/api/candidatos/me").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON).content(PERFIL_EDITADO))
                .andExpect(status().isOk());
        verify(perfilCandidato).actualizar(new ActualizarPerfilCandidatoComando(USUARIO_CANDIDATO, "Laura",
                "Gómez Restrepo", EstadoAcademico.ESTUDIANTE, 10, "Universidad de Antioquia", "Ingeniería de Sistemas",
                "Medellín", "https://github.com/lauragomez", "Me interesa el desarrollo backend con Java."));
    }

    @Test
    void deberiaResponder400SiElGithubNoEsDeGithub() throws Exception {
        mockMvc.perform(put("/api/candidatos/me").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PERFIL_EDITADO.replace("https://github.com/lauragomez", "https://gitlab.com/laura")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("githubUrl: debe empezar por https://github.com/"));
        verify(perfilCandidato, never()).actualizar(any());
    }

    @Test
    void deberiaDefinirLasHabilidadesYDevolverLaLista() throws Exception {
        when(perfilCandidato.definirHabilidades(eq(USUARIO_CANDIDATO), any())).thenReturn(perfil());

        mockMvc.perform(put("/api/candidatos/me/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{ \"habilidadId\": \"" + JAVA.id() + "\", \"nivel\": \"INTERMEDIO\" }]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].habilidadId").value(JAVA.id().toString()))
                .andExpect(jsonPath("$[0].nivel").value("INTERMEDIO"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<CandidatoHabilidad>> enviadas = ArgumentCaptor.forClass(List.class);
        verify(perfilCandidato).definirHabilidades(eq(USUARIO_CANDIDATO), enviadas.capture());
        assertThat(enviadas.getValue()).containsExactly(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO));
    }

    @Test
    void deberiaResponder400ConElIndiceDeLaHabilidadSinNivel() throws Exception {
        mockMvc.perform(put("/api/candidatos/me/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{ \"habilidadId\": \"" + JAVA.id() + "\", \"nivel\": \"BASICO\" },"
                                + " { \"habilidadId\": \"" + UUID.randomUUID() + "\" }]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("habilidades[1].nivel: es obligatorio"));
        verify(perfilCandidato, never()).definirHabilidades(any(), any());
    }

    @Test
    void deberiaResponder400ConMasDe30Habilidades() throws Exception {
        StringBuilder muchas = new StringBuilder("[");
        for (int i = 0; i < 31; i++) {
            muchas.append(i == 0 ? "" : ",")
                    .append("{ \"habilidadId\": \"").append(UUID.randomUUID()).append("\", \"nivel\": \"BASICO\" }");
        }

        mockMvc.perform(put("/api/candidatos/me/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON).content(muchas.append("]").toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("habilidades: máximo 30")));
    }

    @Test
    void deberiaAceptarUnaListaVaciaParaBorrarTodas() throws Exception {
        when(perfilCandidato.definirHabilidades(eq(USUARIO_CANDIDATO), any())).thenReturn(perfil());

        mockMvc.perform(put("/api/candidatos/me/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON).content("[]"))
                .andExpect(status().isOk());
        verify(perfilCandidato).definirHabilidades(USUARIO_CANDIDATO, List.of());
    }

    private String token(Rol rol) {
        UUID usuario = rol == Rol.CANDIDATO ? USUARIO_CANDIDATO : UUID.randomUUID();
        return "Bearer " + jwtService.generarToken(usuario, rol.name().toLowerCase() + "@example.com", rol);
    }

    private static PerfilCandidato perfil() {
        Candidato candidato = Candidato.nuevo(USUARIO_CANDIDATO, "Laura", "Gómez", "UdeA", "Ingeniería de Sistemas",
                EstadoAcademico.ESTUDIANTE, 9, "Medellín")
                .conHabilidades(List.of(new CandidatoHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO)));
        return PerfilCandidato.de(new CandidatoConCuenta(candidato, "laura@udea.edu.co"), List.of(JAVA));
    }
}
