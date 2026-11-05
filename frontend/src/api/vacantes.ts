import { peticion, usarMocks } from './cliente'
import {
  cerrarVacanteMock,
  editarVacanteMock,
  listarMisVacantesMock,
  obtenerVacanteMock,
  publicarVacanteMock,
} from './mocks/vacantes'
import type { EstadoVacante, PaginaDto, VacanteDeEmpresaResumen, VacanteDetalle, VacantePeticion } from './tipos'

// docs/contrato-api.md, secciones 3.2 y 4.4

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
