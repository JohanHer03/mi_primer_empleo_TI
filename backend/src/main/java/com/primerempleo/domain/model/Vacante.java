package com.primerempleo.domain.model;

import com.primerempleo.domain.exception.DatosInvalidosException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Vacante de práctica o junior que publica una empresa verificada (HU-07, docs/contrato-api.md 4.4).
 * Los salarios son pesos colombianos sin decimales.
 */
public record Vacante(
        UUID id,
        UUID empresaId,
        String titulo,
        String descripcion,
        TipoVacante tipo,
        Modalidad modalidad,
        String ciudad,
        Long salarioMin,
        Long salarioMax,
        EstadoVacante estado,
        Instant fechaPublicacion,
        LocalDate fechaCierre,
        List<VacanteHabilidad> habilidades) {

    /** Las fechas "vigentes" se comparan con el día en Colombia, como ck_vacantes_fecha_cierre de V3. */
    public static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");
    public static final int MAXIMO_HABILIDADES = 15;

    public Vacante {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(empresaId, "empresaId");
        Objects.requireNonNull(estado, "estado");
        Objects.requireNonNull(fechaPublicacion, "fechaPublicacion");
        titulo = Textos.obligatorio(titulo, "titulo");
        descripcion = Textos.obligatorio(descripcion, "descripcion");
        ciudad = Textos.obligatorio(ciudad, "ciudad");
        exigir(tipo != null, "tipo: es obligatorio");
        exigir(modalidad != null, "modalidad: es obligatoria");
        exigir(fechaCierre != null, "fechaCierre: es obligatoria");
        validarSalarios(salarioMin, salarioMax);
        habilidades = habilidades == null ? List.of() : List.copyOf(habilidades);
        validarHabilidades(habilidades);
    }

    /** Vacante recién publicada: abierta y con fecha de cierre de hoy en adelante. */
    public static Vacante nueva(UUID empresaId, DatosVacante datos, Instant ahora) {
        validarFechaCierre(datos.fechaCierre(), ahora);
        return new Vacante(UUID.randomUUID(), empresaId, datos.titulo(), datos.descripcion(), datos.tipo(),
                datos.modalidad(), datos.ciudad(), datos.salarioMin(), datos.salarioMax(), EstadoVacante.ABIERTA,
                ahora.truncatedTo(ChronoUnit.MICROS), datos.fechaCierre(), datos.habilidades());
    }

    /**
     * Reemplaza todos los datos, incluida la lista de habilidades. Conserva empresa, estado y fecha de publicación.
     * Que no esté cerrada lo comprueba el caso de uso (409).
     */
    public Vacante editar(DatosVacante datos, Instant ahora) {
        validarFechaCierre(datos.fechaCierre(), ahora);
        return new Vacante(id, empresaId, datos.titulo(), datos.descripcion(), datos.tipo(), datos.modalidad(),
                datos.ciudad(), datos.salarioMin(), datos.salarioMax(), estado, fechaPublicacion,
                datos.fechaCierre(), datos.habilidades());
    }

    public Vacante cerrar() {
        return new Vacante(id, empresaId, titulo, descripcion, tipo, modalidad, ciudad, salarioMin, salarioMax,
                EstadoVacante.CERRADA, fechaPublicacion, fechaCierre, habilidades);
    }

    public boolean estaCerrada() {
        return estado == EstadoVacante.CERRADA;
    }

    public boolean esDe(UUID empresaId) {
        return this.empresaId.equals(empresaId);
    }

    public List<UUID> idsHabilidades() {
        return habilidades.stream().map(VacanteHabilidad::habilidadId).toList();
    }

    private static void validarFechaCierre(LocalDate fechaCierre, Instant ahora) {
        LocalDate hoy = LocalDate.ofInstant(ahora, ZONA_COLOMBIA);
        if (fechaCierre != null && fechaCierre.isBefore(hoy)) {
            throw new DatosInvalidosException("fechaCierre: debe ser hoy o una fecha posterior");
        }
    }

    private static void validarSalarios(Long salarioMin, Long salarioMax) {
        exigir(salarioMin == null || salarioMin >= 0, "salarioMin: no puede ser negativo");
        exigir(salarioMax == null || salarioMax >= 0, "salarioMax: no puede ser negativo");
        exigir(salarioMin == null || salarioMax == null || salarioMin <= salarioMax,
                "salarioMin: no puede ser mayor que salarioMax");
    }

    /** De 1 a 15, sin repetir y al menos una obligatoria (sin ella no hay "lo que te falta"). */
    private static void validarHabilidades(List<VacanteHabilidad> habilidades) {
        exigir(!habilidades.isEmpty() && habilidades.size() <= MAXIMO_HABILIDADES,
                "habilidades: debe tener entre 1 y " + MAXIMO_HABILIDADES);
        Set<UUID> vistas = new HashSet<>();
        for (VacanteHabilidad habilidad : habilidades) {
            exigir(vistas.add(habilidad.habilidadId()), "habilidades: no se puede repetir una habilidad");
        }
        exigir(habilidades.stream().anyMatch(VacanteHabilidad::obligatoria),
                "habilidades: al menos una debe ser obligatoria");
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new DatosInvalidosException(mensaje);
        }
    }
}
