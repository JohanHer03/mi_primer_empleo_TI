import { peticion, usarMocks } from './cliente'
import { actualizarMiPerfilMock, listarEmpresasMock, obtenerMiPerfilMock, verificarEmpresaMock } from './mocks/empresas'
import type { ActualizarEmpresaPeticion, EmpresaPerfil, EmpresaResumen, PaginaDto } from './tipos'

// docs/contrato-api.md, secciones 3.1, 4.2 y 4.3

export function obtenerMiPerfil(): Promise<EmpresaPerfil> {
  if (usarMocks) return obtenerMiPerfilMock()
  return peticion<EmpresaPerfil>('/empresas/me')
}

export function actualizarMiPerfil(datos: ActualizarEmpresaPeticion): Promise<EmpresaPerfil> {
  if (usarMocks) return actualizarMiPerfilMock(datos)
  return peticion<EmpresaPerfil>('/empresas/me', { metodo: 'PUT', cuerpo: datos })
}

/** Solo ADMIN (HU-10): las más antiguas primero; {@code verificada} undefined lista todas. */
export function listarEmpresas(
  verificada: boolean | undefined,
  pagina: number,
  tamano = 10,
): Promise<PaginaDto<EmpresaResumen>> {
  if (usarMocks) return listarEmpresasMock(verificada, pagina, tamano)
  return peticion<PaginaDto<EmpresaResumen>>('/empresas', { parametros: { verificada, page: pagina, size: tamano } })
}

/** Solo ADMIN: idempotente; quitar la verificación queda fuera del MVP. */
export function verificarEmpresa(id: string): Promise<EmpresaResumen> {
  if (usarMocks) return verificarEmpresaMock(id)
  return peticion<EmpresaResumen>(`/empresas/${id}/verificar`, { metodo: 'PATCH' })
}
