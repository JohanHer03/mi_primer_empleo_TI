import type { CategoriaHabilidad, EstadoVacante, Modalidad, NivelHabilidad, TipoVacante } from '../api/tipos'

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
