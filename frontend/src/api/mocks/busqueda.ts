import { ErrorApi } from '../cliente'
import type { CompatibilidadDetalle, FiltrosVacantes, NivelHabilidad, PaginaDto, VacanteResumen } from '../tipos'
import { perfilCandidatoActualMock } from './candidatos'
import { calcularCompatibilidadMock, recibePostulacionesMock } from './compatibilidad'
import { idsVacantesPostuladasMock, postulacionDeMock } from './postulaciones'
import { esperar } from './utilidades'
import { vacantesGuardadasMock } from './vacantes'

// Simulación de GET /api/vacantes (contrato 5.4) y de GET /api/vacantes/{id}/compatibilidad (6.1): solo CANDIDATO (403 para los demás), vacantes abiertas
// y vigentes, compatibilidad calculada al consultar y orden con desempate por la más reciente.

const normalizar = (texto: string) => texto.trim().toLowerCase()

export async function buscarVacantesMock(
  filtros: FiltrosVacantes,
  pagina: number,
  tamano: number,
): Promise<PaginaDto<VacanteResumen>> {
  await esperar()
  const candidato = perfilCandidatoActualMock()
  const postuladas = idsVacantesPostuladasMock(candidato.id)
  const ciudad = filtros.ciudad ? normalizar(filtros.ciudad) : ''

  const encontradas: VacanteResumen[] = vacantesGuardadasMock()
    .filter(
      (v) =>
        recibePostulacionesMock(v) &&
        (!filtros.modalidad || v.modalidad === filtros.modalidad) &&
        (!filtros.tipo || v.tipo === filtros.tipo) &&
        (!filtros.habilidadId || v.habilidades.some((h) => h.habilidadId === filtros.habilidadId)) &&
        (!ciudad || normalizar(v.ciudad) === ciudad),
    )
    .map((v) => ({
      id: v.id,
      titulo: v.titulo,
      empresa: v.empresa,
      tipo: v.tipo,
      modalidad: v.modalidad,
      ciudad: v.ciudad,
      salarioMin: v.salarioMin,
      salarioMax: v.salarioMax,
      fechaPublicacion: v.fechaPublicacion,
      fechaCierre: v.fechaCierre,
      compatibilidad: calcularCompatibilidadMock(v.habilidades, candidato.habilidades),
      postulado: postuladas.has(v.id),
    }))

  const masReciente = (a: VacanteResumen, b: VacanteResumen) => b.fechaPublicacion.localeCompare(a.fechaPublicacion)
  encontradas.sort(
    filtros.orden === 'fechaPublicacion'
      ? masReciente
      : (a, b) => b.compatibilidad - a.compatibilidad || masReciente(a, b),
  )

  return {
    contenido: encontradas.slice(pagina * tamano, (pagina + 1) * tamano),
    pagina,
    tamano,
    totalElementos: encontradas.length,
    totalPaginas: Math.ceil(encontradas.length / tamano),
  }
}

const VALOR_NIVEL: Record<NivelHabilidad, number> = { BASICO: 1, INTERMEDIO: 2, AVANZADO: 3 }

/** También para vacantes cerradas o vencidas (se llega desde "Mis postulaciones"). */
export async function obtenerCompatibilidadMock(vacanteId: string): Promise<CompatibilidadDetalle> {
  await esperar()
  const candidato = perfilCandidatoActualMock()
  const vacante = vacantesGuardadasMock().find((v) => v.id === vacanteId)
  if (!vacante) throw new ErrorApi(404, 'La vacante no existe')
  return {
    vacanteId,
    porcentaje: calcularCompatibilidadMock(vacante.habilidades, candidato.habilidades),
    habilidades: vacante.habilidades
      .map((requerida) => {
        const nivelCandidato = candidato.habilidades.find((h) => h.habilidadId === requerida.habilidadId)?.nivel ?? null
        return {
          ...requerida,
          nivelCandidato,
          cumple: nivelCandidato !== null && VALOR_NIVEL[nivelCandidato] >= VALOR_NIVEL[requerida.nivelRequerido],
        }
      })
      // Orden del contrato: obligatorias primero y luego por nombre
      .sort((a, b) => Number(b.obligatoria) - Number(a.obligatoria) || a.nombre.localeCompare(b.nombre, 'es')),
    postulacion: postulacionDeMock(candidato.id, vacanteId),
  }
}
