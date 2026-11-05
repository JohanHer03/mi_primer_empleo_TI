package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil del estudiante o egresado que busca su primer empleo.
 * Regla: el semestre (1 a 12) es obligatorio para ESTUDIANTE y debe ir vacío para EGRESADO
 * (restricción ck_candidatos_semestre_estado de V3). Declara hasta 30 habilidades, sin repetir (docs/contrato-api.md, 5.2).
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
        String resumen,
        List<CandidatoHabilidad> habilidades) {

    private static final int SEMESTRE_MINIMO = 1;
    private static final int SEMESTRE_MAXIMO = 12;
    public static final int MAXIMO_HABILIDADES = 30;

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
        habilidades = habilidades == null ? List.of() : List.copyOf(habilidades);
        validarHabilidades(habilidades);
    }

    /** Perfil recién registrado: el GitHub, el resumen y las habilidades se completan después, en el perfil. */
    public static Candidato nuevo(UUID usuarioId, String nombres, String apellidos, String universidad,
                                  String programa, EstadoAcademico estadoAcademico, Integer semestre, String ciudad) {
        return new Candidato(UUID.randomUUID(), usuarioId, nombres, apellidos, universidad, programa,
                estadoAcademico, semestre, ciudad, null, null, List.of());
    }

    /** Datos editados por el candidato (docs/contrato-api.md, 5.1). Conserva sus habilidades. */
    public Candidato conPerfil(String nombres, String apellidos, String universidad, String programa,
                               EstadoAcademico estadoAcademico, Integer semestre, String ciudad, String githubUrl,
                               String resumen) {
        return new Candidato(id, usuarioId, nombres, apellidos, universidad, programa, estadoAcademico, semestre,
                ciudad, githubUrl, resumen, habilidades);
    }

    /** Reemplaza la lista completa de habilidades (5.2). */
    public Candidato conHabilidades(List<CandidatoHabilidad> nuevas) {
        return new Candidato(id, usuarioId, nombres, apellidos, universidad, programa, estadoAcademico, semestre,
                ciudad, githubUrl, resumen, nuevas);
    }

    public List<UUID> idsHabilidades() {
        return habilidades.stream().map(CandidatoHabilidad::habilidadId).toList();
    }

    private static void validarHabilidades(List<CandidatoHabilidad> habilidades) {
        if (habilidades.size() > MAXIMO_HABILIDADES) {
            throw new DatosInvalidosException("habilidades: máximo " + MAXIMO_HABILIDADES);
        }
        Set<UUID> vistas = new HashSet<>();
        for (CandidatoHabilidad habilidad : habilidades) {
            if (!vistas.add(habilidad.habilidadId())) {
                throw new DatosInvalidosException("habilidades: no se puede repetir una habilidad");
            }
        }
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
