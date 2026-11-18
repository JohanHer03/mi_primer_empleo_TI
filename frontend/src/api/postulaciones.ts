import { peticion, usarMocks } from './cliente'
import {
  cambiarEstadoPostulacionMock,
  listarMisPostulacionesMock,
  listarPostulantesMock,
  postularseMock,
} from './mocks/postulaciones'
import type { EstadoPostulacion, PaginaDto, PostulacionDeCandidato, Postulante } from './tipos'

// docs/contrato-api.md, secciones 5.5, 5.6, 7.1 y 7.2

/** Reglas 1, 2 y 6: 409 si ya se postuló o la vacante no recibe postulaciones. */
export function postularse(vacanteId: string): Promise<PostulacionDeCandidato> {
  if (usarMocks) return postularseMock(vacanteId)
  return peticion<PostulacionDeCandidato>(`/vacantes/${vacanteId}/postulaciones`, { metodo: 'POST' })
}

/** Postulaciones del candidato autenticado, las más recientes primero. */
export function listarMisPostulaciones(
  estado: EstadoPostulacion | undefined,
  pagina: number,
  tamano = 10,
): Promise<PaginaDto<PostulacionDeCandidato>> {
  if (usarMocks) return listarMisPostulacionesMock(estado, pagina, tamano)
  return peticion<PaginaDto<PostulacionDeCandidato>>('/postulaciones/me', {
    parametros: { estado, page: pagina, size: tamano },
  })
}

/** HU-08: postulantes de una vacante de la empresa, por compatibilidad guardada (mayor primero) y fecha. */
export function listarPostulantes(
  vacanteId: string,
  estado: EstadoPostulacion | undefined,
  pagina: number,
  tamano = 10,
): Promise<PaginaDto<Postulante>> {
  if (usarMocks) return listarPostulantesMock(vacanteId, estado, pagina, tamano)
  return peticion<PaginaDto<Postulante>>(`/vacantes/${vacanteId}/postulaciones`, {
    parametros: { estado, page: pagina, size: tamano },
  })
}

/** HU-09: avanza o rechaza una postulación (regla 5); 409 si la transición no está permitida. */
export function cambiarEstadoPostulacion(postulacionId: string, estado: EstadoPostulacion): Promise<Postulante> {
  if (usarMocks) return cambiarEstadoPostulacionMock(postulacionId, estado)
  return peticion<Postulante>(`/postulaciones/${postulacionId}/estado`, { metodo: 'PATCH', cuerpo: { estado } })
}
