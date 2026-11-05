import { peticion, usarMocks } from './cliente'
import { actualizarMiPerfilMock, obtenerMiPerfilMock } from './mocks/empresas'
import type { ActualizarEmpresaPeticion, EmpresaPerfil } from './tipos'

// docs/contrato-api.md, sección 4.2

export function obtenerMiPerfil(): Promise<EmpresaPerfil> {
  if (usarMocks) return obtenerMiPerfilMock()
  return peticion<EmpresaPerfil>('/empresas/me')
}

export function actualizarMiPerfil(datos: ActualizarEmpresaPeticion): Promise<EmpresaPerfil> {
  if (usarMocks) return actualizarMiPerfilMock(datos)
  return peticion<EmpresaPerfil>('/empresas/me', { metodo: 'PUT', cuerpo: datos })
}
