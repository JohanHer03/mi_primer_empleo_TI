import { peticion, usarMocks } from './cliente'
import { listarMisPostulacionesMock, postularseMock } from './mocks/postulaciones'
import type { EstadoPostulacion, PaginaDto, PostulacionDeCandidato } from './tipos'

// docs/contrato-api.md, secciones 5.5 y 5.6

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
