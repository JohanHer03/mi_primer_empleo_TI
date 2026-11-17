package com.primerempleo.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.PostulacionesCandidatoUseCase;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.PostulacionDeCandidato;
import com.primerempleo.domain.model.PostulacionDeCandidato.VacantePostulada;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PostulacionController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, PostulacionWebMapperImpl.class})
@MockitoBean(types = PostulacionesCandidatoUseCase.class)
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class PostulacionControllerTest {

    private static final UUID USUARIO_CANDIDATO = UUID.randomUUID();
    private static final UUID VACANTE = UUID.randomUUID();

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final PostulacionesCandidatoUseCase postulaciones;

    @Autowired
    PostulacionControllerTest(MockMvc mockMvc, JwtService jwtService, PostulacionesCandidatoUseCase postulaciones) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.postulaciones = postulaciones;
    }

    @Test
    void deberiaPostularseYResponder201() throws Exception {
        when(postulaciones.postularse(USUARIO_CANDIDATO, VACANTE)).thenReturn(postulacion());

        mockMvc.perform(post("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.compatibilidad").value(66.7))
                .andExpect(jsonPath("$.fecha").value("2026-11-16T15:00:00Z"))
                .andExpect(jsonPath("$.candidatoId").doesNotExist())
                .andExpect(jsonPath("$.vacante.id").value(VACANTE.toString()))
                .andExpect(jsonPath("$.vacante.titulo").value("Desarrollador Java junior"))
                .andExpect(jsonPath("$.vacante.empresa.razonSocial").value("Software Medellín S.A.S."))
                .andExpect(jsonPath("$.vacante.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.vacante.fechaCierre").value("2026-11-30"));
    }

    @Test
    void deberiaResponder409SiYaSePostulo() throws Exception {
        when(postulaciones.postularse(any(), any())).thenThrow(RecursoDuplicadoException.postulacionRepetida());

        mockMvc.perform(post("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Ya te postulaste a esta vacante"));
    }

    @Test
    void deberiaResponder409SiLaVacanteYaNoRecibePostulaciones() throws Exception {
        when(postulaciones.postularse(any(), any())).thenThrow(OperacionNoPermitidaException.vacanteSinPostulaciones());

        mockMvc.perform(post("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Esta vacante ya no recibe postulaciones"));
    }

    @Test
    void deberiaResponder403SiUnaEmpresaIntentaPostularse() throws Exception {
        mockMvc.perform(post("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isForbidden());
        verify(postulaciones, never()).postularse(any(), any());
    }

    @Test
    void deberiaListarMisPostulacionesFiltradasPorEstado() throws Exception {
        when(postulaciones.listarMias(USUARIO_CANDIDATO, EstadoPostulacion.ENVIADA, new SolicitudPagina(0, 10)))
                .thenReturn(new Pagina<>(List.of(postulacion()), 0, 10, 1, 1));

        mockMvc.perform(get("/api/postulaciones/me").param("estado", "ENVIADA")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].vacante.titulo").value("Desarrollador Java junior"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deberiaResponder400ConUnEstadoDesconocido() throws Exception {
        mockMvc.perform(get("/api/postulaciones/me").param("estado", "APROBADA")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro estado"));
    }

    @Test
    void deberiaResponder401SinToken() throws Exception {
        mockMvc.perform(get("/api/postulaciones/me")).andExpect(status().isUnauthorized());
    }

    private String token(Rol rol) {
        UUID usuario = rol == Rol.CANDIDATO ? USUARIO_CANDIDATO : UUID.randomUUID();
        return "Bearer " + jwtService.generarToken(usuario, rol.name().toLowerCase() + "@example.com", rol);
    }

    private static PostulacionDeCandidato postulacion() {
        Postulacion postulacion = Postulacion.nueva(UUID.randomUUID(), VACANTE, new BigDecimal("66.7"),
                Instant.parse("2026-11-16T15:00:00Z"));
        return new PostulacionDeCandidato(postulacion, new VacantePostulada(VACANTE, "Desarrollador Java junior",
                UUID.randomUUID(), "Software Medellín S.A.S.", Modalidad.HIBRIDA, "Medellín", EstadoVacante.ABIERTA,
                LocalDate.of(2026, 11, 30)));
    }
}
