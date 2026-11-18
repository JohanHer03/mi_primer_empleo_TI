import type {
  CategoriaHabilidad,
  EstadoAcademico,
  EstadoPostulacion,
  EstadoVacante,
  Modalidad,
  NivelHabilidad,
  TipoVacante,
} from '../api/tipos'

// Textos y formatos en español para los valores que llegan de la API.

export const TEXTO_TIPO: Record<TipoVacante, string> = { PRACTICA: 'Práctica', JUNIOR: 'Junior' }

export const TEXTO_MODALIDAD: Record<Modalidad, string> = {
  PRESENCIAL: 'Presencial',
  HIBRIDA: 'Híbrida',
  REMOTA: 'Remota',
}

export const TEXTO_NIVEL: Record<NivelHabilidad, string> = {
  BASICO: 'Básico',
  INTERMEDIO: 'Intermedio',
  AVANZADO: 'Avanzado',
}

/** En el orden en que el backend lista el catálogo (contrato 4.1). */
export const TEXTO_CATEGORIA: Record<CategoriaHabilidad, string> = {
  LENGUAJE: 'Lenguajes',
  FRAMEWORK: 'Frameworks',
  BASE_DATOS: 'Bases de datos',
  HERRAMIENTA: 'Herramientas',
  BLANDA: 'Habilidades blandas',
}

export const TEXTO_ESTADO_ACADEMICO: Record<EstadoAcademico, string> = {
  ESTUDIANTE: 'Estudiante',
  EGRESADO: 'Egresado',
}

/** En el orden del flujo de la regla 5. */
export const TEXTO_ESTADO_POSTULACION: Record<EstadoPostulacion, string> = {
  ENVIADA: 'Enviada',
  EN_REVISION: 'En revisión',
  ENTREVISTA: 'Entrevista',
  CONTRATADO: 'Contratado',
  RECHAZADA: 'Rechazada',
}

/** Regla 5 (contrato 7.2): a qué estados puede pasar cada uno, primero el avance y luego el rechazo. */
export const SIGUIENTES_ESTADOS: Record<EstadoPostulacion, EstadoPostulacion[]> = {
  ENVIADA: ['EN_REVISION', 'RECHAZADA'],
  EN_REVISION: ['ENTREVISTA', 'RECHAZADA'],
  ENTREVISTA: ['CONTRATADO', 'RECHAZADA'],
  CONTRATADO: [],
  RECHAZADA: [],
}

/** Texto del botón que lleva a cada estado. */
export const TEXTO_ACCION_ESTADO: Record<EstadoPostulacion, string> = {
  ENVIADA: 'Enviada',
  EN_REVISION: 'Pasar a revisión',
  ENTREVISTA: 'Citar a entrevista',
  CONTRATADO: 'Contratar',
  RECHAZADA: 'Rechazar',
}

/** Clase de `Paginas.module.css` para la insignia: en curso (azul), contratado (verde), rechazada (gris). */
export const INSIGNIA_ESTADO_POSTULACION: Record<EstadoPostulacion, string> = {
  ENVIADA: 'insigniaENCURSO',
  EN_REVISION: 'insigniaENCURSO',
  ENTREVISTA: 'insigniaENCURSO',
  CONTRATADO: 'insigniaABIERTA',
  RECHAZADA: 'insigniaCERRADA',
}

const PORCENTAJE = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 1, maximumFractionDigits: 1 })

/** 66.7 → "66,7 %". */
export function formatearCompatibilidad(valor: number): string {
  return `${PORCENTAJE.format(valor)} %`
}

/** Rango de la compatibilidad para el color de su indicador. */
export function nivelCompatibilidad(valor: number): 'alta' | 'media' | 'baja' {
  if (valor >= 70) return 'alta'
  if (valor >= 40) return 'media'
  return 'baja'
}

const ZONA_COLOMBIA = 'America/Bogota'

/** Fecha de hoy en Colombia como "AAAA-MM-DD": el backend compara las fechas de cierre con este día. */
export function hoyEnColombia(): string {
  // en-CA formatea como AAAA-MM-DD
  return new Intl.DateTimeFormat('en-CA', { timeZone: ZONA_COLOMBIA }).format(new Date())
}

/** Una vacante abierta con la fecha de cierre pasada no se cierra sola: el frontend la muestra vencida (contrato 4.4). */
export function estadoVisible(estado: EstadoVacante, fechaCierre: string): 'ABIERTA' | 'CERRADA' | 'VENCIDA' {
  if (estado === 'CERRADA') return 'CERRADA'
  return fechaCierre < hoyEnColombia() ? 'VENCIDA' : 'ABIERTA'
}

/**
 * "2026-11-30" → "30 nov 2026". Una fecha sola se muestra tal cual (sin correr el día por la zona horaria);
 * un instante ("2026-11-04T02:00:00Z") se muestra con el día que era en Colombia.
 */
export function formatearFecha(fecha: string): string {
  const opciones = { day: 'numeric', month: 'short', year: 'numeric' } as const
  if (fecha.length > 10) {
    return new Intl.DateTimeFormat('es-CO', { ...opciones, timeZone: ZONA_COLOMBIA }).format(new Date(fecha))
  }
  const [anio, mes, dia] = fecha.split('-').map(Number)
  return new Intl.DateTimeFormat('es-CO', { ...opciones, timeZone: 'UTC' }).format(new Date(Date.UTC(anio, mes - 1, dia)))
}

const PESOS = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 })

export function formatearSalario(minimo: number | null, maximo: number | null): string | null {
  if (minimo !== null && maximo !== null) return `${PESOS.format(minimo)} – ${PESOS.format(maximo)}`
  if (minimo !== null) return `Desde ${PESOS.format(minimo)}`
  if (maximo !== null) return `Hasta ${PESOS.format(maximo)}`
  return null
}
