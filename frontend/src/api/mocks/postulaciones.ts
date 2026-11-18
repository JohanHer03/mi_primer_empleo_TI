import { ErrorApi } from '../cliente'
import type {
  CompatibilidadDetalle,
  EstadoPostulacion,
  PaginaDto,
  PostulacionDeCandidato,
  Postulante,
  VacanteDetalle,
} from '../tipos'
import { SIGUIENTES_ESTADOS } from '../../components/formato'
import { ID_CANDIDATO_DEMO, perfilCandidatoActualMock, perfilCandidatoPorIdMock } from './candidatos'
import { calcularCompatibilidadMock, recibePostulacionesMock } from './compatibilidad'
import { esperar, guardar, leerGuardado } from './utilidades'
import { vacantePropia, vacantesGuardadasMock } from './vacantes'

// Simulación de postularse y mis postulaciones (contrato 5.5 y 5.6), con las reglas 1, 2 y 6
// en el mismo orden del backend. El candidato de demostración ya tiene tres postulaciones en estados distintos,
// y cuatro candidatos sin cuenta se postularon a "Desarrollador Java junior" de la empresa de demostración
// (postulantes y cambio de estado, contrato 7.1 y 7.2, reglas 4 y 5).

const CLAVE_POSTULACIONES = 'primer-empleo.mock.postulaciones'

interface PostulacionGuardada {
  id: string
  candidatoId: string
  vacanteId: string
  fecha: string
  estado: EstadoPostulacion
  compatibilidad: number
}

const JAVA_JUNIOR = '66666666-0000-4000-8000-000000000001'

const leerPostulaciones = () => leerGuardado<PostulacionGuardada[]>(CLAVE_POSTULACIONES, postulacionesDemo)

function postulacionesDemo(): PostulacionGuardada[] {
  const haceDias = (dias: number) => new Date(Date.now() - dias * 86_400_000).toISOString()
  const demo = (
    numero: number,
    vacanteId: string,
    estado: EstadoPostulacion,
    compatibilidad: number,
    dias: number,
    candidatoId = ID_CANDIDATO_DEMO,
  ) => ({
    id: `88888888-0000-4000-8000-00000000000${numero}`,
    candidatoId,
    vacanteId,
    fecha: haceDias(dias),
    estado,
    compatibilidad,
  })
  return [
    demo(1, '66666666-0000-4000-8000-000000000013', 'ENTREVISTA', 75, 6),
    demo(2, '66666666-0000-4000-8000-000000000002', 'EN_REVISION', 0, 20),
    demo(3, '66666666-0000-4000-8000-000000000003', 'RECHAZADA', 50, 45),
    // "Desarrollador Java junior": Java intermedio y Spring Boot básico obligatorias, Git básico deseable
    demo(4, JAVA_JUNIOR, 'EN_REVISION', 100, 2, '77777777-0000-4000-8000-000000000001'),
    demo(5, JAVA_JUNIOR, 'ENVIADA', 60, 2, '77777777-0000-4000-8000-000000000002'),
    demo(6, JAVA_JUNIOR, 'ENVIADA', 60, 1, '77777777-0000-4000-8000-000000000003'),
    demo(7, JAVA_JUNIOR, 'RECHAZADA', 0, 3, '77777777-0000-4000-8000-000000000004'),
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

function aPostulante(postulacion: PostulacionGuardada): Postulante {
  const candidato = perfilCandidatoPorIdMock(postulacion.candidatoId)
  if (!candidato) throw new ErrorApi(404, 'El candidato no existe')
  const { id, fecha, estado, compatibilidad } = postulacion
  return { id, fecha, estado, compatibilidad, candidato }
}

/** HU-08: por compatibilidad guardada (mayor primero) y, con empate, quien se postuló antes. */
export async function listarPostulantesMock(
  vacanteId: string,
  estado: EstadoPostulacion | undefined,
  pagina: number,
  tamano: number,
): Promise<PaginaDto<Postulante>> {
  await esperar()
  vacantePropia(vacanteId)
  const lista = leerPostulaciones()
    .filter((p) => p.vacanteId === vacanteId && (!estado || p.estado === estado))
    .sort((a, b) => b.compatibilidad - a.compatibilidad || a.fecha.localeCompare(b.fecha) || a.id.localeCompare(b.id))
  return {
    contenido: lista.slice(pagina * tamano, (pagina + 1) * tamano).map(aPostulante),
    pagina,
    tamano,
    totalElementos: lista.length,
    totalPaginas: Math.ceil(lista.length / tamano),
  }
}

/** HU-09: mismo orden de comprobaciones que el backend (404, 403, 409) y los mismos mensajes. */
export async function cambiarEstadoPostulacionMock(postulacionId: string, nuevo: EstadoPostulacion): Promise<Postulante> {
  await esperar()
  const todas = leerPostulaciones()
  const postulacion = todas.find((p) => p.id === postulacionId)
  if (!postulacion) throw new ErrorApi(404, 'La postulación no existe')
  try {
    vacantePropia(postulacion.vacanteId)
  } catch (e) {
    if (e instanceof ErrorApi && e.status === 403) throw new ErrorApi(403, 'La postulación pertenece a otra empresa')
    throw e
  }
  const permitidos = SIGUIENTES_ESTADOS[postulacion.estado]
  if (permitidos.length === 0) {
    throw new ErrorApi(409, `Una postulación ${postulacion.estado} ya no cambia de estado`)
  }
  if (!permitidos.includes(nuevo)) {
    throw new ErrorApi(409, `Una postulación ${postulacion.estado} solo puede pasar a ${permitidos.join(' o ')}`)
  }
  const actualizada = { ...postulacion, estado: nuevo }
  guardar(CLAVE_POSTULACIONES, todas.map((p) => (p.id === postulacionId ? actualizada : p)))
  return aPostulante(actualizada)
}
