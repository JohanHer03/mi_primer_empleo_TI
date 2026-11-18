package com.primerempleo.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.GestionPostulantesUseCase;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.model.Candidato;
import com.primerempleo.domain.model.CandidatoConCuenta;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.EstadoAcademico;
import com.primerempleo.domain.model.EstadoPostulacion;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.PerfilCandidato;
import com.primerempleo.domain.model.PerfilCandidato.HabilidadDeCandidato;
import com.primerempleo.domain.model.Postulacion;
import com.primerempleo.domain.model.Postulante;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GestionPostulantesController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, PostulanteWebMapperImpl.class,
        CandidatoWebMapperImpl.class})
@MockitoBean(types = GestionPostulantesUseCase.class)
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class GestionPostulantesControllerTest {

    private static final UUID USUARIO_EMPRESA = UUID.randomUUID();
    private static final UUID VACANTE = UUID.randomUUID();
    private static final UUID POSTULACION = UUID.randomUUID();
    private static final Habilidad JAVA = Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE);

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final GestionPostulantesUseCase postulantes;

    @Autowired
    GestionPostulantesControllerTest(MockMvc mockMvc, JwtService jwtService, GestionPostulantesUseCase postulantes) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.postulantes = postulantes;
    }

    @Test
    void deberiaListarLosPostulantesConElPerfilDelCandidato() throws Exception {
        when(postulantes.listarDeVacante(USUARIO_EMPRESA, VACANTE, EstadoPostulacion.ENVIADA, new SolicitudPagina(0, 10)))
                .thenReturn(new Pagina<>(List.of(postulante(EstadoPostulacion.ENVIADA)), 0, 10, 1, 1));

        mockMvc.perform(get("/api/vacantes/{id}/postulaciones", VACANTE).param("estado", "ENVIADA")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.contenido[0].id").value(POSTULACION.toString()))
                .andExpect(jsonPath("$.contenido[0].estado").value("ENVIADA"))
                .andExpect(jsonPath("$.contenido[0].compatibilidad").value(50.0))
                .andExpect(jsonPath("$.contenido[0].fecha").value("2026-11-10T14:05:00Z"))
                .andExpect(jsonPath("$.contenido[0].candidato.nombres").value("Laura"))
                .andExpect(jsonPath("$.contenido[0].candidato.email").value("laura@udea.edu.co"))
                .andExpect(jsonPath("$.contenido[0].candidato.githubUrl").value("https://github.com/lauragomez"))
                .andExpect(jsonPath("$.contenido[0].candidato.habilidades[0].nombre").value("Java"))
                .andExpect(jsonPath("$.contenido[0].candidato.habilidades[0].nivel").value("BASICO"))
                .andExpect(jsonPath("$.contenido[0].candidatoId").doesNotExist())
                .andExpect(jsonPath("$.contenido[0].vacanteId").doesNotExist());
    }

    @Test
    void deberiaResponder403SiLaVacanteEsDeOtraEmpresa() throws Exception {
        when(postulantes.listarDeVacante(any(), any(), any(), any())).thenThrow(AccesoDenegadoException.vacanteAjena());

        mockMvc.perform(get("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("La vacante pertenece a otra empresa"));
    }

    @Test
    void deberiaResponder403AUnCandidatoQuePideLosPostulantes() throws Exception {
        mockMvc.perform(get("/api/vacantes/{id}/postulaciones", VACANTE)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO)))
                .andExpect(status().isForbidden());
        verify(postulantes, never()).listarDeVacante(any(), any(), any(), any());
    }

    @Test
    void deberiaCambiarElEstado() throws Exception {
        when(postulantes.cambiarEstado(USUARIO_EMPRESA, POSTULACION, EstadoPostulacion.EN_REVISION))
                .thenReturn(postulante(EstadoPostulacion.EN_REVISION));

        mockMvc.perform(patch("/api/postulaciones/{id}/estado", POSTULACION)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"EN_REVISION\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_REVISION"))
                .andExpect(jsonPath("$.candidato.apellidos").value("Gómez Restrepo"));
    }

    @Test
    void deberiaResponder409ConUnaTransicionNoPermitida() throws Exception {
        when(postulantes.cambiarEstado(any(), any(), any()))
                .thenThrow(OperacionNoPermitidaException.transicionNoPermitida(EstadoPostulacion.ENVIADA));

        mockMvc.perform(patch("/api/postulaciones/{id}/estado", POSTULACION)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CONTRATADO\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("Una postulación ENVIADA solo puede pasar a EN_REVISION o RECHAZADA"));
    }

    @Test
    void deberiaResponder400SinEstado() throws Exception {
        mockMvc.perform(patch("/api/postulaciones/{id}/estado", POSTULACION)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("estado: es obligatorio"));
        verify(postulantes, never()).cambiarEstado(any(), any(), any());
    }

    @Test
    void deberiaResponder400ConUnEstadoDesconocido() throws Exception {
        mockMvc.perform(patch("/api/postulaciones/{id}/estado", POSTULACION)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APROBADA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es válido"));
    }

    @Test
    void deberiaResponder403AUnCandidatoQueCambiaElEstado() throws Exception {
        mockMvc.perform(patch("/api/postulaciones/{id}/estado", POSTULACION)
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"EN_REVISION\"}"))
                .andExpect(status().isForbidden());
        verify(postulantes, never()).cambiarEstado(any(), any(), any());
    }

    private String token(Rol rol) {
        UUID usuario = rol == Rol.EMPRESA ? USUARIO_EMPRESA : UUID.randomUUID();
        return "Bearer " + jwtService.generarToken(usuario, rol.name().toLowerCase() + "@example.com", rol);
    }

    private static Postulante postulante(EstadoPostulacion estado) {
        Candidato candidato = Candidato.nuevo(UUID.randomUUID(), "Laura", "Gómez Restrepo", "Universidad de Antioquia",
                        "Ingeniería de Sistemas", EstadoAcademico.EGRESADO, null, "Medellín")
                .conPerfil("Laura", "Gómez Restrepo", "Universidad de Antioquia", "Ingeniería de Sistemas",
                        EstadoAcademico.EGRESADO, null, "Medellín", "https://github.com/lauragomez", "Me interesa el backend");
        Postulacion postulacion = new Postulacion(POSTULACION, candidato.id(), VACANTE,
                Instant.parse("2026-11-10T14:05:00Z"), estado, new BigDecimal("50.0"));
        PerfilCandidato perfil = new PerfilCandidato(new CandidatoConCuenta(candidato, "laura@udea.edu.co"),
                List.of(new HabilidadDeCandidato(JAVA, NivelHabilidad.BASICO)));
        return new Postulante(postulacion, perfil);
    }
}
