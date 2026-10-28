package com.primerempleo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.primerempleo.application.port.in.CrearHabilidadUseCase.CrearHabilidadComando;
import com.primerempleo.application.port.out.HabilidadRepositoryPort;
import com.primerempleo.domain.exception.RecursoDuplicadoException;
import com.primerempleo.domain.model.CategoriaHabilidad;
import com.primerempleo.domain.model.Habilidad;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HabilidadServiceTest {

    @Mock
    private HabilidadRepositoryPort habilidades;

    private HabilidadService servicio;

    @BeforeEach
    void preparar() {
        servicio = new HabilidadService(habilidades);
    }

    @Test
    void deberiaListarElCatalogoEnOrden() {
        when(habilidades.listar(null)).thenReturn(List.of(
                Habilidad.nueva("Git", CategoriaHabilidad.HERRAMIENTA),
                Habilidad.nueva("Java", CategoriaHabilidad.LENGUAJE)));

        List<Habilidad> catalogo = servicio.listar(null);

        assertThat(catalogo).extracting(Habilidad::nombre).containsExactly("Java", "Git");
    }

    @Test
    void deberiaPasarElFiltroDeCategoria() {
        when(habilidades.listar(CategoriaHabilidad.FRAMEWORK))
                .thenReturn(List.of(Habilidad.nueva("React", CategoriaHabilidad.FRAMEWORK)));

        assertThat(servicio.listar(CategoriaHabilidad.FRAMEWORK)).hasSize(1);
    }

    @Test
    void deberiaCrearUnaHabilidadNueva() {
        when(habilidades.crear(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        Habilidad creada = servicio.crear(new CrearHabilidadComando(" Kotlin ", CategoriaHabilidad.LENGUAJE));

        assertThat(creada.nombre()).isEqualTo("Kotlin");
        verify(habilidades).existePorNombre("Kotlin");
    }

    @Test
    void deberiaRechazarUnNombreRepetido() {
        when(habilidades.existePorNombre("java")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear(new CrearHabilidadComando("java", CategoriaHabilidad.LENGUAJE)))
                .isInstanceOf(RecursoDuplicadoException.class)
                .hasMessage("Ya existe una habilidad con ese nombre");
        verify(habilidades, never()).crear(any());
    }
}
