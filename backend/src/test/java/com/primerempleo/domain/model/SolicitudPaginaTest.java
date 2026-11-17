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
    void deberiaPaginarUnaListaEnMemoria() {
        List<Integer> siete = List.of(1, 2, 3, 4, 5, 6, 7);

        Pagina<Integer> ultima = Pagina.de(siete, new SolicitudPagina(2, 3));

        assertThat(ultima.contenido()).containsExactly(7);
        assertThat(ultima.totalElementos()).isEqualTo(7);
        assertThat(ultima.totalPaginas()).isEqualTo(3);
        assertThat(Pagina.de(siete, new SolicitudPagina(5, 3)).contenido()).isEmpty();
        assertThat(Pagina.de(List.of(), new SolicitudPagina(0, 10)).totalPaginas()).isZero();
    }

    @Test
    void deberiaRechazarUnOrdenDeVacantesDesconocido() {
        assertThat(OrdenVacantes.desde("fechaPublicacion")).isEqualTo(OrdenVacantes.FECHA_PUBLICACION);
        assertThatThrownBy(() -> OrdenVacantes.desde("salario"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("orden: debe ser compatibilidad o fechaPublicacion");
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
