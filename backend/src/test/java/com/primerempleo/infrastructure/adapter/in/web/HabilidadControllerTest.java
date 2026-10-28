package com.primerempleo.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.CrearHabilidadUseCase;
import com.primerempleo.application.port.in.ListarHabilidadesUseCase;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HabilidadController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, HabilidadWebMapperImpl.class})
@MockitoBean(types = {ListarHabilidadesUseCase.class, CrearHabilidadUseCase.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class HabilidadControllerTest {

    private static final String KOTLIN = """
            { "nombre": "Kotlin", "categoria": "LENGUAJE" }
            """;

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final ListarHabilidadesUseCase listarHabilidades;
    private final CrearHabilidadUseCase crearHabilidad;

    @Autowired
    HabilidadControllerTest(MockMvc mockMvc, JwtService jwtService, ListarHabilidadesUseCase listarHabilidades,
                            CrearHabilidadUseCase crearHabilidad) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.listarHabilidades = listarHabilidades;
        this.crearHabilidad = crearHabilidad;
    }

    @Test
    void deberiaListarElCatalogoParaCualquierUsuarioAutenticado() throws Exception {
        when(listarHabilidades.listar(null)).thenReturn(List.of(Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE)));

        mockMvc.perform(get("/api/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Java"))
                .andExpect(jsonPath("$[0].categoria").value("LENGUAJE"))
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void deberiaFiltrarPorCategoria() throws Exception {
        when(listarHabilidades.listar(CategoriaHabilidad.FRAMEWORK)).thenReturn(List.of());

        mockMvc.perform(get("/api/habilidades").param("categoria", "FRAMEWORK")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void deberiaResponder400ConUnaCategoriaInexistente() throws Exception {
        mockMvc.perform(get("/api/habilidades").param("categoria", "OTRA")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro categoria"));
    }

    @Test
    void deberiaResponder401SinToken() throws Exception {
        mockMvc.perform(get("/api/habilidades"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberiaPermitirAlAdminCrearUnaHabilidad() throws Exception {
        when(crearHabilidad.crear(any())).thenReturn(Habilidad.nueva("Kotlin", CategoriaHabilidad.LENGUAJE));

        mockMvc.perform(post("/api/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(KOTLIN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Kotlin"));
    }

    @Test
    void deberiaResponder403SiNoEsAdmin() throws Exception {
        mockMvc.perform(post("/api/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(KOTLIN))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para realizar esta acción"));
        verify(crearHabilidad, never()).crear(any());
    }

    @Test
    void deberiaResponder400SinNombreNiCategoria() throws Exception {
        mockMvc.perform(post("/api/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{ \"nombre\": \" \" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("nombre: es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(containsString("categoria: es obligatoria")));
    }

    @Test
    void deberiaResponder409SiElNombreSeRepite() throws Exception {
        when(crearHabilidad.crear(any())).thenThrow(RecursoDuplicadoException.habilidadRepetida());

        mockMvc.perform(post("/api/habilidades").header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(KOTLIN))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya existe una habilidad con ese nombre"));
    }

    private String token(Rol rol) {
        return "Bearer " + jwtService.generarToken(UUID.randomUUID(), rol.name().toLowerCase() + "@example.com", rol);
    }
}
