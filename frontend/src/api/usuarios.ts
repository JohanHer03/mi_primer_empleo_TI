import { peticion, usarMocks } from './cliente'
import { cambiarContrasenaMock } from './mocks/auth'
import type { CambiarContrasenaPeticion } from './tipos'

// docs/contrato-api.md, sección 8

/** Cualquier rol. Una contraseña actual errónea responde 400 (no 401), así que no cierra la sesión. */
export function cambiarContrasena(datos: CambiarContrasenaPeticion): Promise<void> {
  if (usarMocks) return cambiarContrasenaMock(datos)
  return peticion<void>('/usuarios/me/contrasena', { metodo: 'PUT', cuerpo: datos })
}
