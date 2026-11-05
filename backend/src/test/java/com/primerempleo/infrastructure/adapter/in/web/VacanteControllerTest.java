package com.primerempleo.infrastructure.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.ConsultaVacantesUseCase;
import com.primerempleo.application.port.in.GestionVacantesUseCase;
import com.primerempleo.domain.exception.AccesoDenegadoException;
import com.primerempleo.domain.exception.OperacionNoPermitidaException;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.DatosVacante;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EstadoVacante;
import com.primerempleo.domain.model.Habilidad;
import com.primerempleo.domain.model.Modalidad;
import com.primerempleo.domain.model.NivelHabilidad;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.domain.model.TipoVacante;
import com.primerempleo.domain.model.Vacante;
import com.primerempleo.domain.model.VacanteDeEmpresaResumen;
import com.primerempleo.domain.model.VacanteDetalle;
import com.primerempleo.domain.model.VacanteHabilidad;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
import java.time.Instant;
import java.time.LocalDate;
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

@WebMvcTest(VacanteController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, VacanteWebMapperImpl.class})
@MockitoBean(types = {GestionVacantesUseCase.class, ConsultaVacantesUseCase.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class VacanteControllerTest {

    private static final Instant PUBLICACION = Instant.parse("2026-11-04T15:00:00Z");
    private static final UUID USUARIO_EMPRESA = UUID.randomUUID();
    private static final Habilidad JAVA = new Habilidad(UUID.randomUUID(), "Java", CategoriaHabilidad.LENGUAJE);
    private static final Habilidad GIT = new Habilidad(UUID.randomUUID(), "Git", CategoriaHabilidad.HERRAMIENTA);
    private static final String VACANTE = """
            {
              "titulo": "Desarrollador Java junior",
              "descripcion": "Apoyarás el desarrollo de APIs REST.",
              "tipo": "JUNIOR",
              "modalidad": "HIBRIDA",
              "ciudad": "Medellín",
              "salarioMin": 2800000,
              "salarioMax": 3500000,
              "fechaCierre": "2026-11-30",
              "habilidades": [
                { "habilidadId": "%s", "nivelRequerido": "INTERMEDIO", "obligatoria": true },
                { "habilidadId": "%s", "nivelRequerido": "BASICO", "obligatoria": false }
              ]
            }
            """.formatted(JAVA.id(), GIT.id());

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final GestionVacantesUseCase gestionVacantes;
    private final ConsultaVacantesUseCase consultaVacantes;

    @Autowired
    VacanteControllerTest(MockMvc mockMvc, JwtService jwtService, GestionVacantesUseCase gestionVacantes,
                          ConsultaVacantesUseCase consultaVacantes) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.gestionVacantes = gestionVacantes;
        this.consultaVacantes = consultaVacantes;
    }

    @Test
    void deberiaPublicarYResponder201ConElDetalle() throws Exception {
        when(gestionVacantes.publicar(eq(USUARIO_EMPRESA), any())).thenReturn(detalle(false));

        mockMvc.perform(post("/api/vacantes").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(VACANTE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("Desarrollador Java junior"))
                .andExpect(jsonPath("$.salarioMin").value(2800000))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.fechaPublicacion").value("2026-11-04T15:00:00Z"))
                .andExpect(jsonPath("$.fechaCierre").value("2026-11-30"))
                .andExpect(jsonPath("$.empresa.razonSocial").value("Software Medellín S.A.S."))
                .andExpect(jsonPath("$.empresa.nit").doesNotExist())
                .andExpect(jsonPath("$.habilidades[0].nombre").value("Java"))
                .andExpect(jsonPath("$.habilidades[0].categoria").value("LENGUAJE"))
                .andExpect(jsonPath("$.habilidades[0].nivelRequerido").value("INTERMEDIO"))
                .andExpect(jsonPath("$.habilidades[0].obligatoria").value(true));

        ArgumentCaptor<DatosVacante> datos = ArgumentCaptor.forClass(DatosVacante.class);
        verify(gestionVacantes).publicar(eq(USUARIO_EMPRESA), datos.capture());
        assertThat(datos.getValue().habilidades()).containsExactly(
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false));
    }

    @Test
    void deberiaResponder400SiFaltanCamposOHabilidades() throws Exception {
        mockMvc.perform(post("/api/vacantes").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"titulo\": \"\", \"salarioMin\": -1, \"habilidades\": [] }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("titulo: es obligatorio")))
                .andExpect(jsonPath("$.mensaje").value(containsString("salarioMin: no puede ser negativo")))
                .andExpect(jsonPath("$.mensaje").value(containsString("habilidades: debe tener entre 1 y 15")))
                .andExpect(jsonPath("$.mensaje").value(containsString("fechaCierre: es obligatoria")));
        verify(gestionVacantes, never()).publicar(any(), any());
    }

    @Test
    void deberiaResponder400SiUnaHabilidadNoTraeNivel() throws Exception {
        String sinNivel = VACANTE.replace("\"nivelRequerido\": \"BASICO\", ", "");

        mockMvc.perform(post("/api/vacantes").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(sinNivel))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("habilidades[1].nivelRequerido: es obligatorio"));
    }

    @Test
    void deberiaResponder403ConElMensajeDeLaRegla3() throws Exception {
        when(gestionVacantes.publicar(any(), any())).thenThrow(AccesoDenegadoException.empresaSinVerificar());

        mockMvc.perform(post("/api/vacantes").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(VACANTE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("Tu empresa debe estar verificada para publicar vacantes"));
    }

    @Test
    void deberiaResponder403SiUnCandidatoIntentaPublicar() throws Exception {
        mockMvc.perform(post("/api/vacantes").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO, UUID.randomUUID()))
                        .contentType(MediaType.APPLICATION_JSON).content(VACANTE))
                .andExpect(status().isForbidden());
        verify(gestionVacantes, never()).publicar(any(), any());
    }

    @Test
    void deberiaEditarLaVacanteDelPath() throws Exception {
        VacanteDetalle detalle = detalle(false);
        UUID id = detalle.vacante().id();
        when(gestionVacantes.editar(eq(USUARIO_EMPRESA), eq(id), any())).thenReturn(detalle);

        mockMvc.perform(put("/api/vacantes/{id}", id).header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(VACANTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void deberiaResponder409AlEditarUnaVacanteCerrada() throws Exception {
        when(gestionVacantes.editar(any(), any(), any())).thenThrow(OperacionNoPermitidaException.vacanteCerrada());

        mockMvc.perform(put("/api/vacantes/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(VACANTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No se puede editar una vacante cerrada"));
    }

    @Test
    void deberiaCerrarLaVacante() throws Exception {
        VacanteDetalle cerrada = detalle(true);
        when(gestionVacantes.cerrar(USUARIO_EMPRESA, cerrada.vacante().id())).thenReturn(cerrada);

        mockMvc.perform(patch("/api/vacantes/{id}/cerrar", cerrada.vacante().id())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }

    @Test
    void deberiaResponder403SiUnaEmpresaCierraUnaVacanteAjena() throws Exception {
        when(gestionVacantes.cerrar(any(), any())).thenThrow(AccesoDenegadoException.vacanteAjena());

        mockMvc.perform(patch("/api/vacantes/{id}/cerrar", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaMostrarElDetalleAUnCandidatoConSuRol() throws Exception {
        VacanteDetalle detalle = detalle(false);
        UUID candidato = UUID.randomUUID();
        when(consultaVacantes.consultar(candidato, Rol.CANDIDATO, detalle.vacante().id())).thenReturn(detalle);

        mockMvc.perform(get("/api/vacantes/{id}", detalle.vacante().id())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO, candidato)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Desarrollador Java junior"));
    }

    @Test
    void deberiaResponder404SiLaVacanteNoExiste() throws Exception {
        when(consultaVacantes.consultar(any(), any(), any())).thenThrow(RecursoNoEncontradoException.vacante());

        mockMvc.perform(get("/api/vacantes/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN, UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La vacante no existe"));
    }

    @Test
    void deberiaResponder401SinToken() throws Exception {
        mockMvc.perform(get("/api/vacantes/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deberiaListarLasVacantesDeLaEmpresaConSusPostulaciones() throws Exception {
        VacanteDeEmpresaResumen resumen = new VacanteDeEmpresaResumen(UUID.randomUUID(), "Desarrollador Java junior",
                TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín", EstadoVacante.ABIERTA, PUBLICACION,
                LocalDate.of(2026, 11, 30), 12);
        when(consultaVacantes.listarDeMiEmpresa(USUARIO_EMPRESA, EstadoVacante.ABIERTA, new SolicitudPagina(0, 10)))
                .thenReturn(new Pagina<>(List.of(resumen), 0, 10, 1, 1));

        mockMvc.perform(get("/api/empresas/me/vacantes").param("estado", "ABIERTA")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].titulo").value("Desarrollador Java junior"))
                .andExpect(jsonPath("$.contenido[0].totalPostulaciones").value(12))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    void deberiaResponder400ConUnEstadoDesconocido() throws Exception {
        mockMvc.perform(get("/api/empresas/me/vacantes").param("estado", "BORRADOR")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro estado"));
    }

    @Test
    void deberiaResponder403SiUnCandidatoListaVacantesDeEmpresa() throws Exception {
        mockMvc.perform(get("/api/empresas/me/vacantes")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO, UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    private String token(Rol rol, UUID usuarioId) {
        return "Bearer " + jwtService.generarToken(usuarioId, rol.name().toLowerCase() + "@example.com", rol);
    }

    private static VacanteDetalle detalle(boolean cerrada) {
        Empresa empresa = Empresa.nueva(USUARIO_EMPRESA, "Software Medellín S.A.S.", "9001234567",
                null, "Medellín", null).verificar();
        Vacante vacante = Vacante.nueva(empresa.id(), new DatosVacante("Desarrollador Java junior",
                "Apoyarás el desarrollo de APIs REST.", TipoVacante.JUNIOR, Modalidad.HIBRIDA, "Medellín",
                2_800_000L, 3_500_000L, LocalDate.of(2026, 11, 30), List.of(
                new VacanteHabilidad(JAVA.id(), NivelHabilidad.INTERMEDIO, true),
                new VacanteHabilidad(GIT.id(), NivelHabilidad.BASICO, false))), PUBLICACION);
        return VacanteDetalle.de(cerrada ? vacante.cerrar() : vacante, empresa, List.of(JAVA, GIT));
    }
}
