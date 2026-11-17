import { peticion, usarMocks } from './cliente'
import { buscarVacantesMock, obtenerCompatibilidadMock } from './mocks/busqueda'
import {
  cerrarVacanteMock,
  editarVacanteMock,
  listarMisVacantesMock,
  obtenerVacanteMock,
  publicarVacanteMock,
} from './mocks/vacantes'
import type {
  CompatibilidadDetalle,
  EstadoVacante,
  FiltrosVacantes,
  PaginaDto,
  VacanteDeEmpresaResumen,
  VacanteDetalle,
  VacantePeticion,
  VacanteResumen,
} from './tipos'

// docs/contrato-api.md, secciones 3.2, 4.4, 5.4 y 6.1

export function publicarVacante(datos: VacantePeticion): Promise<VacanteDetalle> {
  if (usarMocks) return publicarVacanteMock(datos)
  return peticion<VacanteDetalle>('/vacantes', { metodo: 'POST', cuerpo: datos })
}

export function editarVacante(id: string, datos: VacantePeticion): Promise<VacanteDetalle> {
  if (usarMocks) return editarVacanteMock(id, datos)
  return peticion<VacanteDetalle>(`/vacantes/${id}`, { metodo: 'PUT', cuerpo: datos })
}

export function cerrarVacante(id: string): Promise<VacanteDetalle> {
  if (usarMocks) return cerrarVacanteMock(id)
  return peticion<VacanteDetalle>(`/vacantes/${id}/cerrar`, { metodo: 'PATCH' })
}

export function obtenerVacante(id: string): Promise<VacanteDetalle> {
  if (usarMocks) return obtenerVacanteMock(id)
  return peticion<VacanteDetalle>(`/vacantes/${id}`)
}

/** Vacantes de la empresa autenticada, las más recientes primero. */
export function listarMisVacantes(
  estado: EstadoVacante | undefined,
  pagina: number,
  tamano = 10,
): Promise<PaginaDto<VacanteDeEmpresaResumen>> {
  if (usarMocks) return listarMisVacantesMock(estado, pagina, tamano)
  return peticion<PaginaDto<VacanteDeEmpresaResumen>>('/empresas/me/vacantes', {
    parametros: { estado, page: pagina, size: tamano },
  })
}

/** Búsqueda del candidato: solo vacantes abiertas y vigentes, con su compatibilidad y si ya se postuló. */
export function buscarVacantes(
  filtros: FiltrosVacantes,
  pagina: number,
  tamano = 10,
): Promise<PaginaDto<VacanteResumen>> {
  if (usarMocks) return buscarVacantesMock(filtros, pagina, tamano)
  return peticion<PaginaDto<VacanteResumen>>('/vacantes', {
    parametros: { ...filtros, page: pagina, size: tamano },
  })
}

/** HU-04: compatibilidad de hoy del candidato con la vacante, cada habilidad con si la cumple y su postulación. */
export function obtenerCompatibilidad(vacanteId: string): Promise<CompatibilidadDetalle> {
  if (usarMocks) return obtenerCompatibilidadMock(vacanteId)
  return peticion<CompatibilidadDetalle>(`/vacantes/${vacanteId}/compatibilidad`)
}
