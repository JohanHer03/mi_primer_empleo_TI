package com.primerempleo.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

class ControladorErroresTest {

    private final ControladorErrores controlador = new ControladorErrores();

    @Test
    void deberiaUsarElCodigoDelErrorConFormatoUniforme() {
        MockHttpServletRequest peticion = new MockHttpServletRequest();
        peticion.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, 404);

        ResponseEntity<ErrorResponse> respuesta = controlador.manejarError(peticion);

        assertThat(respuesta.getStatusCode().value()).isEqualTo(404);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().status()).isEqualTo(404);
        assertThat(respuesta.getBody().error()).isEqualTo("Not Found");
        assertThat(respuesta.getBody().mensaje()).isEqualTo("La ruta solicitada no existe");
        assertThat(respuesta.getBody().timestamp()).isNotNull();
    }

    @Test
    void deberiaResponder500SiNoHayCodigoDeError() {
        ResponseEntity<ErrorResponse> respuesta = controlador.manejarError(new MockHttpServletRequest());

        assertThat(respuesta.getStatusCode().value()).isEqualTo(500);
        assertThat(respuesta.getBody().mensaje()).isEqualTo("Ocurrió un error inesperado");
    }
}
