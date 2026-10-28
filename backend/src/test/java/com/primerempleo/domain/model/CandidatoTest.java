package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CandidatoTest {

    private static final UUID USUARIO = UUID.randomUUID();

    @Test
    void deberiaCrearUnEstudianteConSemestre() {
        Candidato candidato = Candidato.nuevo(USUARIO, " Laura ", "Gómez", "UdeA", "Ingeniería de Sistemas",
                EstadoAcademico.ESTUDIANTE, 9, "Medellín");

        assertThat(candidato.id()).isNotNull();
        assertThat(candidato.nombres()).isEqualTo("Laura");
        assertThat(candidato.semestre()).isEqualTo(9);
    }

    @Test
    void deberiaCrearUnEgresadoSinSemestreYConLosOpcionalesVaciosComoNulos() {
        Candidato candidato = Candidato.nuevo(USUARIO, "Andrés", "Restrepo", "  ", null,
                EstadoAcademico.EGRESADO, null, "");

        assertThat(candidato.semestre()).isNull();
        assertThat(candidato.universidad()).isNull();
        assertThat(candidato.ciudad()).isNull();
    }

    @Test
    void deberiaExigirSemestreAUnEstudiante() {
        assertThatThrownBy(() -> Candidato.nuevo(USUARIO, "Laura", "Gómez", null, null,
                EstadoAcademico.ESTUDIANTE, null, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: es obligatorio para estudiantes");
    }

    @Test
    void deberiaRechazarUnSemestreFueraDeRango() {
        assertThatThrownBy(() -> Candidato.nuevo(USUARIO, "Laura", "Gómez", null, null,
                EstadoAcademico.ESTUDIANTE, 13, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: debe estar entre 1 y 12");
    }

    @Test
    void deberiaRechazarSemestreEnUnEgresado() {
        assertThatThrownBy(() -> Candidato.nuevo(USUARIO, "Andrés", "Restrepo", null, null,
                EstadoAcademico.EGRESADO, 10, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: debe ir vacío para egresados");
    }

    @Test
    void deberiaExigirNombres() {
        assertThatThrownBy(() -> Candidato.nuevo(USUARIO, " ", "Gómez", null, null,
                EstadoAcademico.EGRESADO, null, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("nombres: es obligatorio");
    }
}
