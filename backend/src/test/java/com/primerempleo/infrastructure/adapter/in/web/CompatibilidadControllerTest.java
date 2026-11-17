package com.primerempleo.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.ConsultarCompatibilidadUseCase;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.CompatibilidadDetalle;
import com.primerempleo.domain.model.CompatibilidadDetalle.HabilidadEvaluada;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.HabilidadDeVacante;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.math.BigDecimal;
import java.time.Instant;
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

@WebMvcTest(CompatibilidadController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, CompatibilidadWebMapperImpl.class})
@MockitoBean(types = ConsultarCompatibilidadUseCase.class)
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class CompatibilidadControllerTest {

    private static final UUID USUARIO_CANDIDATO = UUID.randomUUID();
    private static final UUID VACANTE = UUID.randomUUID();
    private static final Habilidad JAVA = Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad GIT = Habilidad.nueva("Git", CategoriaHabilidad.HERRAMIENTA);

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final ConsultarCompatibilidadUseCase compatibilidad;

    @Autowired
    CompatibilidadControllerTest(MockMvc mockMvc, JwtService jwtService, ConsultarCompatibilidadUseCase compatibilidad) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.compatibilidad = compatibilidad;
    }

    @Test
    void deberiaResponderElPorcentajeYCadaHabilidadConSiLaCumple() throws Exception {
        when(compatibilidad.consultar(USUARIO_CANDIDATO, VACANTE)).thenReturn(detalle(null));

        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vacanteId").value(VACANTE.toString()))
                .andExpect(jsonPath("$.porcentaje").value(33.3))
                .andExpect(jsonPath("$.habilidades[0].habilidadId").value(JAVA.id().toString()))
                .andExpect(jsonPath("$.habilidades[0].nombre").value("Java"))
                .andExpect(jsonPath("$.habilidades[0].categoria").value("LENGUAJE"))
                .andExpect(jsonPath("$.habilidades[0].nivelRequerido").value("INTERMEDIO"))
                .andExpect(jsonPath("$.habilidades[0].obligatoria").value(true))
                .andExpect(jsonPath("$.habilidades[0].nivelCandidato").value("BASICO"))
                .andExpect(jsonPath("$.habilidades[0].cumple").value(false))
                .andExpect(jsonPath("$.habilidades[1].nivelCandidato").isEmpty())
                .andExpect(jsonPath("$.habilidades[1].cumple").value(false))
                .andExpect(jsonPath("$.postulacion").isEmpty());
    }

    @Test
    void deberiaIncluirLaPostulacionConLaCompatibilidadGuardada() throws Exception {
        Postulacion postulacion = Postulacion.nueva(UUID.randomUUID(), VACANTE, new BigDecimal("20.0"),
                Instant.parse("2026-11-10T14:05:00Z"));
        when(compatibilidad.consultar(USUARIO_CANDIDATO, VACANTE)).thenReturn(detalle(postulacion));

        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postulacion.id").value(postulacion.id().toString()))
                .andExpect(jsonPath("$.postulacion.estado").value("ENVIADA"))
                .andExpect(jsonPath("$.postulacion.compatibilidad").value(20.0))
                .andExpect(jsonPath("$.postulacion.fecha").value("2026-11-10T14:05:00Z"))
                .andExpect(jsonPath("$.postulacion.candidatoId").doesNotExist());
    }

    @Test
    void deberiaResponder404SiLaVacanteNoExiste() throws Exception {
        when(compatibilidad.consultar(any(), any())).thenThrow(RecursoNoEncontradoException.vacante());

        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La vacante no existe"));
    }

    @Test
    void deberiaResponder403AUnaEmpresa() throws Exception {
        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isForbidden());
        verify(compatibilidad, never()).consultar(any(), any());
    }

    @Test
    void deberiaResponder400ConUnIdQueNoEsUuid() throws Exception {
        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", "no-es-uuid")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro id"));
    }

    @Test
    void deberiaResponder401SinToken() throws Exception {
        mockMvc.perform(get("/api/vacantes/{id}/compatibilidad", VACANTE)).andExpect(status().isUnauthorized());
    }

    private String token(Rol rol) {
        UUID usuario = rol == Rol.CANDIDATO ? USUARIO_CANDIDATO : UUID.randomUUID();
        return "Bearer " + jwtService.generarToken(usuario, rol.name().toLowerCase() + "@example.com", rol);
    }

    /** Java básico de intermedio obligatoria (0,5 × 2) y Git deseable que no tiene: 1 / 3. */
    private static CompatibilidadDetalle detalle(Postulacion postulacion) {
        return new CompatibilidadDetalle(VACANTE, new BigDecimal("33.3"), List.of(
                new HabilidadEvaluada(new HabilidadDeVacante(JAVA, NivelHabilidad.INTERMEDIO, true),
                        NivelHabilidad.BASICO, false),
                new HabilidadEvaluada(new HabilidadDeVacante(GIT, NivelHabilidad.BASICO, false), null, false)),
                postulacion);
    }
}
