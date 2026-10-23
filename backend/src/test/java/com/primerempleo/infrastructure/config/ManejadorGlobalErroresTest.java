package com.primerempleo.infrastructure.config;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

// Sin filtros de seguridad: aquí solo se prueba el formato de los errores.
// El controlador anidado se importa porque Spring Boot excluye las clases internas de las pruebas del escaneo.
@WebMvcTest(ManejadorGlobalErroresTest.ControladorPrueba.class)
@Import(ManejadorGlobalErroresTest.ControladorPrueba.class)
@AutoConfigureMockMvc(addFilters = false)
class ManejadorGlobalErroresTest {

    private final MockMvc mockMvc;

    @Autowired
    ManejadorGlobalErroresTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void deberiaResponder415ConFormatoUniformeSiElContenidoNoEsJson() throws Exception {
        mockMvc.perform(post("/prueba/cuerpo").contentType(MediaType.TEXT_PLAIN).content("hola"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.mensaje").value("Tipo de contenido no soportado; usa application/json"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void deberiaResponder400ConElCampoInvalido() throws Exception {
        mockMvc.perform(post("/prueba/cuerpo").contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value(startsWith("nombre: ")));
    }

    @Test
    void deberiaResponder400SiElJsonEstaMalFormado() throws Exception {
        mockMvc.perform(post("/prueba/cuerpo").contentType(MediaType.APPLICATION_JSON).content("{mal json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El cuerpo de la petición no es válido"));
    }

    @Test
    void deberiaResponder400SiFaltaUnParametroObligatorio() throws Exception {
        mockMvc.perform(get("/prueba/pagina"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Falta el parámetro obligatorio pagina"));
    }

    @Test
    void deberiaResponder400SiElParametroTieneTipoInvalido() throws Exception {
        mockMvc.perform(get("/prueba/pagina").param("pagina", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Valor inválido para el parámetro pagina"));
    }

    @Test
    void deberiaResponder405SiElMetodoNoExiste() throws Exception {
        mockMvc.perform(delete("/prueba/pagina"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.mensaje").value("Método DELETE no permitido en esta ruta"));
    }

    @Test
    void deberiaConservarCodigoYMotivoDeResponseStatusException() throws Exception {
        mockMvc.perform(get("/prueba/conflicto"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.mensaje").value("Ya existe"));
    }

    @Test
    void deberiaSoportarCodigosHttpNoEstandar() throws Exception {
        mockMvc.perform(get("/prueba/codigo-raro"))
                .andExpect(status().is(499))
                .andExpect(jsonPath("$.status").value(499))
                .andExpect(jsonPath("$.error").value("Error"));
    }

    @Test
    void deberiaResponder403SiSeDeniegaElAcceso() throws Exception {
        mockMvc.perform(get("/prueba/denegado"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensaje").value("No tienes permiso para realizar esta acción"));
    }

    @Test
    void deberiaResponder500SinExponerDetallesInternos() throws Exception {
        mockMvc.perform(get("/prueba/falla"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado"));
    }

    record CuerpoPrueba(@NotBlank String nombre) {
    }

    @RestController
    @RequestMapping("/prueba")
    static class ControladorPrueba {

        @PostMapping("/cuerpo")
        String cuerpo(@Valid @RequestBody CuerpoPrueba cuerpo) {
            return cuerpo.nombre();
        }

        @GetMapping("/pagina")
        int pagina(@RequestParam int pagina) {
            return pagina;
        }

        @GetMapping("/conflicto")
        void conflicto() {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe");
        }

        @GetMapping("/codigo-raro")
        void codigoRaro() {
            throw new ResponseStatusException(HttpStatusCode.valueOf(499));
        }

        @GetMapping("/denegado")
        void denegado() {
            throw new AccessDeniedException("denegado");
        }

        @GetMapping("/falla")
        void falla() {
            throw new IllegalStateException("detalle interno que no debe salir");
        }
    }
}
