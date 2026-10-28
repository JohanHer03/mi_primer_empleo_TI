package com.primerempleo.infrastructure.adapter.in.web;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.primerempleo.application.port.in.PerfilEmpresaUseCase;
import com.primerempleo.application.port.in.PerfilEmpresaUseCase.ActualizarPerfilEmpresaComando;
import com.primerempleo.application.port.in.VerificacionEmpresaUseCase;
import com.primerempleo.domain.exception.RecursoNoEncontradoException;
import com.primerempleo.domain.model.Empresa;
import com.primerempleo.domain.model.EmpresaConCuenta;
import com.primerempleo.domain.model.Pagina;
import com.primerempleo.domain.model.Rol;
import com.primerempleo.domain.model.SolicitudPagina;
import com.primerempleo.infrastructure.config.CorsConfig;
import com.primerempleo.infrastructure.security.JwtService;
import com.primerempleo.infrastructure.security.SecurityConfig;
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

@WebMvcTest(EmpresaController.class)
@Import({SecurityConfig.class, CorsConfig.class, JwtService.class, EmpresaWebMapperImpl.class})
@MockitoBean(types = {PerfilEmpresaUseCase.class, VerificacionEmpresaUseCase.class})
@TestPropertySource(properties = {
        "app.cors.origins=http://localhost:5173",
        "app.jwt.secret=secreto-de-pruebas-con-mas-de-32-bytes-de-largo",
        "app.jwt.expiracion-minutos=120"
})
class EmpresaControllerTest {

    private static final UUID USUARIO_EMPRESA = UUID.randomUUID();
    private static final String PERFIL_EDITADO = """
            {
              "razonSocial": "Software Medellín S.A.S. BIC",
              "sector": "Fintech",
              "ciudad": "Envigado",
              "sitioWeb": "https://smbic.co"
            }
            """;

    private final MockMvc mockMvc;
    private final JwtService jwtService;
    private final PerfilEmpresaUseCase perfilEmpresa;
    private final VerificacionEmpresaUseCase verificacionEmpresa;

    @Autowired
    EmpresaControllerTest(MockMvc mockMvc, JwtService jwtService, PerfilEmpresaUseCase perfilEmpresa,
                          VerificacionEmpresaUseCase verificacionEmpresa) {
        this.mockMvc = mockMvc;
        this.jwtService = jwtService;
        this.perfilEmpresa = perfilEmpresa;
        this.verificacionEmpresa = verificacionEmpresa;
    }

    @Test
    void deberiaDevolverElPerfilDeLaEmpresaDelToken() throws Exception {
        when(perfilEmpresa.consultar(USUARIO_EMPRESA)).thenReturn(empresa(false));

        mockMvc.perform(get("/api/empresas/me").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.razonSocial").value("Software Medellín S.A.S."))
                .andExpect(jsonPath("$.nit").value("9001234567"))
                .andExpect(jsonPath("$.email").value("talento@softwaremedellin.co"))
                .andExpect(jsonPath("$.verificada").value(false))
                .andExpect(jsonPath("$.fechaCreacion").doesNotExist());
    }

    @Test
    void deberiaResponder403SiUnCandidatoPideElPerfilDeEmpresa() throws Exception {
        mockMvc.perform(get("/api/empresas/me").header(HttpHeaders.AUTHORIZATION, token(Rol.CANDIDATO, UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaActualizarElPerfilConElUsuarioDelToken() throws Exception {
        when(perfilEmpresa.actualizar(any())).thenReturn(empresa(false));

        mockMvc.perform(put("/api/empresas/me").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON).content(PERFIL_EDITADO))
                .andExpect(status().isOk());
        verify(perfilEmpresa).actualizar(new ActualizarPerfilEmpresaComando(
                USUARIO_EMPRESA, "Software Medellín S.A.S. BIC", "Fintech", "Envigado", "https://smbic.co"));
    }

    @Test
    void deberiaResponder400SiElPerfilNoEsValido() throws Exception {
        mockMvc.perform(put("/api/empresas/me").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"razonSocial\": \"\", \"sitioWeb\": \"smbic.co\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(containsString("razonSocial: es obligatoria")))
                .andExpect(jsonPath("$.mensaje").value(containsString("sitioWeb:")));
        verify(perfilEmpresa, never()).actualizar(any());
    }

    @Test
    void deberiaListarEmpresasPaginadasParaElAdmin() throws Exception {
        when(verificacionEmpresa.listar(false, new SolicitudPagina(0, 10)))
                .thenReturn(new Pagina<>(List.of(empresa(false)), 0, 10, 1, 1));

        mockMvc.perform(get("/api/empresas").param("verificada", "false")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN, UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].razonSocial").value("Software Medellín S.A.S."))
                .andExpect(jsonPath("$.contenido[0].fechaCreacion").value("2026-10-27T15:00:00Z"))
                .andExpect(jsonPath("$.pagina").value(0))
                .andExpect(jsonPath("$.tamano").value(10))
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.totalPaginas").value(1));
    }

    @Test
    void deberiaResponder400ConUnTamanoDePaginaMayorA50() throws Exception {
        mockMvc.perform(get("/api/empresas").param("size", "51")
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN, UUID.randomUUID())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("size: debe estar entre 1 y 50"));
    }

    @Test
    void deberiaResponder403SiUnaEmpresaListaEmpresas() throws Exception {
        mockMvc.perform(get("/api/empresas").header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deberiaVerificarUnaEmpresa() throws Exception {
        EmpresaConCuenta verificada = empresa(true);
        when(verificacionEmpresa.verificar(verificada.empresa().id())).thenReturn(verificada);

        mockMvc.perform(patch("/api/empresas/{id}/verificar", verificada.empresa().id())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN, UUID.randomUUID())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verificada").value(true));
    }

    @Test
    void deberiaResponder404SiLaEmpresaNoExiste() throws Exception {
        when(verificacionEmpresa.verificar(any())).thenThrow(RecursoNoEncontradoException.empresa());

        mockMvc.perform(patch("/api/empresas/{id}/verificar", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.ADMIN, UUID.randomUUID())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("La empresa no existe"));
    }

    @Test
    void deberiaResponder403SiUnaEmpresaIntentaVerificar() throws Exception {
        mockMvc.perform(patch("/api/empresas/{id}/verificar", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, token(Rol.EMPRESA, USUARIO_EMPRESA)))
                .andExpect(status().isForbidden());
        verify(verificacionEmpresa, never()).verificar(eq(USUARIO_EMPRESA));
    }

    private String token(Rol rol, UUID usuarioId) {
        return "Bearer " + jwtService.generarToken(usuarioId, rol.name().toLowerCase() + "@example.com", rol);
    }

    private static EmpresaConCuenta empresa(boolean verificada) {
        Empresa empresa = Empresa.nueva(USUARIO_EMPRESA, "Software Medellín S.A.S.", "9001234567",
                "Desarrollo de software", "Medellín", "https://softwaremedellin.co");
        return new EmpresaConCuenta(verificada ? empresa.verificar() : empresa, "talento@softwaremedellin.co",
                Instant.parse("2026-10-27T15:00:00Z"));
    }
}
