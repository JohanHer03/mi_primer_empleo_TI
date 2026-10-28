package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.List;
import org.junit.jupiter.api.Test;

class HabilidadTest {

    @Test
    void deberiaCrearLaHabilidadConElNombreRecortado() {
        Habilidad habilidad = Habilidad.nueva("  Spring Boot ", CategoriaHabilidad.FRAMEWORK);

        assertThat(habilidad.id()).isNotNull();
        assertThat(habilidad.nombre()).isEqualTo("Spring Boot");
    }

    @Test
    void deberiaExigirNombreYCategoria() {
        assertThatThrownBy(() -> Habilidad.nueva(" ", CategoriaHabilidad.LENGUAJE))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("nombre: es obligatorio");
        assertThatThrownBy(() -> Habilidad.nueva("Java", null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("categoria: es obligatoria");
    }

    @Test
    void deberiaOrdenarPorCategoriaYLuegoPorNombreEnEspanol() {
        List<Habilidad> desordenadas = List.of(
                Habilidad.nueva("Trabajo en equipo", CategoriaHabilidad.BLANDA),
                Habilidad.nueva("Python", CategoriaHabilidad.LENGUAJE),
                Habilidad.nueva("Comunicación", CategoriaHabilidad.BLANDA),
                Habilidad.nueva("Aprendizaje autónomo", CategoriaHabilidad.BLANDA),
                Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE),
                Habilidad.nueva("React", CategoriaHabilidad.FRAMEWORK));

        List<String> nombres = desordenadas.stream().sorted(Habilidad.ORDEN_CATALOGO).map(Habilidad::nombre).toList();

        assertThat(nombres).containsExactly(
                "Java", "Python", "React", "Aprendizaje autónomo", "Comunicación", "Trabajo en equipo");
    }
}
