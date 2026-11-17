import { hoyEnColombia } from '../../components/formato'
import type { CandidatoHabilidad, HabilidadRequerida, NivelHabilidad, VacanteDetalle } from '../tipos'

// Copia de CalculadoraCompatibilidad del backend (contrato 5.3), solo para el modo simulado:
// con la API real la compatibilidad siempre la calcula el backend.

const VALOR_NIVEL: Record<NivelHabilidad, number> = { BASICO: 1, INTERMEDIO: 2, AVANZADO: 3 }

/** Porcentaje de 0 a 100 con un decimal (mitad hacia arriba). Suma en sextos, como el backend, para no perder precisión. */
export function calcularCompatibilidadMock(requeridas: HabilidadRequerida[], delCandidato: CandidatoHabilidad[]): number {
  let puntos = 0
  let pesos = 0
  for (const requerida of requeridas) {
    const peso = requerida.obligatoria ? 2 : 1
    const propia = delCandidato.find((h) => h.habilidadId === requerida.habilidadId)
    const sextos = propia
      ? Math.min(6, (VALOR_NIVEL[propia.nivel] * 6) / VALOR_NIVEL[requerida.nivelRequerido])
      : 0
    puntos += sextos * peso
    pesos += peso
  }
  if (pesos === 0) return 0
  return Math.round((puntos * 1000) / (pesos * 6)) / 10
}

/** Regla 2: abierta y con la fecha de cierre vigente en hora de Colombia. */
export function recibePostulacionesMock(vacante: VacanteDetalle): boolean {
  return vacante.estado === 'ABIERTA' && vacante.fechaCierre >= hoyEnColombia()
}
