package com.primerempleo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.ArrayList;
import java.util.List;
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

    @Test
    void deberiaEditarElPerfilConservandoIdUsuarioYHabilidades() {
        UUID java = UUID.randomUUID();
        Candidato candidato = estudiante().conHabilidades(List.of(new CandidatoHabilidad(java, NivelHabilidad.BASICO)));

        Candidato editado = candidato.conPerfil("Laura", "Gómez Restrepo", "UdeA", "Ingeniería de Sistemas",
                EstadoAcademico.EGRESADO, null, "Envigado", " https://github.com/lauragomez ", "Backend con Java.");

        assertThat(editado.id()).isEqualTo(candidato.id());
        assertThat(editado.usuarioId()).isEqualTo(USUARIO);
        assertThat(editado.githubUrl()).isEqualTo("https://github.com/lauragomez");
        assertThat(editado.semestre()).isNull();
        assertThat(editado.idsHabilidades()).containsExactly(java);
    }

    @Test
    void deberiaAplicarLaReglaDelSemestreTambienAlEditar() {
        assertThatThrownBy(() -> estudiante().conPerfil("Laura", "Gómez", null, null,
                EstadoAcademico.ESTUDIANTE, null, null, null, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("semestre: es obligatorio para estudiantes");
    }

    @Test
    void deberiaPermitirQuedarSinHabilidades() {
        assertThat(estudiante().conHabilidades(List.of()).habilidades()).isEmpty();
    }

    @Test
    void deberiaRechazarHabilidadesRepetidas() {
        UUID java = UUID.randomUUID();

        assertThatThrownBy(() -> estudiante().conHabilidades(List.of(
                new CandidatoHabilidad(java, NivelHabilidad.BASICO),
                new CandidatoHabilidad(java, NivelHabilidad.AVANZADO))))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: no se puede repetir una habilidad");
    }

    @Test
    void deberiaRechazarMasDe30Habilidades() {
        List<CandidatoHabilidad> muchas = new ArrayList<>();
        for (int i = 0; i < 31; i++) {
            muchas.add(new CandidatoHabilidad(UUID.randomUUID(), NivelHabilidad.BASICO));
        }

        assertThatThrownBy(() -> estudiante().conHabilidades(muchas))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: máximo 30");
    }

    @Test
    void deberiaOrdenarLasHabilidadesDelPerfilComoElCatalogo() {
        Habilidad git = new Habilidad(UUID.randomUUID(), "Git", CategoriaHabilidad.HERRAMIENTA);
        Habilidad java = new Habilidad(UUID.randomUUID(), "Java", CategoriaHabilidad.LENGUAJE);
        Candidato candidato = estudiante().conHabilidades(List.of(
                new CandidatoHabilidad(git.id(), NivelHabilidad.BASICO),
                new CandidatoHabilidad(java.id(), NivelHabilidad.INTERMEDIO)));

        PerfilCandidato perfil = PerfilCandidato.de(new CandidatoConCuenta(candidato, "laura@udea.edu.co"),
                List.of(git, java));

        assertThat(perfil.habilidades()).extracting(h -> h.habilidad().nombre()).containsExactly("Java", "Git");
    }

    @Test
    void deberiaRechazarElPerfilSiUnaHabilidadNoEstaEnElCatalogo() {
        UUID inexistente = UUID.randomUUID();
        Candidato candidato = estudiante().conHabilidades(List.of(new CandidatoHabilidad(inexistente, NivelHabilidad.BASICO)));

        assertThatThrownBy(() -> PerfilCandidato.de(new CandidatoConCuenta(candidato, "laura@udea.edu.co"), List.of()))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessage("habilidades: la habilidad " + inexistente + " no existe");
    }

    private static Candidato estudiante() {
        return Candidato.nuevo(USUARIO, "Laura", "Gómez", "UdeA", "Ingeniería de Sistemas",
                EstadoAcademico.ESTUDIANTE, 9, "Medellín");
    }
}
