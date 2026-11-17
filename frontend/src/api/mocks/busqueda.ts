import type { FiltrosVacantes, PaginaDto, VacanteResumen } from '../tipos'
import { perfilCandidatoActualMock } from './candidatos'
import { calcularCompatibilidadMock, recibePostulacionesMock } from './compatibilidad'
import { idsVacantesPostuladasMock } from './postulaciones'
import { esperar } from './utilidades'
import { vacantesGuardadasMock } from './vacantes'

// Simulación de GET /api/vacantes (contrato 5.4): solo CANDIDATO (403 para los demás), vacantes abiertas
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
