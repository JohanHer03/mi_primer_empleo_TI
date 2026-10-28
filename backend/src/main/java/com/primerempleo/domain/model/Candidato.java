package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.Objects;
import java.util.UUID;

/**
 * Perfil del estudiante o egresado que busca su primer empleo.
 * Regla: el semestre (1 a 12) es obligatorio para ESTUDIANTE y debe ir vacío para EGRESADO
 * (restricción ck_candidatos_semestre_estado de V3).
 */
public record Candidato(
        UUID id,
        UUID usuarioId,
        String nombres,
        String apellidos,
        String universidad,
        String programa,
        EstadoAcademico estadoAcademico,
        Integer semestre,
        String ciudad,
        String githubUrl,
        String resumen) {

    private static final int SEMESTRE_MINIMO = 1;
    private static final int SEMESTRE_MAXIMO = 12;

    public Candidato {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(usuarioId, "usuarioId");
        nombres = Textos.obligatorio(nombres, "nombres");
        apellidos = Textos.obligatorio(apellidos, "apellidos");
        universidad = Textos.opcional(universidad);
        programa = Textos.opcional(programa);
        ciudad = Textos.opcional(ciudad);
        githubUrl = Textos.opcional(githubUrl);
        resumen = Textos.opcional(resumen);
        if (estadoAcademico == null) {
            throw new DatosInvalidosException("estadoAcademico: es obligatorio");
        }
        validarSemestre(estadoAcademico, semestre);
    }

    /** Perfil recién registrado: el GitHub y el resumen se completan después, en el perfil. */
    public static Candidato nuevo(UUID usuarioId, String nombres, String apellidos, String universidad,
                                  String programa, EstadoAcademico estadoAcademico, Integer semestre, String ciudad) {
        return new Candidato(UUID.randomUUID(), usuarioId, nombres, apellidos, universidad, programa,
                estadoAcademico, semestre, ciudad, null, null);
    }

    private static void validarSemestre(EstadoAcademico estado, Integer semestre) {
        if (estado == EstadoAcademico.ESTUDIANTE) {
            if (semestre == null) {
                throw new DatosInvalidosException("semestre: es obligatorio para estudiantes");
            }
            if (semestre < SEMESTRE_MINIMO || semestre > SEMESTRE_MAXIMO) {
                throw new DatosInvalidosException("semestre: debe estar entre 1 y 12");
            }
        } else if (semestre != null) {
            throw new DatosInvalidosException("semestre: debe ir vacío para egresados");
        }
    }
}
