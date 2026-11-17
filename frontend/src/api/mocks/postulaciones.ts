import { ErrorApi } from '../cliente'
import type {
  CompatibilidadDetalle,
  EstadoPostulacion,
  PaginaDto,
  PostulacionDeCandidato,
  VacanteDetalle,
} from '../tipos'
import { ID_CANDIDATO_DEMO, perfilCandidatoActualMock } from './candidatos'
import { calcularCompatibilidadMock, recibePostulacionesMock } from './compatibilidad'
import { esperar, guardar, leerGuardado } from './utilidades'
import { vacantesGuardadasMock } from './vacantes'

// Simulación de postularse y mis postulaciones (contrato 5.5 y 5.6), con las reglas 1, 2 y 6
// en el mismo orden del backend. El candidato de demostración ya tiene tres postulaciones en estados distintos.

const CLAVE_POSTULACIONES = 'primer-empleo.mock.postulaciones'

interface PostulacionGuardada {
  id: string
  candidatoId: string
  vacanteId: string
  fecha: string
  estado: EstadoPostulacion
  compatibilidad: number
}

const leerPostulaciones = () => leerGuardado<PostulacionGuardada[]>(CLAVE_POSTULACIONES, postulacionesDemo)

function postulacionesDemo(): PostulacionGuardada[] {
  const haceDias = (dias: number) => new Date(Date.now() - dias * 86_400_000).toISOString()
  const demo = (numero: number, vacanteId: string, estado: EstadoPostulacion, compatibilidad: number, dias: number) => ({
    id: `88888888-0000-4000-8000-00000000000${numero}`,
    candidatoId: ID_CANDIDATO_DEMO,
    vacanteId,
    fecha: haceDias(dias),
    estado,
    compatibilidad,
  })
  return [
    demo(1, '66666666-0000-4000-8000-000000000013', 'ENTREVISTA', 75, 6),
    demo(2, '66666666-0000-4000-8000-000000000002', 'EN_REVISION', 0, 20),
    demo(3, '66666666-0000-4000-8000-000000000003', 'RECHAZADA', 50, 45),
  ]
}

/** Ids de las vacantes a las que ya se postuló el candidato (`postulado` de la búsqueda). */
export function idsVacantesPostuladasMock(candidatoId: string): Set<string> {
  return new Set(leerPostulaciones().filter((p) => p.candidatoId === candidatoId).map((p) => p.vacanteId))
}

/** La postulación del candidato a la vacante, con la compatibilidad guardada, o null (contrato 6.1). */
export function postulacionDeMock(candidatoId: string, vacanteId: string): CompatibilidadDetalle['postulacion'] {
  const guardada = leerPostulaciones().find((p) => p.candidatoId === candidatoId && p.vacanteId === vacanteId)
  if (!guardada) return null
  const { id, fecha, estado, compatibilidad } = guardada
  return { id, fecha, estado, compatibilidad }
}

function aDto(postulacion: PostulacionGuardada, vacante: VacanteDetalle): PostulacionDeCandidato {
  return {
    id: postulacion.id,
    fecha: postulacion.fecha,
    estado: postulacion.estado,
    compatibilidad: postulacion.compatibilidad,
    vacante: {
      id: vacante.id,
      titulo: vacante.titulo,
      empresa: vacante.empresa,
      modalidad: vacante.modalidad,
      ciudad: vacante.ciudad,
      estado: vacante.estado,
      fechaCierre: vacante.fechaCierre,
    },
  }
}

export async function postularseMock(vacanteId: string): Promise<PostulacionDeCandidato> {
  await esperar()
  const candidato = perfilCandidatoActualMock()
  const vacante = vacantesGuardadasMock().find((v) => v.id === vacanteId)
  if (!vacante) throw new ErrorApi(404, 'La vacante no existe')
  if (idsVacantesPostuladasMock(candidato.id).has(vacanteId)) throw new ErrorApi(409, 'Ya te postulaste a esta vacante')
  if (!recibePostulacionesMock(vacante)) throw new ErrorApi(409, 'Esta vacante ya no recibe postulaciones')
  const postulacion: PostulacionGuardada = {
    id: crypto.randomUUID(),
    candidatoId: candidato.id,
    vacanteId,
    fecha: new Date().toISOString(),
    estado: 'ENVIADA',
    // Regla 6: se guarda la del momento
    compatibilidad: calcularCompatibilidadMock(vacante.habilidades, candidato.habilidades),
  }
  guardar(CLAVE_POSTULACIONES, [postulacion, ...leerPostulaciones()])
  return aDto(postulacion, vacante)
}

export async function listarMisPostulacionesMock(
  estado: EstadoPostulacion | undefined,
  pagina: number,
  tamano: number,
): Promise<PaginaDto<PostulacionDeCandidato>> {
  await esperar()
  const candidato = perfilCandidatoActualMock()
  const vacantes = vacantesGuardadasMock()
  const propias = leerPostulaciones()
    .filter((p) => p.candidatoId === candidato.id && (!estado || p.estado === estado))
    .sort((a, b) => b.fecha.localeCompare(a.fecha))
    .flatMap((p) => {
      const vacante = vacantes.find((v) => v.id === p.vacanteId)
      return vacante ? [aDto(p, vacante)] : []
    })
  return {
    contenido: propias.slice(pagina * tamano, (pagina + 1) * tamano),
    pagina,
    tamano,
    totalElementos: propias.length,
    totalPaginas: Math.ceil(propias.length / tamano),
  }
}
