package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.List;
import org.junit.jupiter.api.Test;

class SolicitudPaginaTest {

    @Test
    void deberiaAceptarPaginasDesdeCeroYTamanosDe1A50() {
        assertThatCode(() -> new SolicitudPagina(0, 1)).doesNotThrowAnyException();
        assertThatCode(() -> new SolicitudPagina(3, 50)).doesNotThrowAnyException();
    }

    @Test
    void deberiaRechazarUnaPaginaNegativa() {
        assertThatThrownBy(() -> new SolicitudPagina(-1, 10))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("page: debe ser 0 o mayor");
    }

    @Test
    void deberiaRechazarTamanosFueraDeRango() {
        assertThatThrownBy(() -> new SolicitudPagina(0, 51))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("size: debe estar entre 1 y 50");
        assertThatThrownBy(() -> new SolicitudPagina(0, 0))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void deberiaTransformarElContenidoConservandoLosTotales() {
        Pagina<Integer> pagina = new Pagina<>(List.of(1, 2), 1, 2, 5, 3);

        Pagina<String> texto = pagina.map(n -> "n" + n);

        assertThat(texto.contenido()).containsExactly("n1", "n2");
        assertThat(texto.totalElementos()).isEqualTo(5);
        assertThat(texto.totalPaginas()).isEqualTo(3);
    }
}
